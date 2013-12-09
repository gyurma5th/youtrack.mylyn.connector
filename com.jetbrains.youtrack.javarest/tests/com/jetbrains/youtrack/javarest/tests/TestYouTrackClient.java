package com.jetbrains.youtrack.javarest.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackClientFactory;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;

public class TestYouTrackClient {

  private static YouTrackClient client;

  static final String TEST_PROJECT_NAME = "testproject";

  static final String MYLYN_PROJECT_NAME = "mylyn";

  private static YouTrackClientFactory clientFactory;

  @BeforeClass
  public static void initialize() {
    clientFactory = new YouTrackClientFactory();
    client = clientFactory.getClient("http://nylym.myjetbrains.com/youtrack/");
    try {
      client.login("testuser", "12345");
    } catch (Exception e) {
      fail("Can't login into YT\n" + e.getMessage());
    }
  }

  @Test
  public void testHandleCookies() {
    int issues_count = 100;
    // client.getNumberOfIssues("project: " + TEST_PROJECT_NAME);
    List<YouTrackIssue> issues = client.getIssuesInProject(TEST_PROJECT_NAME, issues_count);
    assertEquals(issues_count, issues.size());
    try {
      for (YouTrackIssue issue : issues) {
        client.getIssue(issue.getId());
      }
    } catch (RuntimeException e) {
      fail("Cant get issues.");
    }

  }
}
