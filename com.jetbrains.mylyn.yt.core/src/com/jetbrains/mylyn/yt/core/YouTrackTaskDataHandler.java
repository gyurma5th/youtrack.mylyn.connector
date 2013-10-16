/**
@author: amarch
 */

package com.jetbrains.mylyn.yt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.osgi.util.NLS;

import util.CastCheck;

import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackComment;
import com.jetbrains.youtrack.javarest.client.YouTrackCustomField;
import com.jetbrains.youtrack.javarest.client.YouTrackCustomField.YouTrackCustomFieldType;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;
import com.jetbrains.youtrack.javarest.utils.StateBundleValues;
import com.jetbrains.youtrack.javarest.utils.StateValue;

public class YouTrackTaskDataHandler extends AbstractTaskDataHandler {

    private final YouTrackConnector connector;

    private static boolean enableEditMode = false;

    private static final String COMMENT_NEW = "TaskAttribute.COMMENT_NEW";

    private static final String LINK_PREFIX = "TaskAttribute.LINK";

    public static final String CUSTOM_FIELD_KIND = "TaslAttributeKind.CUSTOM_FIELD_KIND";

    public static final String SUMMARY_CREATED_FROM_ECLIPSE = "<Issue created from Eclipse Connector. Please specify issue summary.>";

    public YouTrackTaskDataHandler(YouTrackConnector connector) {
	this.connector = connector;
    }

    private String getNameFromLabel(TaskAttribute attribute) {
	if (attribute == null || attribute.getMetaData() == null
		|| attribute.getMetaData().getLabel() == null) {
	    return null;
	}
	String label = attribute.getMetaData().getLabel();
	return label.substring(0, label.length() - 1);
    }

    private String labelFromName(String name) {
	return name + ":";
    }

    private boolean isCustomField(YouTrackProject project, String field) {
	return project.getCustomFieldsMap().keySet().contains(field);
    }

    @Override
    public RepositoryResponse postTaskData(TaskRepository repository,
	    TaskData taskData, Set<TaskAttribute> oldAttributes,
	    IProgressMonitor monitor) throws CoreException {

	YouTrackClient client = YouTrackConnector.getClient(repository);
	YouTrackIssue issue = new YouTrackIssue();

	try {
	    issue = buildIssue(repository, taskData);
	    YouTrackProject project = YouTrackConnector.getProject(repository,
		    issue.property("projectShortName").toString());
	    Map<String, Object> changedCF = new HashMap<>();
	    for (String name : issue.getProperties().keySet()) {
		if (isCustomField(project, name)) {
		    changedCF.put(name, issue.getProperties().get(name));
		}
	    }
	    issue.getProperties().putAll(changedCF);

	    if (taskData.isNew()) {

		if (project.getModelIssue() != null) {
		    String modelIssueId = project.getModelIssue().getId();
		    client.updateIssue(modelIssueId, issue);
		    // project.setModelIssue(null);
		    return new RepositoryResponse(ResponseKind.TASK_CREATED,
			    modelIssueId);
		} else {
		    String uploadedIssueId = client.putNewIssue(issue);
		    issue.setId(uploadedIssueId);

		    StringBuilder addCFsCommand = new StringBuilder();

		    for (String name : issue.getProperties().keySet()) {
			if (isCustomField(project, name)) {
			    if (issue.getProperties().get(name) instanceof String) {
				addCFsCommand.append(name
					+ ": "
					+ issue.getProperties().get(name)
						.toString() + " ");
			    } else {
				addCFsCommand.append(name + " ");
				for (String value : (LinkedList<String>) issue
					.getProperties().get(name)) {
				    addCFsCommand.append(value + " ");
				}
			    }
			}
		    }

		    if (addCFsCommand.toString() != null) {
			client.applyCommand(issue.getId(),
				addCFsCommand.toString());
		    }
		    return new RepositoryResponse(ResponseKind.TASK_CREATED,
			    issue.getId());
		}

	    } else {
		// upload new comments
		String newComment = getNewComment(taskData);
		if (newComment != null && newComment.length() > 0) {
		    client.addComment(
			    taskData.getRoot()
				    .getAttribute(TaskAttribute.TASK_KEY)
				    .getValue(), newComment);
		    taskData.getRoot().getMappedAttribute(COMMENT_NEW)
			    .clearValues();
		}

		client.updateIssue(
			taskData.getRoot().getAttribute(TaskAttribute.TASK_KEY)
				.getValue(), issue);

		return new RepositoryResponse(ResponseKind.TASK_UPDATED,
			taskData.getTaskId());
	    }
	} catch (CoreException e) {
	    if (issue.getId() != null) {
		client.deleteIssue(issue.getId());
	    }
	    throw new CoreException(new org.eclipse.core.runtime.Status(
		    IStatus.ERROR, YouTrackCorePlugin.ID_PLUGIN, IStatus.OK,
		    "Couldn't upload new issue: \n"
			    + e.getStatus().getMessage(), e));
	}
    }

    @Override
    public boolean initializeTaskData(TaskRepository repository, TaskData data,
	    ITaskMapping initializationData, IProgressMonitor monitor)
	    throws CoreException {

	if (data.isNew() && initializationData.getProduct() != null) {
	    YouTrackProject project = YouTrackConnector.getProject(repository,
		    initializationData.getProduct());
	    TaskAttribute attribute = data.getRoot().createAttribute(
		    TaskAttribute.SUMMARY);
	    attribute.setValue(SUMMARY_CREATED_FROM_ECLIPSE);
	    attribute = data.getRoot().createAttribute(TaskAttribute.PRODUCT);
	    attribute.setValue(initializationData.getProduct());
	    attribute = data.getRoot().createAttribute(
		    TaskAttribute.DESCRIPTION);
	    attribute.setValue("");
	    YouTrackIssue issue;
	    try {
		issue = buildIssue(repository, data);
		YouTrackClient client = YouTrackConnector.getClient(repository);
		String uploadedIssueId = client.putNewIssue(issue);
		issue = client.getIssue(uploadedIssueId);
		project.setModelIssue(issue);
	    } catch (CoreException e) {
		throw new RuntimeException(e.getMessage());
	    }
	}

	TaskAttribute attribute = data.getRoot().createAttribute(
		TaskAttribute.SUMMARY);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_SHORT_RICH_TEXT)
		.setLabel("Summary:");

	attribute = data.getRoot().createAttribute(TaskAttribute.DATE_CREATION);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_DATETIME).setLabel("Created:");

	attribute = data.getRoot().createAttribute(TaskAttribute.TASK_KEY);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Issue key:");

	attribute = data.getRoot().createAttribute(
		TaskAttribute.DATE_MODIFICATION);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_DATETIME).setLabel("Updated:");

	attribute = data.getRoot().createAttribute(TaskAttribute.DESCRIPTION);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_LONG_RICH_TEXT)
		.setLabel("Description:");

	attribute = data.getRoot().createAttribute(TaskAttribute.USER_REPORTER);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_SHORT_RICH_TEXT)
		.setLabel("Reporter:");

	attribute = data.getRoot().createAttribute(TaskAttribute.USER_ASSIGNED);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Assignee:");

	attribute = data.getRoot().createAttribute(TaskAttribute.COMPONENT);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_TASK_DEPENDENCY)
		.setLabel("Relates to:");

	attribute = data.getRoot().createAttribute(TaskAttribute.PRIORITY);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_SHORT_TEXT)
		.setLabel("Priority level:");

	attribute = data.getRoot().createAttribute("ISSUE_URL");
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_URL).setLabel("Issue url:");

	attribute = data.getRoot().createAttribute(TaskAttribute.STATUS);
	attribute.getMetaData().setReadOnly(false)
		.setType(TaskAttribute.TYPE_BOOLEAN).setLabel("Resolved:");

	attribute = data.getRoot().createAttribute(TaskAttribute.PRODUCT);
	attribute.getMetaData().setReadOnly(true)
		.setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Project:");

	if (!data.isNew()) {
	    attribute = data.getRoot().createAttribute(COMMENT_NEW);
	    attribute.getMetaData().setReadOnly(false)
		    .setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
	}

	if (data.isNew() || enableEditMode) {
	    if (initializationData == null) {
		return false;
	    }

	    String product = initializationData.getProduct();
	    if (product == null) {
		return false;
	    }

	    YouTrackProject project = YouTrackConnector.getProject(repository,
		    product);

	    if (project == null) {
		return false;
	    }

	    if (!project.isCustomFieldsUpdated()) {
		project.updateCustomFields(YouTrackConnector
			.getClient(repository));
	    }

	    for (YouTrackCustomField field : project.getCustomFields()) {
		TaskAttribute customFieldAttribute = data.getRoot()
			.createAttribute(field.getName());
		customFieldAttribute.getMetaData().setReadOnly(true)
			.setLabel(labelFromName(field.getName()))
			.setKind(CUSTOM_FIELD_KIND);
		if (YouTrackCustomFieldType.getTypeByName(field.getType())
			.isSimple()) {
		    customFieldAttribute.getMetaData().setType(
			    TaskAttribute.TYPE_SHORT_TEXT);
		} else {
		    if (YouTrackCustomFieldType.getTypeByName(field.getType())
			    .singleField()) {
			customFieldAttribute.getMetaData().setType(
				TaskAttribute.TYPE_SINGLE_SELECT);
		    } else {
			customFieldAttribute.getMetaData().setType(
				TaskAttribute.TYPE_MULTI_SELECT);
		    }
		}
		if (project.getModelIssue() != null) {
		    if (project.getModelIssue().getProperties()
			    .containsKey(field.getName())) {
			customFieldAttribute.setValue(project.getModelIssue()
				.property(field.getName()).toString());
		    } else {
			customFieldAttribute.setValue(field.getEmptyText());
		    }
		}

	    }

	    attribute = data.getRoot()
		    .getMappedAttribute(TaskAttribute.PRODUCT);
	    attribute.setValue(product);
	    attribute.getMetaData().setReadOnly(true);

	}

	return true;
    }

    @Override
    public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
	return new TaskAttributeMapper(repository);
    }

    public TaskData readTaskData(TaskRepository repository,
	    YouTrackIssue issue, IProgressMonitor monitor) throws CoreException {
	try {
	    TaskData taskData = parseIssue(repository, issue, monitor);
	    return taskData;
	} catch (Exception e) {
	    throw new CoreException(new Status(IStatus.ERROR,
		    YouTrackCorePlugin.ID_PLUGIN, NLS.bind(
			    "Error parsing task {0}", issue.getId()), e));
	}
    }

    private TaskData parseIssue(TaskRepository repository, YouTrackIssue issue,
	    IProgressMonitor monitor) throws CoreException {

	issue.mapProperties();

	/*
	 * because valid id not contain '-': public static boolean
	 * isValidTaskId(String taskId) { return !taskId.contains(HANDLE_DELIM);
	 * } see also createTaskData() in JiraTaskDataHandler
	 */
	String issueId = issue.getId();
	TaskData taskData = new TaskData(getAttributeMapper(repository),
		repository.getConnectorKind(), repository.getRepositoryUrl(),
		issueId.substring(issueId.indexOf("-") + 1));
	initializeTaskData(repository, taskData, null, monitor);

	TaskAttribute attribute = taskData.getRoot().getAttribute(
		TaskAttribute.SUMMARY);
	attribute.setValue(issue.property("summary").toString());

	attribute = taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION);
	attribute.setValue(issue.property("description").toString());

	attribute = taskData.getRoot().getAttribute(TaskAttribute.TASK_KEY);
	attribute.setValue(issueId);

	attribute = taskData.getRoot()
		.getAttribute(TaskAttribute.DATE_CREATION);
	taskData.getAttributeMapper().setDateValue(attribute,
		new Date(Long.parseLong(issue.property("created").toString())));

	attribute = taskData.getRoot().getAttribute(
		TaskAttribute.DATE_MODIFICATION);
	taskData.getAttributeMapper().setDateValue(attribute,
		new Date(Long.parseLong(issue.property("updated").toString())));

	attribute = taskData.getRoot()
		.getAttribute(TaskAttribute.USER_REPORTER);
	attribute.setValue(issue.property("reporterName").toString());

	attribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
	attribute.setValue(issue.property("projectShortName").toString());
	YouTrackProject project = connector.getProject(repository, taskData
		.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue());

	if (!project.isCustomFieldsUpdated()) {
	    project.updateCustomFields(connector.getClient(repository));
	}
	// TODO: change everywhere project name to full project name
	// attribute.setValue(connector.getProject(repository,
	// issue.getProjectName()).getProjectFullName());

	attribute = taskData.getRoot()
		.getAttribute(TaskAttribute.USER_ASSIGNED);
	attribute.setValue(issue.property("Assignee").toString());

	// Duplicate field from custom fileds
	// because 'priority' icon needs priority level
	if (issue.getProperties().containsKey("Priority")) {
	    attribute = taskData.getRoot().getAttribute(TaskAttribute.PRIORITY);
	    attribute.setValue(connector.toPriorityLevel(
		    issue.property("Priority").toString(),
		    project.getCustomFieldsMap().get("Priority").getBundle()
			    .getValues()).toString());
	}

	attribute = taskData.getRoot().getAttribute("ISSUE_URL");
	attribute.setValue("<a href=\"" + taskData.getRepositoryUrl()
		+ "/issue/" + issue.getId() + "\">" + issue.getId() + "</a>");

	attribute = taskData.getRoot().getAttribute(TaskAttribute.COMPONENT);
	if (issue.getProperties().containsKey("links")) {
	    int count = 0;
	    if (issue.property("links") instanceof String) {

		TaskAttribute attr = taskData.getRoot().createAttribute(
			LINK_PREFIX + count);
		attribute.getMetaData().setReadOnly(true)
			.setType(TaskAttribute.TYPE_TASK_DEPENDENCY);
		attr.addValue(issue.property("links").toString());
		attribute.addValue(attr.getValue());
	    } else {
		for (String value : (Iterable<String>) issue.property("links")) {
		    TaskAttribute attr = taskData.getRoot().createAttribute(
			    LINK_PREFIX + count);
		    attribute.getMetaData().setReadOnly(true)
			    .setType(TaskAttribute.TYPE_TASK_DEPENDENCY);
		    attr.addValue(value);
		    attribute.addValue("\n" + attr.getValue());
		    count++;
		}
	    }
	}

	if (issue.getComments() != null && issue.getComments().size() > 0) {

	    TaskCommentMapper mapper = new TaskCommentMapper();
	    int count = 0;
	    for (YouTrackComment comment : issue.getComments()) {

		mapper.setAuthor(repository.createPerson(comment
			.getAuthorName()));
		mapper.setCreationDate(comment.getCreationDate());
		mapper.setText(comment.getText());
		mapper.setNumber(count);

		TaskAttribute commentAttribute = taskData.getRoot()
			.createAttribute(TaskAttribute.PREFIX_COMMENT + count);
		mapper.applyTo(commentAttribute);
		count++;
	    }
	}

	for (YouTrackCustomField field : project.getCustomFields()) {
	    TaskAttribute customFieldAttribute = taskData.getRoot()
		    .createAttribute(field.getName());
	    customFieldAttribute.getMetaData().setReadOnly(true)
		    .setLabel(labelFromName(field.getName()))
		    .setKind(CUSTOM_FIELD_KIND);
	    if (YouTrackCustomFieldType.getTypeByName(field.getType())
		    .isSimple()) {
		customFieldAttribute.getMetaData().setType(
			TaskAttribute.TYPE_SHORT_TEXT);
	    } else {
		if (YouTrackCustomFieldType.getTypeByName(field.getType())
			.singleField()) {
		    customFieldAttribute.getMetaData().setType(
			    TaskAttribute.TYPE_SINGLE_SELECT);
		} else {
		    customFieldAttribute.getMetaData().setType(
			    TaskAttribute.TYPE_MULTI_SELECT);
		}
	    }

	    if (issue.getProperties().containsKey(field.getName())) {

		// check if issue complete
		if (YouTrackCustomFieldType.getTypeByName(field.getType())
			.equals(YouTrackCustomFieldType.STATE)) {
		    LinkedList<StateValue> states = ((StateBundleValues) field
			    .getBundle().getBundleValues()).getStateValues();
		    for (StateValue state : states) {
			if (state.getValue().equals(
				issue.property("State").toString())) {
			    if (state.isResolved()) {
				attribute = taskData.getRoot().createAttribute(
					TaskAttribute.DATE_COMPLETION);
				attribute.getMetaData().setReadOnly(true)
					.setType(TaskAttribute.TYPE_DATETIME)
					.setLabel("Completed date:");
				taskData.getAttributeMapper()
					.setDateValue(
						attribute,
						taskData.getAttributeMapper()
							.getDateValue(
								taskData.getRoot()
									.getAttribute(
										TaskAttribute.DATE_MODIFICATION)));
				attribute = taskData.getRoot().getAttribute(
					TaskAttribute.STATUS);
				attribute.setValue("true");
			    } else {
				break;
			    }
			}
		    }
		}

		if (YouTrackCustomFieldType.getTypeByName(field.getType())
			.singleField()
			|| !(issue.property(field.getName()) instanceof List<?>)) {
		    customFieldAttribute.setValue(issue.property(
			    field.getName()).toString());
		} else {
		    customFieldAttribute.setValues((List<String>) issue
			    .property(field.getName()));
		}
	    } else {
		customFieldAttribute.setValue("");
		if (project.getCustomFieldsMap().get(field.getName())
			.getEmptyText() != null) {
		    customFieldAttribute.setValue(project.getCustomFieldsMap()
			    .get(field.getName()).getEmptyText());
		}
	    }
	}

	return taskData;
    }

    public YouTrackIssue buildIssue(TaskRepository repository, TaskData taskData)
	    throws CoreException {

	YouTrackIssue issue = new YouTrackIssue();

	if (taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).toString() != null) {
	    issue.addProperty("projectShortName", taskData.getRoot()
		    .getAttribute(TaskAttribute.PRODUCT).getValue());
	} else {
	    throw new CoreException(new org.eclipse.core.runtime.Status(
		    IStatus.ERROR, YouTrackCorePlugin.ID_PLUGIN, IStatus.OK,
		    "Wrong or null project name.", null));
	}

	if (taskData.getRoot().getAttribute(TaskAttribute.SUMMARY).toString() != null) {
	    issue.addProperty("summary",
		    taskData.getRoot().getAttribute(TaskAttribute.SUMMARY)
			    .getValue());
	    issue.addProperty("description",
		    taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION)
			    .getValue());
	} else {
	    throw new CoreException(new org.eclipse.core.runtime.Status(
		    IStatus.ERROR, YouTrackCorePlugin.ID_PLUGIN, IStatus.OK,
		    "Wrong or null summary.", null));
	}

	YouTrackProject project = YouTrackConnector.getProject(repository,
		taskData.getRoot().getAttribute(TaskAttribute.PRODUCT)
			.getValue());

	if (!project.isCustomFieldsUpdated()) {
	    project.updateCustomFields(YouTrackConnector.getClient(repository));
	}

	for (TaskAttribute attribute : taskData.getRoot().getAttributes()
		.values()) {
	    if (isCustomField(project, attribute.getId())
		    && attribute.getValue() != null
		    && !attribute.getValue().equals("")) {

		// TODO: fix this mess

		String emptyText = YouTrackConnector
			.getProject(
				repository,
				taskData.getRoot()
					.getAttribute(TaskAttribute.PRODUCT)
					.getValue()).getCustomFieldsMap()
			.get(getNameFromLabel(attribute)).getEmptyText();

		if (attribute.getValue().toString().equals(emptyText)) {
		    issue.addProperty(attribute.getId(), attribute.getValue()
			    .toString());
		} else {
		    Class fieldClass = YouTrackCustomFieldType
			    .getTypeByName(
				    YouTrackConnector
					    .getProject(
						    repository,
						    taskData.getRoot()
							    .getAttribute(
								    TaskAttribute.PRODUCT)
							    .getValue())
					    .getCustomFieldsMap()
					    .get(getNameFromLabel(attribute))
					    .getType()).getFieldValuesClass();
		    try {
			if (attribute.getMetaData().getType()
				.equals(TaskAttribute.TYPE_MULTI_SELECT)) {
			    LinkedList<String> multiValues = new LinkedList<>();
			    for (String value : attribute.getValues()) {
				multiValues.add(value);
			    }
			    issue.addProperty(attribute.getId(),
				    (Object) multiValues);
			} else {
			    issue.addProperty(attribute.getId(), CastCheck
				    .toObject(fieldClass, attribute.getValue())
				    .toString());
			}
		    } catch (NumberFormatException e) {
			throw new CoreException(
				new org.eclipse.core.runtime.Status(
					IStatus.ERROR,
					YouTrackCorePlugin.ID_PLUGIN,
					IStatus.OK,
					NLS.bind(
						"Wrong type of value in field \"{0}\".",
						attribute.getMetaData()
							.getLabel()), null));
		    }
		}
	    }
	}

	return issue;
    }

    public static List<String> unzipList(List<String> list) {
	if (list.size() > 0) {
	    String s = list.get(0);
	    if (s.startsWith("[") && s.endsWith("]")) {
		return new ArrayList<String>(Arrays.asList(s.substring(1,
			s.length() - 1).split(",")));
	    } else {
		return list;
	    }
	}
	return new ArrayList<String>();
    }

    @Override
    public void migrateTaskData(TaskRepository taskRepository, TaskData taskData) {

	if ((taskData != null && taskData.isNew()) || isEnableEditMode()) {

	    if (taskData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT) != null) {

		YouTrackProject project = YouTrackConnector.getProject(
			taskRepository,
			taskData.getRoot()
				.getMappedAttribute(TaskAttribute.PRODUCT)
				.getValue());

		for (TaskAttribute attr : taskData.getRoot().getAttributes()
			.values()) {
		    if (TaskAttribute.DESCRIPTION.equals(attr.getId())) {
			attr.getMetaData().setReadOnly(false);
		    } else if (TaskAttribute.SUMMARY.equals(attr.getId())) {
			attr.getMetaData().setReadOnly(false);
		    } else if (isCustomField(project, attr.getId())) {
			attr.getMetaData().setReadOnly(false);
			String customFieldName = getNameFromLabel(attr);
			if (project.getCustomFieldsMap().get(customFieldName) == null) {
			    project.updateCustomFields(YouTrackConnector
				    .getClient(taskRepository));
			}
			YouTrackCustomField customField = project
				.getCustomFieldsMap().get(customFieldName);
			if (!YouTrackCustomFieldType.getTypeByName(
				customField.getType()).isSimple()) {
			    LinkedList<String> values = customField.getBundle()
				    .getValues();
			    if (customField.getEmptyText() != null) {
				attr.putOption(customField.getEmptyText(),
					customField.getEmptyText());
			    }
			    if (values != null) {
				for (String value : values) {
				    attr.putOption(value, value);
				}
			    }
			}
		    }
		}
	    }
	}
    }

    private String getNewComment(TaskData taskData) {
	String newComment = "";
	TaskAttribute attribute = taskData.getRoot().getMappedAttribute(
		COMMENT_NEW);
	if (attribute != null) {
	    newComment = taskData.getAttributeMapper().getValue(attribute);
	}
	return newComment;
    }

    public static boolean isEnableEditMode() {
	return enableEditMode;
    }

    public static void setEnableEditMode(boolean enableEditMode) {
	YouTrackTaskDataHandler.enableEditMode = enableEditMode;
    }

}
