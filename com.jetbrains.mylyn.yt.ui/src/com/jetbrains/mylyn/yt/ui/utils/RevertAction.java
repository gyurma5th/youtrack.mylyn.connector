package com.jetbrains.mylyn.yt.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITask.SynchronizationState;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class RevertAction extends Action {

  public static final String ID = "org.eclipse.mylyn.tasklist.actions.mark.discard"; //$NON-NLS-1$

  private final List<IRepositoryElement> selectedElements;

  private AbstractTaskEditorPage taskEditorPage;

  public RevertAction(List<IRepositoryElement> selectedElements) {
    this.selectedElements = selectedElements;
    setToolTipText("Revert all changes");
    setImageDescriptor(CommonImages.UNDO);
    setId(ID);
    if (selectedElements.size() == 1 && (selectedElements.get(0) instanceof ITask)) {
      ITask task = (ITask) selectedElements.get(0);
    } else {
      setEnabled(false);
    }
  }

  public AbstractTaskEditorPage getTaskEditorPage() {
    return taskEditorPage;
  }

  public void setTaskEditorPage(AbstractTaskEditorPage taskEditorPage) {
    this.taskEditorPage = taskEditorPage;
  }

  public static boolean hasOutgoingChanges(ITask task) {
    return task.getSynchronizationState().equals(SynchronizationState.OUTGOING)
        || task.getSynchronizationState().equals(SynchronizationState.CONFLICT);
  }

  @Override
  public void run() {

    getTaskEditorPage().getModel().revert();

    if (getTaskEditorPage().getModel().getChangedAttributes().size() > 0) {
      ArrayList<AbstractTask> toClear = new ArrayList<AbstractTask>();
      for (Object selectedObject : selectedElements) {
        if (selectedObject instanceof ITask && hasOutgoingChanges((ITask) selectedObject)) {
          toClear.add(((AbstractTask) selectedObject));
        }
      }
      if (toClear.size() > 0) {
        AbstractTask task = toClear.get(0);
        boolean confirm =
            MessageDialog.openConfirm(null, "Confirm revert", "Revert all changes?" + "\n\n" //$NON-NLS-1$
                + task.getSummary());
        if (confirm) {
          if (taskEditorPage != null) {
            taskEditorPage.doSave(null);
          }
          try {
            YouTrackTaskDataHandler.setEnableEditMode(false);
            TasksUi.getTaskDataManager().discardEdits(task);
          } catch (CoreException e) {
            TasksUiInternal.displayStatus("Clear outgoing failed", e.getStatus());
          }
        } else {
          return;
        }
      }
    }
    getTaskEditorPage().getEditor().refreshPages();
  }
}
