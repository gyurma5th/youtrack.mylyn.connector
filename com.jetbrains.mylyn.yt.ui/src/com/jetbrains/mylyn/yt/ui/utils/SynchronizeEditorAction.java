package com.jetbrains.mylyn.yt.ui.utils;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.LocalTask;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

import com.jetbrains.mylyn.yt.ui.YouTrackTaskEditorPageFactory;

public class SynchronizeEditorAction extends BaseSelectionListenerAction {

  public static final String ID = "org.eclipse.mylyn.tasklist.actions.synchronize.editor"; //$NON-NLS-1$

  private static final String tooltipText = "Synchronize Incoming changes (F5)";

  public SynchronizeEditorAction() {
    super("Synchronize");
    setToolTipText(tooltipText);
    setId(ID);
    setImageDescriptor(CommonImages.REFRESH);
  }

  @Override
  public void run() {
    IStructuredSelection selection = getStructuredSelection();
    if (selection == null) {
      return;
    }
    Object selectedObject = selection.getFirstElement();
    if (!(selectedObject instanceof TaskEditor)) {
      return;
    }

    final TaskEditor editor = (TaskEditor) selectedObject;
    YouTrackTaskEditorPageFactory.synchronizeTaskUi(editor);
  }

  @Override
  protected boolean updateSelection(IStructuredSelection selection) {
    Object selectedObject = selection.getFirstElement();
    if (selectedObject instanceof TaskEditor) {
      TaskEditor editor = (TaskEditor) selectedObject;
      ITask task = editor.getTaskEditorInput().getTask();
      return !(task instanceof LocalTask);
    }
    return false;
  }
}
