package com.jetbrains.mylyn.yt.tests;

/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Tasktop Technologies - initial API and implementation
 *******************************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.commons.sdk.util.TestConfiguration;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TasksUiTestUtil;
import org.eclipse.mylyn.tests.util.TestFixture;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackRepositoryConnector;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;


/**
 * @author Steffen Pingel
 */
public class YouTrackFixture extends TestFixture {

  private static YouTrackFixture current;

  private YouTrackRepositoryConnector connector;

  private TaskRepository repository;

  private YouTrackClient client;

  private String version;

  private YouTrackTaskDataHandler dataHandler;

  public static YouTrackFixture DEFAULT = new YouTrackFixture(YouTrackTestConstants.REPOSITORY_URL,
      YouTrackTestConstants.LAST_MAJOR_VERSION, "youtrack");

  public YouTrackFixture(String url, String version, String info) {
    super(YouTrackCorePlugin.CONNECTOR_KIND, url);
    this.version = version;
    setInfo("YouTrack", version, info);

    repository =
        new TaskRepository(YouTrackCorePlugin.CONNECTOR_KIND, YouTrackTestConstants.REPOSITORY_URL);
    UserCredentials credentials =
        new UserCredentials(YouTrackTestConstants.REAL_USER_ID,
            YouTrackTestConstants.REAL_USER_PASSWORD);
    repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(
        credentials.getUserName(), credentials.getPassword()), false);
    TasksUiPlugin.getRepositoryManager().addRepository(repository);
    TasksUiTestUtil.ensureTasksUiInitialization();
    connector =
        (YouTrackRepositoryConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
    client = YouTrackRepositoryConnector.getClient(repository);
    dataHandler = (YouTrackTaskDataHandler) connector.getTaskDataHandler();
  }

  public static YouTrackFixture current() {
    return current(DEFAULT);
  }

  public static YouTrackFixture current(YouTrackFixture fixture) {
    if (current == null) {
      fixture.activate();
    }
    return current;
  }

  @Override
  public YouTrackFixture activate() {
    current = this;
    setUpFramework();
    return this;
  }

  @Override
  protected TestFixture getDefault() {
    return TestConfiguration.getDefault().discoverDefault(YouTrackFixture.class, "youtrack");
  }

  public String getVersion() {
    return version;
  }

  public TaskData createTask(String summary, String description) throws Exception {
    final String summaryNotNull = summary != null ? summary : "summary";
    final String descriptionNotNull = description != null ? description : "description";
    return createTask(new HashMap<String, String>() {
      private static final long serialVersionUID = 1L;
      {
        put(TaskAttribute.SUMMARY, summaryNotNull);
        put(TaskAttribute.DESCRIPTION, descriptionNotNull);
      }
    });
  }

  public TaskData createTask(Map<String, String> additionalAttributeValues) throws Exception {
    Assert.isLegal(additionalAttributeValues.containsKey(TaskAttribute.SUMMARY),
        "need value for Summary");
    Assert.isLegal(additionalAttributeValues.containsKey(TaskAttribute.DESCRIPTION),
        "need value for Description");
    ITaskMapping initializationData = new TaskMapping() {

      @Override
      public String getProduct() {
        return YouTrackTestConstants.TEST_PROJECT_NAME;
      }
    };

    TaskAttributeMapper mapper = dataHandler.getAttributeMapper(repository);
    TaskData taskData =
        new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
    dataHandler.initializeTaskData(repository, taskData, initializationData, null);
    for (String attributeKey : additionalAttributeValues.keySet()) {
      taskData.getRoot().createMappedAttribute(attributeKey)
          .setValue(additionalAttributeValues.get(attributeKey));
    }
    YouTrackIssue issue = dataHandler.buildIssue(repository, taskData);
    String id = client.putNewIssue(issue);
    return getTask(id, client);
  }

  public TaskData getTask(String id, YouTrackClient client) throws Exception {
    return dataHandler.parseIssue(repository, client.getIssue(id), null);
  }

  @Override
  public YouTrackRepositoryConnector connector() {
    return connector;
  }

  @Override
  public TaskRepository repository() {
    return repository;
  }

  public YouTrackClient client() {
    return client;
  }
}
