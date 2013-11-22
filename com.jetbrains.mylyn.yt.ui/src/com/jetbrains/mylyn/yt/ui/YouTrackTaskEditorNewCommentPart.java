package com.jetbrains.mylyn.yt.ui;

import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackTaskEditorNewCommentPart extends TaskEditorRichTextPart {

  public YouTrackTaskEditorNewCommentPart() {
    setPartName(Messages.TaskEditorNewCommentPart_New_Comment);
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
}
