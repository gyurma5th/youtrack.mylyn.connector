package com.jetbrains.mylyn.yt.ui.utils;

import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.ui.CommonImages;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;
import com.jetbrains.mylyn.yt.ui.YouTrackTaskEditorPage;

public class EditAction extends Action {

  private YouTrackTaskEditorPage page;

  public EditAction(YouTrackTaskEditorPage page) {
    this.page = page;
    setToolTipText("Edit");
    setImageDescriptor(CommonImages.EDIT);
    if (page.getModel().getTaskData().isNew()) {
      setEnabled(false);
    }
  }

  @Override
  public void run() {
    if (!YouTrackTaskDataHandler.isEnableEditMode()) {
      page.doEdit();
    }
  }

}
