package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.ToolBarButtonContribution;
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
}
