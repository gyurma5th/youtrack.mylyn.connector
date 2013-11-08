/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import java.util.LinkedList;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;

public class NewYouTrackIssueWizard extends NewTaskWizard implements INewWizard {

  private NewIssueProjectPage page;

  class NewIssueProjectPage extends WizardPage {

    protected NewIssueProjectPage(String pageName) {
      super(pageName);
      setPageComplete(false);
    }

    public Combo projectCombo;

    @Override
    public void createControl(Composite parent) {
      setTitle("Select project name from repository");
      Composite composite = new Composite(parent, SWT.BORDER);
      composite.setLayout(new GridLayout(1, false));

      Label label = new Label(composite, SWT.NONE);
      label.setText("Select project:");
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);

      projectCombo = new Combo(composite, SWT.NONE);
      GridData gd = new GridData(SWT.FILL);
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      getProjectCombo().setLayoutData(gd);

      setControl(composite);

      LinkedList<YouTrackProject> projects;
      projects =
          (LinkedList<YouTrackProject>) YouTrackConnector.getClient(getTaskRepository())
              .getProjects();
      if (getProjectCombo() != null) {
        for (YouTrackProject project : projects) {
          getProjectCombo().add(project.getBothNames());
        }
        if (projects != null && projects.size() > 0) {
          getProjectCombo().setText(projects.get(0).getBothNames());
        }
      }

      projectCombo.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          setPageComplete(getProjectCombo().getSelectionIndex() != -1);
        }
      });
    }

    public Combo getProjectCombo() {
      return projectCombo;
    }
  }

  public NewYouTrackIssueWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
    super(taskRepository, taskSelection);
    setWindowTitle("New YouTrack Issue");
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {}

  @Override
  public void addPages() {
    page = new NewIssueProjectPage("Select project name from repository");
    addPage(page);
  }

  @Override
  protected ITaskMapping getInitializationData() {

    final String projectName =
        (page.getProjectCombo().getSelectionIndex() != -1) ? page.getProjectCombo().getText() : "";

    return new TaskMapping() {
      @Override
      public String getProduct() {
        return YouTrackProject.getShortNameFromBoth(projectName);
      }
    };
  }

}
