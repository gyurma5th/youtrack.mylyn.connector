package com.jetbrains.mylyn.yt.ui.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.widgets.Display;

import com.jetbrains.mylyn.yt.core.YouTrackRepositoryConnector;
import com.jetbrains.mylyn.yt.ui.YouTrackTaskEditorPage;

public class UpdateProjectAction extends Action {

  private YouTrackTaskEditorPage page;

  public UpdateProjectAction(YouTrackTaskEditorPage page) {
    this.page = page;
    setToolTipText("Update project settings");
    setImageDescriptor(TasksUiImages.REPOSITORY_UPDATE_CONFIGURATION);
  }

  @Override
  public void run() {
    final String projectname =
        page.getModel().getTaskData().getRoot().getMappedAttribute(TaskAttribute.PRODUCT)
            .getValue();
    Job job = new Job("Update settings of " + projectname) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        YouTrackRepositoryConnector.forceUpdateProjectCustomFields(page.getTaskRepository(),
            projectname);
        syncWithUi();
        return Status.OK_STATUS;
      }
    };
    job.setUser(true);
    job.schedule();
  }

  private void syncWithUi() {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (!page.getModel().getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY)
            .getMetaData().isReadOnly()) {
          page.doEdit();
        }
      }
    });
  }
}
