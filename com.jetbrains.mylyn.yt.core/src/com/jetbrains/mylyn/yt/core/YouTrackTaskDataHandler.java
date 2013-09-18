/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.core;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.core.AbstractTaskCategory;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
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

import org.eclipse.swt.widgets.Link;


public class YouTrackTaskDataHandler extends AbstractTaskDataHandler{
	
	private final YouTrackConnector connector;
	
	private static boolean enableEditMode = false;
	
	private static final String COMMENT_NEW = "TaskAttribute.COMMENT_NEW";
	
	public YouTrackTaskDataHandler(YouTrackConnector connector) {
		this.connector = connector;
	}
	
	@Override
	public RepositoryResponse postTaskData(TaskRepository repository,
			TaskData taskData, Set<TaskAttribute> oldAttributes,
			IProgressMonitor monitor) throws CoreException {
		
		YouTrackClient client = YouTrackConnector.getClient(repository);
		YouTrackIssue issue = new YouTrackIssue();
		
		try {
			issue = buildIssue(repository, taskData);
			Map<String, String> changedCF = new HashMap<>();
			for(String key : issue.getProperties().keySet()){
				if(key.startsWith("CustomField")){
					changedCF.put(key.substring("CustomField".length(), key.length()-1),
							issue.getProperties().get(key).toString());
				}
			}
			issue.getProperties().putAll(changedCF);
			
			if(taskData.isNew()){
			
				String uploadedIssueId = client.putNewIssue(issue);
				issue.setId(uploadedIssueId);
				
				StringBuilder addCFsCommand = new StringBuilder();
				
				Set<String> customFiledsNames = client.getProjectCustomFieldNames(
						taskData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT).getValue());
				for(String key : customFiledsNames){
					if(key.startsWith("CustomField")){
						addCFsCommand.append(key + ": " + issue.getProperties().get(key).toString() + " ");
					}
				}
				
				if(addCFsCommand.toString() != null){
					client.applyCommand(issue.getId(), addCFsCommand.toString());
				}
				
				return new RepositoryResponse(ResponseKind.TASK_CREATED, issue.getId());
			}
			else{
				//upload new comments
				String newComment = getNewComment(taskData);
				if (newComment != null && newComment.length() > 0) {
						client.addComment(taskData.getRoot().getAttribute(TaskAttribute.TASK_KEY).getValue(), newComment);
						taskData.getRoot().getMappedAttribute(COMMENT_NEW).clearValues();
				}
				
				client.updateIssue(taskData.getRoot().getAttribute(TaskAttribute.TASK_KEY).getValue(), issue);
				
				return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskData.getTaskId());
			}
		} catch (CoreException e){
			if(issue.getId() != null){
				client.deleteIssue(issue.getId());
			}
			throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
					YouTrackCorePlugin.ID_PLUGIN, IStatus.OK, "Couldn't upload new issue: \n" + e.getStatus().getMessage() , e));
		}	
	}

	@Override
	public boolean initializeTaskData(TaskRepository repository, TaskData data,
			ITaskMapping initializationData, IProgressMonitor monitor)
			throws CoreException {

		TaskAttribute attribute = data.getRoot().createAttribute(TaskAttribute.SUMMARY);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_SHORT_RICH_TEXT).setLabel("Summary:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.DATE_CREATION);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_DATETIME).setLabel("Created:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.TASK_KEY);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Issue key:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.DATE_MODIFICATION);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_DATETIME).setLabel("Updated:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.DESCRIPTION);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setLabel("Description:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.USER_REPORTER);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_SHORT_RICH_TEXT).setLabel("Reporter:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.USER_ASSIGNED);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Assignee:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.COMPONENT);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_TASK_DEPENDENCY).setLabel("Related to:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.PRIORITY);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_SHORT_TEXT).setLabel("Priority level:");
		
		attribute = data.getRoot().createAttribute("ISSUE_URL");
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_URL).setLabel("Issue url:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.STATUS);
		attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_BOOLEAN).setLabel("Resolved:");
		
		attribute = data.getRoot().createAttribute(TaskAttribute.PRODUCT);
		attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Project:");
		
		if(!data.isNew()){
			attribute = data.getRoot().createAttribute(COMMENT_NEW);
			attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
		}
		
		if(data.isNew() || enableEditMode){
			if (initializationData == null) {
				return false;
			}
			
			String product = initializationData.getProduct();
			if (product == null) {
				return false;
			}
			
			
			YouTrackProject project = YouTrackConnector.getProject(repository,  product);
			
			if(!project.isCustomFieldsUpdated()){
				project.updateCustomFields(YouTrackConnector.getClient(repository));
			}
			
			int count = 0;
			for(YouTrackCustomField field : project.getCustomFields()){
				TaskAttribute customFieldAttribute = data.getRoot().createAttribute("CustomField" + count);
				customFieldAttribute.getMetaData().setReadOnly(true).setLabel(field.getName() + ":");
				if(YouTrackCustomFieldType.getTypeByName(field.getType()).isSimple()){
					customFieldAttribute.getMetaData().setType(TaskAttribute.TYPE_SHORT_TEXT);
				} else {
					if(YouTrackCustomFieldType.getTypeByName(field.getType()).singleField()){
						customFieldAttribute.getMetaData().setType(TaskAttribute.TYPE_SINGLE_SELECT);
					} else {
						customFieldAttribute.getMetaData().setType(TaskAttribute.TYPE_MULTI_SELECT);
					}
				}
				count++;
			}
			
			attribute = data.getRoot().getMappedAttribute(TaskAttribute.PRODUCT);
			attribute.setValue(product);
			attribute.getMetaData().setReadOnly(true);
			
		}
		
		return true;
	}

	@Override
	public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
		return new TaskAttributeMapper(repository);
	}

	public TaskData readTaskData(TaskRepository repository, YouTrackIssue issue, IProgressMonitor monitor) throws CoreException {
		try {
			TaskData taskData = parseIssue(repository, issue, monitor);
			return taskData;
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, YouTrackCorePlugin.ID_PLUGIN, NLS.bind(
					"Error parsing task {0}", issue.getId()), e));
		}
	}
	
	private TaskData parseIssue(TaskRepository repository, YouTrackIssue issue, IProgressMonitor monitor)
			throws CoreException {
		
		issue.mapProperties();
		
		/*
		because valid id not contain '-':
		public static boolean isValidTaskId(String taskId) {
			return !taskId.contains(HANDLE_DELIM);
		}
		see also createTaskData() in JiraTaskDataHandler
		*/
		
		String issueId = issue.getId();
		TaskData taskData = new TaskData(getAttributeMapper(repository), repository.getConnectorKind(),
				repository.getRepositoryUrl(), issueId.substring(issueId.indexOf("-") + 1));
		initializeTaskData(repository, taskData, null, monitor);
		
		TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.SUMMARY);
		attribute.setValue(issue.property("summary").toString());

		attribute = taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION);
		attribute.setValue(issue.property("description").toString());
		
		attribute = taskData.getRoot().getAttribute(TaskAttribute.TASK_KEY);
		attribute.setValue(issueId);

		attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION);
		taskData.getAttributeMapper().setDateValue(attribute, new Date(Long.parseLong(issue.property("created").toString())));
		
		attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION);
		taskData.getAttributeMapper().setDateValue(attribute, new Date(Long.parseLong(issue.property("updated").toString())));
		
		attribute = taskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER);
		attribute.setValue(issue.property("reporterName").toString());
		
		attribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
		attribute.setValue(issue.property("projectShortName").toString());
//		TODO: change everywhere project name to full project name
//		attribute.setValue(connector.getProject(repository, issue.getProjectName()).getProjectFullName());
		
		attribute = taskData.getRoot().getAttribute(TaskAttribute.USER_ASSIGNED);
		attribute.setValue(issue.property("Assignee").toString());
		
		// Duplicate field from custom fileds
		// because 'priority' icon needs priority level
		attribute = taskData.getRoot().getAttribute(TaskAttribute.PRIORITY);
		attribute.setValue( connector.toPriorityLevel(issue.property("Priority").toString()).toString());
		
		attribute = taskData.getRoot().getAttribute("ISSUE_URL");
		attribute.setValue("<a href=\"" + taskData.getRepositoryUrl() + "/issue/" + issue.getId() + "\">" + issue.getId() + "</a>");
		
		attribute = taskData.getRoot().getAttribute(TaskAttribute.COMPONENT);
		if(issue.property("links") instanceof String){
			attribute.addValue(issue.property("links").toString());
		} else {
			for(String value: (Iterable<String>) issue.property("links")){
				attribute.addValue(value);
			}
		}

		//TODO: Add other complete task statements
		if(issue.property("State").toString().equals("Fixed")){
			attribute = taskData.getRoot().createAttribute(TaskAttribute.DATE_COMPLETION);
			attribute.getMetaData().setReadOnly(true).setType(TaskAttribute.TYPE_DATETIME).setLabel("Completed date:");
			taskData.getAttributeMapper().setDateValue(attribute, 
					taskData.getAttributeMapper().getDateValue(taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION)));
			attribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
			attribute.setValue("true");
		}
		
		
		if(issue.getComments() != null && issue.getComments().size() > 0) {

			TaskCommentMapper mapper = new TaskCommentMapper();
			int count = 0;
			for(YouTrackComment comment: issue.getComments()){
				
				mapper.setAuthor(repository.createPerson(comment.getAuthorName()));
				mapper.setCreationDate(comment.getCreationDate());
				mapper.setText(comment.getText());
				mapper.setNumber(count);

				TaskAttribute commentAttribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + count);
				mapper.applyTo(commentAttribute);
				count++;
			}
		}
		
		YouTrackProject project = connector.getProject(repository, taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue());
		
		if(!project.isCustomFieldsUpdated()){
			project.updateCustomFields(connector.getClient(repository));
		}
		
		int count = 0;
		for(YouTrackCustomField field : project.getCustomFields()){
			TaskAttribute customFieldAttribute = taskData.getRoot().createAttribute("CustomField" + count);
			customFieldAttribute.getMetaData().setReadOnly(true).setLabel(field.getName() + ":");
			if(YouTrackCustomFieldType.getTypeByName(field.getType()).isSimple()){
				customFieldAttribute.getMetaData().setType(TaskAttribute.TYPE_SHORT_TEXT);
			} else {
				if(YouTrackCustomFieldType.getTypeByName(field.getType()).singleField()){
					customFieldAttribute.getMetaData().setType(TaskAttribute.TYPE_SINGLE_SELECT);
				} else {
					customFieldAttribute.getMetaData().setType(TaskAttribute.TYPE_MULTI_SELECT);
				}
			}
			
			if(issue.getProperties().containsKey(field.getName())){
				customFieldAttribute.setValue(issue.property(field.getName()).toString());
			} else{
				customFieldAttribute.setValue("");
				if(project.getCustomFieldsMap().get(field.getName()).getEmptyText() != null){
					customFieldAttribute.setValue(project.getCustomFieldsMap().get(field.getName()).getEmptyText());
				}
			}
			count++;
		}
		
		return taskData;
	}
	
	public YouTrackIssue buildIssue(TaskRepository repository, TaskData taskData) throws CoreException{
		
		YouTrackIssue issue = new YouTrackIssue();
		
		if (taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).toString() != null)
		{
			issue.addProperty("projectShortName", taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue());
		} else {
			throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
					YouTrackCorePlugin.ID_PLUGIN, IStatus.OK, "Wrong or null project name.", null));
		}
		
		if (taskData.getRoot().getAttribute(TaskAttribute.SUMMARY).toString() != null)
		{
			issue.addProperty("summary", taskData.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue());
			issue.addProperty("description", taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue());
		} else {
			throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
					YouTrackCorePlugin.ID_PLUGIN, IStatus.OK, "Wrong or null summary.", null));
		}
		
		YouTrackProject project = YouTrackConnector.getProject(repository, 
				taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue());
		
		if(!project.isCustomFieldsUpdated()){
			project.updateCustomFields(YouTrackConnector.getClient(repository));
		}
		
		for (TaskAttribute attribute : taskData.getRoot().getAttributes().values()) {
			if (attribute.getId().startsWith("CustomField") && attribute.getValue() != null && !attribute.getValue().equals("")){
				
				//TODO: fix this mess
				
				String emptyText = YouTrackConnector.getProject(repository, taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue()).
						getCustomFieldsMap().get(attribute.getMetaData().getLabel().replaceAll(":", "")).getEmptyText();
				
				if(attribute.getValue().toString().equals(emptyText)){
					issue.addProperty("CustomField" + attribute.getMetaData().getLabel(), 
							attribute.getValue().toString() );
				} else {
					Class fieldClass =  YouTrackCustomFieldType.getTypeByName(
							YouTrackConnector.getProject(repository, taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue()).
							getCustomFieldsMap().get(attribute.getMetaData().getLabel().replaceAll(":", "")).getType()).
							getFieldValuesClass();
					try{
						issue.addProperty("CustomField" + attribute.getMetaData().getLabel(), 
								CastCheck.toObject(fieldClass, attribute.getValue()).toString() );
					}
					catch(NumberFormatException e) {
						throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR,
								YouTrackCorePlugin.ID_PLUGIN, IStatus.OK, NLS.bind("Wrong type of value in field \"{0}\".", attribute.getMetaData().getLabel()), null));
					}
				}
			}
		}
		
		
		return issue;
	}
	
	@Override
	public void migrateTaskData(TaskRepository taskRepository, TaskData taskData) {
		
		if ((taskData != null && taskData.isNew()) || isEnableEditMode()) {
			
//			TODO: see exception here
			if(taskData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT) != null){
				
				YouTrackProject project = YouTrackConnector.getProject(taskRepository, taskData.getRoot().getMappedAttribute(TaskAttribute.PRODUCT).getValue());
				 
				for (TaskAttribute attr : taskData.getRoot().getAttributes().values()) {
					if (TaskAttribute.DESCRIPTION.equals(attr.getId())) {
						attr.getMetaData().setReadOnly(false);
					} else if (TaskAttribute.SUMMARY.equals(attr.getId())) {
						attr.getMetaData().setReadOnly(false);
					} else if (attr.getId().startsWith("CustomField")) {
						attr.getMetaData().setReadOnly(false);
						String customFieldName = attr.getMetaData().getLabel().replaceAll(":", "");
						if(project.getCustomFieldsMap().get(customFieldName) == null){
							project.updateCustomFields(YouTrackConnector.getClient(taskRepository));
						}
						YouTrackCustomField customField = project.getCustomFieldsMap().get(customFieldName);
						if(!YouTrackCustomFieldType.getTypeByName(customField.getType()).isSimple()){
							LinkedList<String> values  = customField.getBundle().getValues();
							if(values != null){
								for(String value: values){
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
		TaskAttribute attribute = taskData.getRoot().getMappedAttribute(COMMENT_NEW);
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
