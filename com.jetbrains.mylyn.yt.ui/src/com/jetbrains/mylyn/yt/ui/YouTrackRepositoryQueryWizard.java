package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

public class YouTrackRepositoryQueryWizard extends RepositoryQueryWizard {

  private TaskRepository repository;

  public YouTrackRepositoryQueryWizard(TaskRepository repository) {
    super(repository);
    this.repository = repository;
  }

  @Override
  public boolean performFinish() {


    boolean confirm =
        MessageDialog.openConfirm(null, "Confirm query", "Revert all changes?" + "\n\n");
    if (confirm) {
      return super.performFinish();
    } else {
      return false;
    }
  }

}
