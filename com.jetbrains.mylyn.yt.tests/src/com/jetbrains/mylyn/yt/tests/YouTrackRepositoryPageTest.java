package com.jetbrains.mylyn.yt.tests;

import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TasksUiTestUtil;
import org.eclipse.ui.PlatformUI;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.ui.YouTrackRepositoryPage;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;

/**
 * @author Rob Elves
 */
public class YouTrackRepositoryPageTest extends TestCase {

  private TaskRepositoryManager manager;

  private TaskRepository repository;

  private static final String repositoryUrl = "http://nylym.myjetbrains.com/youtrack/";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    manager = TasksUiPlugin.getRepositoryManager();
    manager.clearRepositories(TasksUiPlugin.getDefault().getRepositoriesFilePath());
    repository = new TaskRepository(YouTrackCorePlugin.CONNECTOR_KIND, repositoryUrl);
    UserCredentials credentials = CommonTestUtil.getCredentials(PrivilegeLevel.USER);
    repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(
        credentials.getUserName(), credentials.getPassword()), false);
    TasksUiPlugin.getRepositoryManager().addRepository(repository);
    TasksUiTestUtil.ensureTasksUiInitialization();
  }

  private YouTrackClient createClient(String hostUrl, String username, String password,
      String htAuthUser, String htAuthPass, String encoding) throws MalformedURLException {
    TaskRepository taskRepository = new TaskRepository(YouTrackCorePlugin.CONNECTOR_KIND, hostUrl);

    AuthenticationCredentials credentials = new AuthenticationCredentials(username, password);
    taskRepository.setCredentials(AuthenticationType.REPOSITORY, credentials, false);

    AuthenticationCredentials webCredentials =
        new AuthenticationCredentials(htAuthUser, htAuthPass);
    taskRepository.setCredentials(AuthenticationType.HTTP, webCredentials, false);
    taskRepository.setCharacterEncoding(encoding);

    YouTrackConnector connector =
        (YouTrackConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
    return YouTrackConnector.getClient(repository);
  }

  public void testLoginInvalidPassword() throws Exception {

    EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
    WizardDialog dialog =
        new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
    dialog.create();
    YouTrackRepositoryPage page = (YouTrackRepositoryPage) wizard.getSettingsPage();
    page.setPassword("bogus");
    try {
      YouTrackClient client =
          createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
              page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
      client.login(page.getUserName(), page.getPassword());
    } catch (RuntimeException e) {
      return;
    }
    fail("LoginException didn't occur!");
  }

}
