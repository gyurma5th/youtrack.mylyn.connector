/*******************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.jetbrains.mylyn.yt.tests;

import java.net.MalformedURLException;

import junit.framework.TestCase;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.internal.tasks.core.TaskRepositoryManager;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.wizards.EditRepositoryWizard;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TasksUiTestUtil;
import org.eclipse.ui.PlatformUI;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackRepositoryConnector;
import com.jetbrains.mylyn.yt.ui.YouTrackRepositoryPage;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;

/**
 * @author Rob Elves
 * @author Alexander Marchuk
 */
public class YouTrackRepositoryPageTest extends TestCase {

  private TaskRepository repository;

  private TaskRepositoryManager manager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TasksUiPlugin.getRepositoryManager().clearRepositories(
        TasksUiPlugin.getDefault().getRepositoriesFilePath());
    repository =
        new TaskRepository(YouTrackCorePlugin.CONNECTOR_KIND, YouTrackTestConstants.REPOSITORY_URL);
    UserCredentials credentials =
        new UserCredentials(YouTrackTestConstants.REAL_USER_ID,
            YouTrackTestConstants.REAL_USER_PASSWORD);
    repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(
        credentials.getUserName(), credentials.getPassword()), false);
    TasksUiPlugin.getRepositoryManager().addRepository(repository);
    TasksUiTestUtil.ensureTasksUiInitialization();
  }

  private EditRepositoryWizard createEditRepositoryWizard() {
    EditRepositoryWizard wizard = new EditRepositoryWizard(repository);
    WizardDialog dialog =
        new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
    dialog.create();
    return wizard;
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

    YouTrackRepositoryConnector connector =
        (YouTrackRepositoryConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
    if (repository.getRepositoryUrl().equals(hostUrl)) {
      return YouTrackRepositoryConnector.getClient(repository);
    } else {
      return YouTrackRepositoryConnector.getClient(new TaskRepository(
          repository.getConnectorKind(), hostUrl));
    }
  }


  public void testLoginInvalidPassword() throws Exception {
    YouTrackRepositoryPage page =
        (YouTrackRepositoryPage) createEditRepositoryWizard().getSettingsPage();
    page.setPassword("bogus");
    try {
      YouTrackClient client =
          createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
              page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
      client.login(page.getUserName(), page.getPassword());
    } catch (RuntimeException e) {
      return;
    }
    fail("Exception in login didn't occur!");
  }

  public void testValidationInvalidUserid() throws Exception {
    YouTrackRepositoryPage page =
        (YouTrackRepositoryPage) createEditRepositoryWizard().getSettingsPage();
    page.setUserId("bogus");
    try {
      YouTrackClient client =
          createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
              page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
      client.login(page.getUserName(), page.getPassword());
    } catch (RuntimeException e) {
      return;
    }
    fail("Exception in login didn't occur!");
  }

  public void testLoginWithValidCredentials() throws Exception {
    YouTrackRepositoryPage page =
        (YouTrackRepositoryPage) createEditRepositoryWizard().getSettingsPage();
    try {
      YouTrackClient client =
          createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
              page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
      if (client.login(page.getUserName(), page.getPassword())) {
        return;
      } else {
        fail("Can't login with correct credentials (and exception didn't occur!)");
      }
    } catch (RuntimeException e) {
      fail("Exception while login with correct credentials!");
    }
  }

  public void testValidationInvalidUrl() throws Exception {
    YouTrackRepositoryPage page =
        (YouTrackRepositoryPage) createEditRepositoryWizard().getSettingsPage();
    page.setUrl("http://nylym.myjetbrains.com");
    try {
      YouTrackClient client =
          createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
              page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
      client.login(page.getUserName(), page.getPassword());
      fail("Exception in login didn't occur with wrong repository url!");
    } catch (RuntimeException e) {}
  }

  public void testPersistChangeOfUrl() throws Exception {
    assertEquals(1, TasksUiPlugin.getRepositoryManager().getAllRepositories().size());
    String tempUid = repository.getUserName();
    String tempPass = repository.getPassword();
    EditRepositoryWizard wizard = createEditRepositoryWizard();
    YouTrackRepositoryPage page = (YouTrackRepositoryPage) wizard.getSettingsPage();
    YouTrackClient client =
        createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
            page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
    client.login(page.getUserName(), page.getPassword());
    wizard.performFinish();
    assertEquals(1, TasksUiPlugin.getRepositoryManager().getAllRepositories().size());
    TaskRepository repositoryTest =
        TasksUiPlugin.getRepositoryManager().getRepository(YouTrackCorePlugin.CONNECTOR_KIND,
            YouTrackTestConstants.REPOSITORY_URL);
    assertNotNull(repositoryTest);
    assertEquals(tempUid, repositoryTest.getUserName());
    assertEquals(tempPass, repositoryTest.getPassword());
  }

  public void testValidateOnFinishInvalidUserId() throws Exception {
    assertEquals(1, TasksUiPlugin.getRepositoryManager().getAllRepositories().size());
    EditRepositoryWizard wizard = createEditRepositoryWizard();
    YouTrackRepositoryPage page = (YouTrackRepositoryPage) wizard.getSettingsPage();
    YouTrackClient client =
        createClient(page.getRepositoryUrl(), page.getUserName(), page.getPassword(),
            page.getHttpAuthUserId(), page.getHttpAuthPassword(), page.getCharacterEncoding());
    client.login(page.getUserName(), page.getPassword());
    String oldUserId = page.getUserName();
    page.setUserId("bogus");
    boolean finished = wizard.performFinish();
    assertFalse(finished);
    assertEquals(1, TasksUiPlugin.getRepositoryManager().getAllRepositories().size());
    TaskRepository repositoryTest =
        TasksUiPlugin.getRepositoryManager().getRepository(YouTrackCorePlugin.CONNECTOR_KIND,
            YouTrackTestConstants.REPOSITORY_URL);
    assertEquals(oldUserId, repositoryTest.getCredentials(AuthenticationType.REPOSITORY)
        .getUserName());
  }

}
