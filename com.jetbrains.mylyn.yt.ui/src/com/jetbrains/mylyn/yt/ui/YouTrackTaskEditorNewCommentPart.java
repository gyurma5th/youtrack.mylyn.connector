package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.ToolBarButtonContribution;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackTaskEditorNewCommentPart extends TaskEditorRichTextPart {

  private String partId;

  public YouTrackTaskEditorNewCommentPart() {
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
    ToolBarButtonContribution submitButtonContribution =
        new ToolBarButtonContribution("org.eclipse.mylyn.tasks.toolbars.submit") { //$NON-NLS-1$
          @Override
          protected Control createButton(Composite composite) {
            Button submitButton = new Button(composite, SWT.FLAT);
            submitButton.setText("Submit" + " "); //$NON-NLS-1$
            submitButton.setImage(CommonImages.getImage(TasksUiImages.REPOSITORY_SUBMIT));
            submitButton.setBackground(null);
            submitButton.addListener(SWT.Selection, new Listener() {
              public void handleEvent(Event e) {
                doSubmit();
              }
            });
            return submitButton;
          }
        };
    submitButtonContribution.marginLeft = 10;
    manager.add(submitButtonContribution);
  }

  private void doSubmit() {
    YouTrackTaskDataHandler.setPostNewCommentMode(true);
    getTaskEditorPage().doSubmit();
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
