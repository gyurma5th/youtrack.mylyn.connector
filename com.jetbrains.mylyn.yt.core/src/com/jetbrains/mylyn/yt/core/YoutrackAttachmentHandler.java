/**
 * 
 */
package com.jetbrains.mylyn.yt.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.UnsubmittedTaskAttachment;

import com.jetbrains.youtrack.javarest.client.YouTrackClient;

/**
 * @author evoVaGy3
 *
 */
public class YoutrackAttachmentHandler extends AbstractTaskAttachmentHandler {

	//private static final String URL = "URL";
	private static final String NAME = "NAME";
	private YouTrackRepositoryConnector repositoryConnector;

	public YoutrackAttachmentHandler(YouTrackRepositoryConnector rc) {
		this.repositoryConnector = rc;
	}

	@Override
	public boolean canGetContent(TaskRepository repository, ITask task) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler#canPostContent(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.ITask)
	 */
	@Override
	public boolean canPostContent(TaskRepository repository, ITask task) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler#getContent(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.tasks.core.data.TaskAttribute, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream getContent(TaskRepository repository, ITask task, TaskAttribute attachmentAttribute,
			IProgressMonitor monitor) throws CoreException {
		YouTrackClient client = YouTrackRepositoryConnector.getClient(repository);
		TaskAttachmentMapper attachmentMapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
		try {
			return client.getAttachment(attachmentMapper.getUrl());
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(Status.ERROR, YouTrackCorePlugin.ID_PLUGIN, "Cannot load attachment." + e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler#postContent(org.eclipse.mylyn.tasks.core.TaskRepository, org.eclipse.mylyn.tasks.core.ITask, org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource, java.lang.String, org.eclipse.mylyn.tasks.core.data.TaskAttribute, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, "Add attachment", (int) source.getLength() + 1);
		
		UnsubmittedTaskAttachment attachment = new UnsubmittedTaskAttachment(source, attachmentAttribute);

		YouTrackClient client = YouTrackRepositoryConnector.getClient(repository);
		String issueId = YouTrackRepositoryConnector.getRealIssueId(task.getTaskId(), repository);
		client.addAttachment(issueId, attachment.getContentType(), attachment.getFileName(), source.getLength(),
				attachment.getDescription(), attachment.createInputStream(progress.newChild(1)),
				progress.newChild((int) source.getLength()));

	}

}

