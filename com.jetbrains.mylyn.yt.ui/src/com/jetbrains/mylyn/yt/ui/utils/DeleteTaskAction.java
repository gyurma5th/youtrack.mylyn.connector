package com.jetbrains.mylyn.yt.ui.utils;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.actions.DeleteAction;
import org.eclipse.mylyn.tasks.core.ITask;

public class DeleteTaskAction extends DeleteAction {

  public static final String ID = "org.eclipse.mylyn.editor.actions.delete"; //$NON-NLS-1$

  private final ITask task;

  public DeleteTaskAction(ITask task) {
    Assert.isNotNull(task);
    this.task = task;
    setId(ID);
    setActionDefinitionId(null);
    setToolTipText("Delete task");
    setImageDescriptor(CommonImages.REMOVE);
  }

  @Override
  public IStructuredSelection getStructuredSelection() {
    if (task != null) {
      return new StructuredSelection(task);
    } else {
      return super.getStructuredSelection();
    }
  }
}
