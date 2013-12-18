/*******************************************************************************
 * Copyright (c) 2004, 2010 Nathan Hapke and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Nathan Hapke - initial API and implementation Tasktop Technologies - improvements
 * Frank Becker - improvements
 *******************************************************************************/
package com.jetbrains.mylyn.yt.tests;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tests.util.TasksUiTestUtil;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;

/**
 * @author Nathan Hapke
 * @author Rob Elves
 * @author Thomas Ehrnhoefer
 * @author Steffen Pingel
 * @author Frank Becker
 * @author Alexander Marchuk
 */

public class YouTrackRepositoryConnectorStandaloneTest extends TestCase {

  private TaskRepository repository;

  private YouTrackConnector connector;

  private YouTrackClient client;

  @Override
  public void setUp() throws Exception {
    repository =
        new TaskRepository(YouTrackCorePlugin.CONNECTOR_KIND, YouTrackTestConstants.REPOSITORY_URL);
    UserCredentials credentials =
        new UserCredentials(YouTrackTestConstants.REAL_USER_ID,
            YouTrackTestConstants.REAL_USER_PASSWORD);
    repository.setCredentials(AuthenticationType.REPOSITORY, new AuthenticationCredentials(
        credentials.getUserName(), credentials.getPassword()), false);
    TasksUiPlugin.getRepositoryManager().addRepository(repository);
    TasksUiTestUtil.ensureTasksUiInitialization();

    connector = (YouTrackConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
    client = YouTrackConnector.getClient(repository);
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

    YouTrackTaskDataHandler taskDataHandler =
        (YouTrackTaskDataHandler) connector.getTaskDataHandler();
    TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
    TaskData taskData =
        new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
    taskDataHandler.initializeTaskData(repository, taskData, initializationData, null);
    for (String attributeKey : additionalAttributeValues.keySet()) {
      taskData.getRoot().createMappedAttribute(attributeKey)
          .setValue(additionalAttributeValues.get(attributeKey));
    }
    YouTrackIssue issue = taskDataHandler.buildIssue(repository, taskData);
    String id = client.putNewIssue(issue);
    return taskDataHandler.parseIssue(repository, client.getIssue(id), null);
  }

  public void testGetTaskData() throws Exception {
    TaskData taskData = createTask(null, null);
    Set<String> taskIds = new HashSet<String>();
    taskIds.add(taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue() + "-"
        + taskData.getTaskId());
    final Set<TaskData> results = new HashSet<TaskData>();
    TaskDataCollector collector = new TaskDataCollector() {
      @Override
      public void accept(TaskData taskData) {
        results.add(taskData);
      }
    };
    connector.getTaskDataHandler().getMultiTaskData(repository, taskIds, collector,
        new NullProgressMonitor());
    assertEquals(1, results.size());
    TaskData updatedTaskData = results.iterator().next();
    String taskId = updatedTaskData.getTaskId();
    assertEquals(taskId, updatedTaskData.getTaskId());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue(), updatedTaskData
        .getRoot().getAttribute(TaskAttribute.SUMMARY).getValue());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue());
  }

  public void testGetMultiTaskData() throws Exception {
    TaskData taskData = createTask(null, null);
    TaskData taskData2 = createTask(null, null);
    TaskData taskData3 = createTask(null, null);
    Set<String> taskIds = new HashSet<String>();
    taskIds.add(taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue() + "-"
        + taskData.getTaskId());
    taskIds.add(taskData2.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue() + "-"
        + taskData2.getTaskId());
    taskIds.add(taskData3.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue() + "-"
        + taskData3.getTaskId());
    final Map<String, TaskData> results = new HashMap<String, TaskData>();
    TaskDataCollector collector = new TaskDataCollector() {
      @Override
      public void accept(TaskData taskData) {
        results.put(taskData.getTaskId(), taskData);
      }
    };
    connector.getTaskDataHandler().getMultiTaskData(repository, taskIds, collector,
        new NullProgressMonitor());
    assertEquals(3, results.size());

    TaskData updatedTaskData = results.get(taskData.getTaskId());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue(), updatedTaskData
        .getRoot().getAttribute(TaskAttribute.SUMMARY).getValue());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue());
    assertEquals(taskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue());


    updatedTaskData = results.get(taskData2.getTaskId());
    assertEquals(taskData2.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue());
    assertEquals(taskData2.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue());
    assertEquals(taskData2.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue());
    assertEquals(taskData2.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue());

    updatedTaskData = results.get(taskData3.getTaskId());
    assertEquals(taskData3.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue());
    assertEquals(taskData3.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION).getValue());
    assertEquals(taskData3.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.DATE_CREATION).getValue());
    assertEquals(taskData3.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue(),
        updatedTaskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER).getValue());
  }

  public void testPerformQuery() throws Exception {
    final String summaryNotNull = "Summary for testPerformQuery " + new Date();
    final String descriptionNotNull = "Description for testPerformQuery " + new Date();
    TaskData taskData = createTask(new HashMap<String, String>() {
      {
        put(TaskAttribute.SUMMARY, summaryNotNull);
        put(TaskAttribute.DESCRIPTION, descriptionNotNull);
      }
    });

    // run query
    RepositoryQuery query =
        new RepositoryQuery(repository.getConnectorKind(), "handle-testQueryViaConnector");
    query.setAttribute(YouTrackCorePlugin.QUERY_KEY_FILTER, "project: "
        + taskData.getRoot().getAttribute(TaskAttribute.PRODUCT).getValue() + " summary: \""
        + summaryNotNull + "\"");
    final Map<String, TaskData> changedTaskData = new HashMap<String, TaskData>();
    TaskDataCollector collector = new TaskDataCollector() {
      @Override
      public void accept(TaskData taskData) {
        changedTaskData.put(taskData.getTaskId(), taskData);
      }
    };
    connector.performQuery(repository, query, collector, null, new NullProgressMonitor());
    assertEquals(1, changedTaskData.size());

    taskData.getRoot().getMappedAttribute("Priority")
        .setValue(YouTrackTestConstants.MINOR_PRIORUTY);
    taskData.getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION).setValue(descriptionNotNull);

    YouTrackTaskDataHandler taskDataHandler =
        (YouTrackTaskDataHandler) connector.getTaskDataHandler();
    YouTrackIssue issue = taskDataHandler.buildIssue(repository, taskData);
    String id = client.putNewIssue(issue);
    TaskData taskDataNew = taskDataHandler.parseIssue(repository, client.getIssue(id), null);

    // run query again
    final Map<String, TaskData> changedTaskData2 = new HashMap<String, TaskData>();
    TaskDataCollector collector2 = new TaskDataCollector() {
      @Override
      public void accept(TaskData taskData) {
        changedTaskData2.put(taskData.getTaskId(), taskData);
      }
    };
    connector.performQuery(repository, query, collector2, null, new NullProgressMonitor());
    assertEquals(2, changedTaskData2.size());

    // compare query results
    changedTaskData2.keySet().removeAll(changedTaskData.keySet());
    assertEquals(1, changedTaskData2.size());
    taskData = changedTaskData2.get(taskDataNew.getTaskId());
    assertNotNull(taskData);
    assertTrue(taskData.getRoot().getAttribute(TaskAttribute.PRIORITY).getValue()
        .equals(taskDataNew.getRoot().getAttribute(TaskAttribute.PRIORITY).getValue()));
  }
}
