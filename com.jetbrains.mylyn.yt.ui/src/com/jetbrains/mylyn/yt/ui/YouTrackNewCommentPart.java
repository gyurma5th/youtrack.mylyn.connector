package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.jetbrains.mylyn.yt.core.YouTrackRepositoryConnector;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;

public class YouTrackNewCommentPart extends TaskEditorRichTextPart {

  private String partId;

  public YouTrackNewCommentPart() {
    setPartName("New Comment");
    setSectionStyle(ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
    setExpandVertically(true);
  }

  @Override
  public void initialize(AbstractTaskEditorPage taskEditorPage) {
    super.initialize(taskEditorPage);
    setAttribute(getModel().getTaskData().getRoot()
        .getMappedAttribute(YouTrackTaskDataHandler.COMMENT_NEW));
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    if (getAttribute() == null) {
      return;
    }
    super.createControl(parent, toolkit);
  }

  @Override
  protected void fillToolBar(ToolBarManager manager) {
    Action submitAction = new Action() {
      @Override
      public void run() {
        doSubmit();
      }
    };
    submitAction.setToolTipText("Submit");
    submitAction.setImageDescriptor(TasksUiImages.REPOSITORY_SUBMIT);
    manager.add(submitAction);
  }

  private void doSubmit() {

    boolean taskEdited = false;
    if (!getTaskEditorPage().getModel().getTaskData().getRoot()
        .getMappedAttribute(TaskAttribute.SUMMARY).getMetaData().isReadOnly()) {
      taskEdited = true;
    }

    if (getAttribute().getValue() != null && getAttribute().getValue().length() > 0) {
      YouTrackClient client =
          YouTrackRepositoryConnector.getClient(getTaskEditorPage().getTaskRepository());
      String newComment = getNewComment(getTaskData());
      if (newComment != null && newComment.length() > 0) {
        client.addComment(getTaskData().getRoot().getAttribute(TaskAttribute.TASK_KEY).getValue(),
            newComment);
        getTaskData().getRoot().getMappedAttribute(YouTrackTaskDataHandler.COMMENT_NEW)
            .clearValues();
      }
      YouTrackTaskEditorPageFactory.synchronizeTaskUi(getTaskEditorPage().getEditor());
    }
  }

  private String getNewComment(TaskData taskData) {
    String newComment = "";
    TaskAttribute attribute =
        taskData.getRoot().getMappedAttribute(YouTrackTaskDataHandler.COMMENT_NEW);
    if (attribute != null) {
      newComment = taskData.getAttributeMapper().getValue(attribute);
    }
    return newComment;
  }

  @Override
  public void appendText(String text) {
    if (getEditor() == null) {
      return;
    }

    getEditor().showEditor();
    StringBuilder strBuilder = new StringBuilder();
    String oldText = getEditor().getViewer().getDocument().get();
    if (strBuilder.length() != 0) {
      strBuilder.append("\n"); //$NON-NLS-1$
    }
    strBuilder.append(oldText);
    strBuilder.append(text);
    getEditor().getViewer().getDocument().set(strBuilder.toString());
    TaskAttribute attribute =
        getTaskData().getRoot().getMappedAttribute(YouTrackTaskDataHandler.COMMENT_NEW);
    if (attribute != null) {
      attribute.setValue(strBuilder.toString());
      getTaskEditorPage().getModel().attributeChanged(attribute);
    }
    getEditor().getViewer().getTextWidget().setCaretOffset(strBuilder.length());
    getEditor().getViewer().getTextWidget().showSelection();
  }

  public void setPartId(String partId) {
    this.partId = partId;
  }

  public String getPartId() {
    return partId;
  }

}
