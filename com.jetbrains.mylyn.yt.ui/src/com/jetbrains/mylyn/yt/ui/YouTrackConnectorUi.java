/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;


public class YouTrackConnectorUi extends AbstractRepositoryConnectorUi {

  public YouTrackConnectorUi() {}

  @Override
  public String getConnectorKind() {
    return YouTrackCorePlugin.CONNECTOR_KIND;
  }

  @Override
  public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
    return new YouTrackRepositorySettingsPage(taskRepository);
  }

  @Override
  public boolean hasSearchPage() {
    return false;
  }

  @Override
  public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
    return new NewYouTrackIssueWizard(repository, selection);
  }

  @Override
  public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
    YouTrackRepositoryQueryWizard wizard = new YouTrackRepositoryQueryWizard(repository);
    wizard.addPage(new YouTrackRepositoryQueryPage("youtrack.repository.query.page", repository,
        query));
    return wizard;
  }

  @Override
  public String getTaskKindLabel(ITask repositoryTask) {
    return "Issue";
  }

  @Override
  public String getReplyText(TaskRepository taskRepository, ITask task, ITaskComment taskComment,
      boolean includeTask) {
    if (taskComment == null) {
      return String.format("\nReply to description:", task.getAttribute(TaskAttribute.DESCRIPTION));
    } else {
      return String.format("\nReply to comment:", taskComment.getText());
    }
  }

  @Override
  public ImageDescriptor getTaskPriorityOverlay(ITask task) {
    ImageDescriptor descriptor = new ImageDescriptor() {

      @Override
      public ImageData getImageData() {
        ImageData imageData = new ImageData(8, 8, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        imageData.setPixel(5, 5, 5);
        return imageData;
      }
    };
    // return descriptor;
    return TasksUiInternal.getPriorityImage(task);
  }
}
