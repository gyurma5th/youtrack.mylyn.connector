/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.internal.tasks.core.data.TaskDataManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;


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

      TaskDataManager manager = TasksUiPlugin.getTaskDataManager();
      TaskData taskData = null;
      try {
        taskData = manager.getTaskData(task);
        TaskAttribute notWiki =
            taskData.getRoot().getAttribute(
                YouTrackTaskDataHandler.NOT_WIKI_COMMENT_PREFIX + taskComment.getNumber());
        return String.format("\nReply to comment:", notWiki.getValue());
      } catch (CoreException e) {
        return "";
      }
    }
  }

  @Override
  public ImageDescriptor getTaskPriorityOverlay(final ITask task) {
    ImageDescriptor descriptor = new ImageDescriptor() {

      @Override
      public ImageData getImageData() {
        TaskDataManager manager = TasksUiPlugin.getTaskDataManager();
        TaskData taskData = null;
        Image image;
        try {
          taskData = manager.getTaskData(task);
        } catch (CoreException e) {}

        if (taskData != null
            && taskData.getRoot().getAttributes().containsKey(TaskAttribute.PRIORITY)
            && taskData.getRoot().getMappedAttribute(TaskAttribute.PRIORITY).getValue() != null
            && taskData.getRoot().getMappedAttribute(TaskAttribute.PRIORITY).getValue().length() > 0
            && taskData.getRoot().getAttributes().containsKey("Priority")
            && taskData.getRoot().getMappedAttribute("Priority").getValue() != null) {
          String colorIndex =
              taskData.getRoot().getMappedAttribute(TaskAttribute.PRIORITY).getValue();
          String priority = taskData.getRoot().getMappedAttribute("Priority").getValue();
          image = getPriorityIconImage(Integer.parseInt(colorIndex), priority, true);
        } else {
          image = getPriorityIconImage(0, " ", true);
        }

        ImageData imageData = image.getImageData();
        return imageData;
      }
    };
    return descriptor;
  }

  public static Image getPriorityIconImage(int colorIndex, String priority, boolean small) {

    int width = 19;
    int height = 22;
    int xText = 4;
    int yText = 2;
    int fontHeight = 13;

    if (small) {
      width = 8;
      height = 14;
      xText = 1;
      yText = 0;
      fontHeight = 7;
    }
    String text = "";
    if (priority != null) {
      text = priority.substring(0, 1);
    }

    Color color = new Color(null, PriorityColorIndex.getColorByIndex(colorIndex).getBackground());
    final Image image = new Image(null, width, height);
    GC gc = new GC(image);
    gc.setBackground(color);
    gc.setForeground(new Color(null, PriorityColorIndex.getColorByIndex(colorIndex).getFont()));
    gc.fillRectangle(0, 0, width, height);
    Font font = new Font(null, "Arial", fontHeight, SWT.NONE);
    gc.setFont(font);
    int charWidth = gc.getCharWidth(text.charAt(0));
    gc.drawText(text, (width - charWidth) / 2, ((height - fontHeight) / 2) - 1, true);
    if (colorIndex == 20) {
      gc.drawRectangle(0, 0, width - 1, height - 1);
    }
    gc.dispose();

    return image;
  }
}
