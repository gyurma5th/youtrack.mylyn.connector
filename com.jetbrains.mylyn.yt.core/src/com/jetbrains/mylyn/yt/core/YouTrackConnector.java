/**
@author: amarch
 */

package com.jetbrains.mylyn.yt.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.eclipse.osgi.util.NLS;

import com.jetbrains.youtrack.javarest.client.YouTrackIssue;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;

import com.jetbrains.youtrack.javarest.utils.MyRunnable;

public class YouTrackConnector extends AbstractRepositoryConnector {

	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	private static Map<TaskRepository, YouTrackClient> clientByRepository = new HashMap<TaskRepository, YouTrackClient>();
	
	private static Map<TaskRepository, HashSet<YouTrackProject>> projectsByRepository = new HashMap<>();

	private final YouTrackTaskDataHandler taskDataHandler;
	
	private static final int ISSUES_PER_ONE_QUERY = 20;

	public YouTrackConnector() {
		taskDataHandler = new YouTrackTaskDataHandler(this);
	}
	
	public static YouTrackProject getProject(TaskRepository repository, String projectname){
		if(!projectsByRepository.keySet().contains(repository)){
			YouTrackClient client = getClient(repository);
			List<YouTrackProject> projects = client.getProjects();
			projectsByRepository.put(repository, new HashSet<YouTrackProject>(projects));
		} 
		
		HashSet<YouTrackProject> projects = projectsByRepository.get(repository);
		
		for(YouTrackProject project : projects){
			if(project.getProjectShortName().equals(projectname)){
				return project;
			}
		}
		return null;
	}
	
	public static void updateProjectCustomFields(TaskRepository repository, String projectname){
		
		YouTrackProject project = YouTrackConnector.getProject(repository, projectname);
		
		YouTrackClient client  = clientByRepository.get(repository);
		
		if(project != null){
			if(!project.isCustomFieldsUpdated()){
				project.updateCustomFields(client);
			}
		} else {
			for(YouTrackProject repoProject : projectsByRepository.get(repository)){
				if(!repoProject.isCustomFieldsUpdated()){
					repoProject.updateCustomFields(client);
				}
			}
		}
	}
	

	public static synchronized YouTrackClient getClient(TaskRepository repository) {
		
		YouTrackClient client = clientByRepository.get(repository);
		
		//TODO: fix 
		try {
			if (client == null || ! new URL(client.getBaseServerURL()).getHost().
					equals(new URL(repository.getUrl()).getHost())) {
				
				client = YouTrackClient.createClient(repository.getUrl().toString());
					boolean login = client.login(repository.getUserName(), repository.getPassword());
					if (!login) {
//						throw new CoreException(new Status(IStatus.ERROR, YouTrackCorePlugin.ID_PLUGIN, NLS.bind(
//										"Can't login into  : {0}", repository.getUrl().toString())));
						throw new RuntimeException("Can't login into  : " + repository.getUrl().toString());
					}
				clientByRepository.put(repository, client);
			} /*else {
				try {
					boolean login = client.loginWithCredentials();
					if (!login) {
						throw new CoreException(new Status(IStatus.ERROR,
								YouTrackCorePlugin.ID_PLUGIN, NLS.bind("Can't login into  : {0}", repository.getUrl().toString())));
					}
				} catch (Exception e) {
					throw new CoreException(new Status(IStatus.ERROR,
							YouTrackCorePlugin.ID_PLUGIN, NLS.bind("Can't login into  : {0}", repository.getUrl().toString()), e));
				}

			}*/
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed url " + e.getMessage(), e);
//			throw new CoreException(new Status(IStatus.ERROR,
//					YouTrackCorePlugin.ID_PLUGIN, NLS.bind("MalformedURLException in url : {0}", repository.getUrl().toString()), e));
		}
		return client;
	}

	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		return true;
	}

	@Override
	public String getConnectorKind() {
		return YouTrackCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public String getLabel() {
		return "YouTrack Demo Repository";
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
		// ignore
		return null;
	}

	@Override
	public TaskData getTaskData(TaskRepository taskRepository, String taskId,
			IProgressMonitor monitor) throws CoreException {
		
		System.err.println(taskId);
		
		String realIssueId;
		if(taskId.contains("-")){
			realIssueId = taskId;
		} else {
			realIssueId = TasksUiPlugin.getTaskList().getTask(taskRepository.getRepositoryUrl(), taskId).getTaskKey();
		}
		
		YouTrackIssue issue = getClient(taskRepository).getIssue(realIssueId);
		// stall a while to allow the UI to update
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
		return taskDataHandler.readTaskData(taskRepository, issue, monitor);
	}

	@Override
	public String getTaskIdFromTaskUrl(String taskFullUrl) {
		// ignore
		return null;
	}

	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		// ignore
		return null;
	}

	@Override
	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task,
			TaskData taskData) {
		// ignore
		return false;
	}

	@Override
	public IStatus performQuery(final TaskRepository repository,
			final IRepositoryQuery query, final TaskDataCollector collector,
			ISynchronizationSession session, final IProgressMonitor monitor) {

		return new MyRunnable() {
			@Override
			public void run() throws Exception {
				List<YouTrackIssue> issues = null;
				String projectname = query.getAttribute(YouTrackCorePlugin.QUERY_KEY_PROJECT);
				String filter = query.getAttribute(YouTrackCorePlugin.QUERY_KEY_FILTER);
				
				int issuesCount = queryIssuesAmount(projectname, filter, repository);
				issues = getClient(repository).getIssuesByFilter(getFilter(projectname, filter, repository), issuesCount);

				for (YouTrackIssue issue : issues) {
					TaskData taskData = taskDataHandler.readTaskData(repository, issue, monitor);
					collector.accept(taskData);
				}
			}
		}.execute("Query failed", YouTrackCorePlugin.ID_PLUGIN);
		
	}
	
	public String getFilter(String projectname, String filter, TaskRepository repository) {
		
		String returnFilter = new String(filter);
		if(filter != null){
			if(projectname != null && !projectname.equals("")){
				returnFilter += "project: " + projectname;
			}
		} else{
			returnFilter = "project: " + projectname;
		}
		
		//TODO: fix
		returnFilter += " sort by: {issue id}";
		
		return returnFilter;
	
	}
	
	public int queryIssuesAmount(String projectname, String filter, TaskRepository repository) throws CoreException{
		return getClient(repository).getNumberOfIssues(getFilter(projectname, filter, repository));
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository taskRepository,
			IProgressMonitor monitor) throws CoreException {
		// ignore

	}

	@Override
	public TaskMapper getTaskMapping(TaskData taskData) {
		return new TaskMapper(taskData);
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository repository, ITask task,
			TaskData taskData) {
		getTaskMapping(taskData).applyTo(task);
	}

	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		return taskDataHandler;
	}
	
	public PriorityLevel toPriorityLevel(String priority) {
		switch (priority) {
		case "Show-stopper":
			return PriorityLevel.P1;
		case "Critical":
			return PriorityLevel.P2;
		case "Major":
			return PriorityLevel.P3;
		case "Normal":
			return PriorityLevel.P4;
		case "Minor":
			return PriorityLevel.P5;
		default:
			return null;
		}
	}

}
