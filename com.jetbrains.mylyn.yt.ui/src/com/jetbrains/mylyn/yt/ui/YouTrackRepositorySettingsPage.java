/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackRepositoryConnector;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;

public class YouTrackRepositorySettingsPage extends AbstractRepositorySettingsPage {

  private static final String PAGE_TITLE = "YouTrack Repository Settings";

  private static final String TRACKER_URL_HINT =
      "Specify YouTrack host (e.g. 'http://me.myjetbrains.com/youtrack/')";

  private static final String REPO_ALREADY_EXISTS =
      "A repository with this name already exists, please choose another name.";

  private static final String ENTER_VALID_USER_AND_PASSWORD =
      "Enter a user login and password (JetBrains Account not supported).";

  private static final String DEFAULT_MESSAGE = "";

  public YouTrackRepositorySettingsPage(TaskRepository taskRepository) {
    super(PAGE_TITLE, DEFAULT_MESSAGE, taskRepository);
    setNeedsAnonymousLogin(true);
    setNeedsAdvanced(false);
    setNeedsEncoding(false);
    setNeedsHttpAuth(false);
    setNeedsProxy(false);
    setNeedsValidateOnFinish(true);
    setNeedsTimeZone(false);
  }

  @Override
  public void applyTo(TaskRepository repository) {
    super.applyTo(repository);
    String location = getLocation(repository).getAbsolutePath();
    repository.setProperty(YouTrackCorePlugin.REPOSITORY_KEY_PATH, location);
  }

  private File getLocation(TaskRepository repository) {
    File root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
    return new File(root, repository.getRepositoryUrl());
  }

  @Override
  protected void createAdditionalControls(Composite parent) {}

  @Override
  public String getConnectorKind() {
    return YouTrackCorePlugin.CONNECTOR_KIND;
  }

  @Override
  protected Validator getValidator(final TaskRepository repository) {
    return new Validator() {
      @Override
      public void run(IProgressMonitor monitor) throws CoreException {
        String location = repository.getRepositoryUrl();
        String user = repository.getUserName();
        String passwd = repository.getPassword();
        try {
          YouTrackClient client = YouTrackRepositoryConnector.getClient(repository);
          if (!client.login(user, passwd)) {
            throw new CoreException(new Status(IStatus.ERROR, YouTrackUiPlugin.ID_PLUGIN,
                "Credentials are not valid"));
          }
        } catch (Exception e) {
          throw new CoreException(new Status(IStatus.ERROR, YouTrackUiPlugin.ID_PLUGIN, NLS.bind(
              "Host ''{0}'' or credentials are not valid for YT host", location)));
        }
      }
    };
  }

  @Override
  protected void repositoryTemplateSelected(RepositoryTemplate template) {
    repositoryLabelEditor.setStringValue(template.label);
    setUrl(template.repositoryUrl);
    setAnonymous(template.anonymous);
    getContainer().updateButtons();
  }

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    addRepositoryTemplatesToServerUrlCombo();
    setAnonymous(false);

    for (Control control : parent.getChildren()) {
      if (control instanceof Label) {
        Label label = (Label) control;
        if (label.getText().equals(LABEL_SERVER)) {
          label.setText("Tracker URL:");
          return;
        }
      } else if (control instanceof Composite) {
        Composite composite = (Composite) control;
        for (Control innerControl : composite.getChildren()) {
          if (innerControl instanceof Composite) {
            Composite composite2 = (Composite) innerControl;
            for (Control innerControl2 : composite2.getChildren()) {
              if (innerControl2 instanceof Button) {
                // always save password and make repository online
                Button button = (Button) innerControl2;
                if (button.getText().equals("Save Password")) {
                  button.setSelection(true);
                } else if (button.getText().equals("Validate on Finish")) {
                  button.setSelection(true);
                }
                button.setEnabled(false);
                button.setVisible(false);
              }
            }
          }
        }
      }
    }

    setMessage(TRACKER_URL_HINT, IMessageProvider.NONE);
  }

  @Override
  protected void createSettingControls(Composite parent) {
    super.createSettingControls(parent);

    // Search for the server label and override the text value
    for (Control control : parent.getChildren()) {
      if (control instanceof Label) {
        Label label = (Label) control;
        if (label.getText().equals(LABEL_SERVER)) {
          label.setText("Tracker URL:");
          return;
        }
      }
    }
  }

  @Override
  protected void createContributionControls(Composite parentControl) {
    // disable contributions to suppress the Task Editor Settings section
  }

  @Override
  public boolean isPageComplete() {
    String errorMessage = null;
    String url = getRepositoryUrl();
    // check for errors
    errorMessage = isUniqueUrl(url);
    if (errorMessage == null) {
      for (TaskRepository repository : TasksUi.getRepositoryManager().getAllRepositories()) {
        if (!repository.equals(getRepository())
            && getRepositoryLabel().equals(repository.getRepositoryLabel())) {
          errorMessage = REPO_ALREADY_EXISTS;
          break;
        }
      }
    }
    if (errorMessage == null) {
      // check for messages
      if (!isValidUrl(url)) {
        errorMessage = TRACKER_URL_HINT;
      }
      if (errorMessage == null && (!needsAnonymousLogin() || !anonymousButton.getSelection())
          && isMissingCredentials()) {
        errorMessage = ENTER_VALID_USER_AND_PASSWORD;
      }
      setMessage(errorMessage, repository == null ? IMessageProvider.NONE : IMessageProvider.ERROR);
    } else {
      setMessage(errorMessage, IMessageProvider.ERROR);
    }
    return errorMessage == null && super.isPageComplete();
  }
}
