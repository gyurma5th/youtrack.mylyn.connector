/**
 * 
 */
package com.jetbrains.mylyn.yt.core;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.mylyn.internal.tasks.core.TaskAttachment;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.util.AttachmentUtil;
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
@SuppressWarnings("restriction")
public class YoutrackAttachmentHandler extends AbstractTaskAttachmentHandler {


	public YoutrackAttachmentHandler(YouTrackRepositoryConnector rc) {
	}

	@Override
	public boolean canGetContent(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public boolean canPostContent(TaskRepository repository, ITask task) {
		return true;
	}

	@Override
	public InputStream getContent(TaskRepository repository, ITask task, TaskAttribute attachmentAttribute,
			IProgressMonitor monitor) throws CoreException {
		YouTrackClient client = YouTrackRepositoryConnector.getClient(repository);
		TaskAttachmentMapper attachmentMapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
		try {
			return client.getAttachment(attachmentMapper.getUrl());
		} catch (URISyntaxException e) {
			throw new CoreException(new Status(Status.ERROR, YouTrackCorePlugin.ID_PLUGIN,
					"Cannot load attachment." + e.getMessage(), e));
		}
	}

	@Override
	public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment,
			TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, "Add attachment", (int) source.getLength() + 2);

		UnsubmittedTaskAttachment attachment = new UnsubmittedTaskAttachment(source, attachmentAttribute);

		YouTrackClient client = YouTrackRepositoryConnector.getClient(repository);
		String issueId = YouTrackRepositoryConnector.getRealIssueId(task.getTaskId(), repository);
		client.addAttachment(issueId, attachment.getContentType(), attachment.getFileName(), source.getLength(),
				attachment.getDescription(), attachment.createInputStream(progress.newChild(1)),
				progress.newChild((int) source.getLength()));

		if (!comment.isEmpty()) {
			StringBuilder builder = new StringBuilder();
			builder.append("=").append(attachment.getFileName()).append("=\n");
			String description = attachment.getDescription();
			if (AttachmentUtil.isContext(new TaskAttachment(repository, task, attachmentAttribute))) {
				description = Messages.AttachmentTableLabelProvider_Task_Context;
			}
			builder.append(description).append("\n");
			builder.append("[file:").append(client.createAttachmentName(attachment.getFileName(), attachment.getDescription()))
					.append("]\n\n");
			builder.append(comment);
			client.addComment(issueId, builder.toString());
		}

	}

}
