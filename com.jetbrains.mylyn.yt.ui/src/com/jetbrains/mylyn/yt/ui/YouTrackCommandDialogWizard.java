package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class YouTrackCommandDialogWizard extends WizardDialog {

  public YouTrackCommandDialogWizard(Shell parentShell, IWizard newWizard) {
    super(parentShell, newWizard);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {

    super.createButtonsForButtonBar(parent);

    Button finishButton = getButton(IDialogConstants.FINISH_ID);
    finishButton.setText("Apply");
  }

  @Override
  protected void finishPressed() {
    boolean finish = ((YouTrackCommandWizard) getWizard()).finishPressed();
    if (finish) {
      super.finishPressed();
      YouTrackTaskEditorPageFactory.synchronizeTaskUi(((YouTrackCommandWizard) getWizard())
          .getEditor());
    }
  }
}
