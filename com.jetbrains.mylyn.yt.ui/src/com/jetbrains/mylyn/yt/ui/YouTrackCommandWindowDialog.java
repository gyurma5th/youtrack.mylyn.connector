package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class YouTrackCommandWindowDialog extends WizardDialog {

  public YouTrackCommandWindowDialog(Shell parentShell, IWizard newWizard) {
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
    boolean finish = ((YouTrackCommandWindowWizard) getWizard()).finishPressed();
    if (finish) {
      super.finishPressed();
      YouTrackTaskEditorPageFactory.synchronizeTaskUi(((YouTrackCommandWindowWizard) getWizard())
          .getEditor());
    }
  }
}
