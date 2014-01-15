package com.jetbrains.mylyn.yt.ui.utils;

import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

import com.jetbrains.mylyn.yt.ui.YouTrackTaskEditorPage;

public class SubmitAction extends Action {

  private YouTrackTaskEditorPage page;

  public SubmitAction(YouTrackTaskEditorPage page) {
    this.page = page;
    setToolTipText("Submit (Ctrl + Enter)");
    setImageDescriptor(TasksUiImages.REPOSITORY_SUBMIT);
  }

  @Override
  public void run() {
    page.doSubmit();
  }

}
