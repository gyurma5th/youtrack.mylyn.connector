/**
 * @author: amarch
 */

package com.jetbrains.youtrack.javarest.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackClientFactory;
import com.jetbrains.youtrack.javarest.client.YouTrackCustomField;
import com.jetbrains.youtrack.javarest.client.YouTrackIssue;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;
import com.jetbrains.youtrack.javarest.utils.BundleValues;
import com.jetbrains.youtrack.javarest.utils.IntellisenseItem;
import com.jetbrains.youtrack.javarest.utils.SavedSearch;
import com.jetbrains.youtrack.javarest.utils.UserSavedSearch;

public class TestClientMethods {

  private static YouTrackClient client;

  private static final String TEST_PROJECT_NAME = "autotest";

  private static final String TEST_PROJECT_USER = "tester";

  private static final String TEST_PROJECT_USER_PASSWORD = "12345";

  private static final String MYLYN_PROJECT_NAME = "mylyn";

  private static YouTrackClientFactory clientFactory;

  @BeforeClass
  public static void initialize() {
    clientFactory = new YouTrackClientFactory();
    client = clientFactory.getClient("http://nylym.myjetbrains.com/youtrack/");
    try {
      client.login(TEST_PROJECT_USER, TEST_PROJECT_USER_PASSWORD);
    } catch (Exception e) {
      fail("Can't login into YT\n" + e.getMessage());
    }
  }

  @Test
  public void testHandleCookies() {
    int issues_count = 17;
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

  @Test
  public void testCreateClient() {
    String[] validServerUrls =
        {"http://nylym.myjetbrains.com/youtrack/", "http://youtrack.jetbrains.com/",
            "http://youtrack.jetbrains.com:80/", "http://nylym.myjetbrains.com:80/youtrack/"};
    for (String serverUrl : validServerUrls) {
      clientFactory.getClient(serverUrl);
    }
  }

  @Test
  public void testLogin() {

    YouTrackClient testClient = clientFactory.getClient("http://nylym.myjetbrains.com/youtrack/");

    try {
      assertTrue("Jersey test login method",
          testClient.login(TEST_PROJECT_USER, TEST_PROJECT_USER_PASSWORD));
      assertTrue("Jersey test login method with credentials", testClient.loginWithCredentials());
    } catch (Exception e) {
      fail("Exception while login test:" + e.getMessage());
    }

    try {
      testClient.login(null, null);
      fail("Exception expected");
    } catch (Exception e) {}

    try {
      testClient.setPassword("");
      testClient.loginWithCredentials();
      fail("Exception expected");
    } catch (Exception e) {}

    try {
      testClient.setPassword("pass");
      testClient.loginWithCredentials();
      fail("Exception expected");
    } catch (Exception e) {}
  }

  @Test
  public void testGetIssueById() {

    YouTrackIssue issue = client.getIssue(TEST_PROJECT_NAME + "-3");
    issue.mapFields();
    assertEquals("testGetIssueById$1", issue.getSummary());
    assertEquals("Issue for testGetIssueById", issue.getDescription());
    assertEquals("root", issue.getSingleField("reporterName"));
    assertEquals(TEST_PROJECT_NAME + "-3", issue.getId());
    assertEquals("Fixed", issue.getSingleCustomFieldValue("State"));
    assertEquals("Task", issue.getSingleCustomFieldValue("Type"));
    assertEquals(2, issue.getTags().size());
    try {
      issue = client.getIssue(null);
      fail("Exception expected when issue id null.");
    } catch (Exception e) {}

    try {
      issue = client.getIssue("");
      fail("Exception expected when get issue with incorrect id. ");
    } catch (Exception e) {}

    try {
      issue = client.getIssue("-1");
      fail("Exception expected when get issue with incorrect id. ");
    } catch (Exception e) {}
  }

  @Test
  public void testGetIssuesInProject() {

    List<YouTrackIssue> issues = client.getIssuesInProject(TEST_PROJECT_NAME, 10);
    assertEquals(10, issues.size());
    issues = client.getIssuesInProject(TEST_PROJECT_NAME);
    assertEquals(10, issues.size());
    issues =
        client.getIssuesInProject(TEST_PROJECT_NAME, "project: " + TEST_PROJECT_NAME, 0, 10, 0);
    assertEquals(10, issues.size());
    issues = client.getIssuesInProject(TEST_PROJECT_NAME, "!@#$%^&*", 0, 10, 0);
    assertEquals(0, issues.size());

    issues = client.getIssuesInProject(TEST_PROJECT_NAME, 10);
    YouTrackIssue i = new YouTrackIssue();
    for (YouTrackIssue issue : issues) {
      if (issue.getId().equals(TEST_PROJECT_NAME + "-3")) {
        i = issue;
        break;
      }
    }
    i.mapFields();
    assertEquals("testGetIssueById$1", i.getSummary());

    try {
      issues = client.getIssuesInProject(null);
      fail("Exception while get list of issues expected.");
    } catch (Exception e) {}

    try {
      issues = client.getIssuesInProject("not exist projectname");
      fail("Exception while get list of issues expected.");
    } catch (Exception e) {}

    try {
      issues = client.getIssuesInProject("!@#$%^&*", "", 0, 10, 0);
      fail("Exception while get list of issues expected.");
    } catch (Exception e) {}

  }

  @Test
  public void testGetProjects() {
    List<YouTrackProject> projects = client.getProjects();
    Set<String> projectsShortNamesSet = new HashSet<String>();
    for (YouTrackProject project : projects) {
      projectsShortNamesSet.add(project.getProjectShortName());
    }
    assertTrue(projectsShortNamesSet.contains(TEST_PROJECT_NAME));
    assertTrue(projectsShortNamesSet.contains(MYLYN_PROJECT_NAME));
  }

  @Test
  public void testGetProject() {
    YouTrackProject project = client.getProject(TEST_PROJECT_NAME);
    assertEquals(project.getProjectShortName(), TEST_PROJECT_NAME);
  }

  @Test
  public void testPutNewIssue() {
    YouTrackIssue issue = new YouTrackIssue();
    issue.addSingleField("projectShortName", TEST_PROJECT_NAME);
    issue.addSingleField("summary", "testPutNewIssue$: test issue ? , + - # {");
    issue.addSingleField("description", "test description");
    client.putNewIssue(issue);

    issue.addSingleField("description", null);
    client.putNewIssue(issue);

    issue.addSingleField("summary", null);
    try {
      client.putNewIssue(issue);
      fail("Exception expected in oput new issue ");
    } catch (Exception e) {}

    issue.addSingleField("summary", "");
    issue.addSingleField("projectShortName", "");
    try {
      client.putNewIssue(issue);
      fail("Exception expected in oput new issue ");
    } catch (Exception e) {}

  }

  @Test
  public void testDeleteIssue() {
    YouTrackIssue issue = new YouTrackIssue();
    issue.addSingleField("projectShortName", TEST_PROJECT_NAME);
    issue.addSingleField("summary", "testDeleteIssue$: test issue ? , + - # {");
    issue.addSingleField("description", "test description");
    String id = client.putNewIssue(issue);
    client.deleteIssue(id);
    try {
      client.getIssue(id);
      fail("Exception expected while get deleted issue.");
    } catch (Exception e) {}

    try {
      client.deleteIssue(" ");
      fail("Exception expected while delete issue.");
    } catch (Exception e) {}

    try {
      client.deleteIssue(null);
      fail("Exception expected while delete issue.");
    } catch (Exception e) {}
  }

  @Test
  public void testApplyCommand() {
    YouTrackIssue issue = client.getIssue(TEST_PROJECT_NAME + "-13");
    String id = issue.getId();

    client.applyCommand(id, "Feature");
    issue = client.getIssue(id);
    assertEquals("Feature", issue.getSingleCustomFieldValue("Type"));

    client.applyCommand(id, "add Submitted");
    issue = client.getIssue(id);
    assertEquals("Submitted", issue.getSingleCustomFieldValue("State"));

    client.applyCommand(id, "Won't fix");
    issue = client.getIssue(id);
    assertEquals("Won't fix", issue.getSingleCustomFieldValue("State"));

    client.applyCommand(id, "Bug Submitted");

    try {
      client.applyCommand(id, null);
      fail("Exception expected while apply command to issue.");
    } catch (Exception e) {}

  }

  @Test
  public void testIssueExist() {
    assertTrue(client.issueExist(TEST_PROJECT_NAME + "-3"));
    assertFalse(client.issueExist(TEST_PROJECT_NAME + "-0"));

    try {
      client.issueExist(null);
      fail("Exception expected while check that null issue id exist.");
    } catch (Exception e) {}

  }

  @Test
  public void testGetNumberOfIssues() {
    int count = client.getNumberOfIssues("project: " + TEST_PROJECT_NAME);
    assertTrue(client.getNumberOfIssues("project: " + TEST_PROJECT_NAME) <= client
        .getNumberOfIssues(null));
    assertEquals(1, client.getNumberOfIssues("#{Meta Issue} " + "project: " + TEST_PROJECT_NAME));
  }

  @Test
  public void testGetIssuesByFilter() {
    List<YouTrackIssue> issues = client.getIssuesByFilter("project: " + TEST_PROJECT_NAME, 10);
    issues = client.getIssuesByFilter("project: " + TEST_PROJECT_NAME);
    issues = client.getIssuesByFilter("{Usability Problem}");
    if (issues.size() > 0) {
      for (YouTrackIssue issue : issues) {
        assertEquals("Usability Problem", issue.getSingleCustomFieldValue("Type"));
      }
    }

    issues = client.getIssuesByFilter(null);
  }

  @Test
  public void testGetProjectCustomFields() {
    LinkedList<YouTrackCustomField> cfs = client.getProjectCustomFields(TEST_PROJECT_NAME);
    Set<String> cfNames = client.getProjectCustomFieldNames(TEST_PROJECT_NAME);
    assertTrue(cfNames.contains("Priority"));
    assertTrue(cfNames.contains("Type"));
    assertTrue(cfNames.contains("State"));
    assertTrue(cfNames.contains("Assignee"));
    assertTrue(cfNames.contains("Subsystem"));
    assertTrue(cfNames.contains("Fix versions"));
    assertTrue(cfNames.contains("Affected versions"));
    assertTrue(cfNames.contains("Fixed in build"));
    assertTrue(cfNames.contains("Simple string"));
    assertTrue(cfNames.contains("Integer field"));

    try {
      cfs = client.getProjectCustomFields(null);
      fail("Exception expected while get custom fields by null projectname.");
    } catch (Exception e) {}

    try {
      cfs = client.getProjectCustomFields("unexisted project");
      fail("Exception expected while get custom fields by unexisted projectname.");
    } catch (Exception e) {}

  }

  @Test
  public void testGetProjectCustomField() {
    YouTrackCustomField cf = client.getProjectCustomField(TEST_PROJECT_NAME, "Type");
    assertEquals("Type", cf.getName());
    cf.findBundle();
    assertEquals("Types", cf.getBundle().getName());
    assertEquals("enum[1]", cf.getType());

    try {
      cf = client.getProjectCustomField(null, "Type");
      fail("Exception expected while get custom field by null projectname.");
    } catch (Exception e) {}

    try {
      cf = client.getProjectCustomField("unexisted project", "Type");
      fail("Exception expected while get custom field by unexisted projectname.");
    } catch (Exception e) {}

    try {
      cf = client.getProjectCustomField(MYLYN_PROJECT_NAME, "Unexisted custom field");
      fail("Exception expected while get unexisted custom field.");
    } catch (Exception e) {}
  }

  @Test
  public void testGetCustomFieldBundleValues() {

    LinkedList<String> bundleValues =
        ((BundleValues) client.getEnumerationBundleValues("Types")).getValues();
    assertEquals(10, bundleValues.size());
    assertEquals("Bug", bundleValues.get(0));

    try {
      bundleValues =
          ((BundleValues) client.getEnumerationBundleValues("Unexisted enumeration bundlename"))
              .getValues();
      fail("Exception expected while get unexisted enumeration bundle values.");
    } catch (Exception e) {}

    try {
      bundleValues = ((BundleValues) client.getEnumerationBundleValues(null)).getValues();
      fail("Exception expected while get null bundle values.");
    } catch (Exception e) {}

    bundleValues = ((BundleValues) client.getOwnedFieldBundleValues("Subsystems")).getValues();
    assertEquals(1, bundleValues.size());
    assertEquals("No subsystem", bundleValues.get(0));
    assertEquals("root", client.getOwnedFieldBundleValues("Subsystems").getOwnedFields().get(0)
        .getOwner());

    try {
      bundleValues =
          ((BundleValues) client.getOwnedFieldBundleValues("Unexisted ownedField bundle"))
              .getValues();
      fail("Exception expected while get unexisted ownedField bundle values.");
    } catch (Exception e) {}

    try {
      bundleValues = ((BundleValues) client.getOwnedFieldBundleValues(null)).getValues();
      fail("Exception expected while get null bundle values.");
    } catch (Exception e) {}

    bundleValues = ((BundleValues) client.getBuildBundleValues("Builds")).getValues();
    assertEquals(0, bundleValues.size());

    try {
      bundleValues =
          ((BundleValues) client.getBuildBundleValues("Unexisted build bundle")).getValues();
      fail("Exception expected while get unexisted build bundle values.");
    } catch (Exception e) {}

    try {
      bundleValues = ((BundleValues) client.getBuildBundleValues(null)).getValues();
      fail("Exception expected while get null bundle values.");
    } catch (Exception e) {}

    bundleValues = ((BundleValues) client.getStateBundleValues("States")).getValues();

    assertEquals(14, bundleValues.size());
    assertTrue(bundleValues.contains("Submitted"));
    assertTrue(bundleValues.contains("Open"));
    assertTrue(bundleValues.contains("In Progress"));
    assertTrue(bundleValues.contains("To be discussed"));
    assertTrue(bundleValues.contains("Reopened"));
    assertTrue(bundleValues.contains("Can't Reproduce"));
    assertTrue(bundleValues.contains("Duplicate"));
    assertTrue(bundleValues.contains("Fixed"));
    assertTrue(bundleValues.contains("Won't fix"));
    assertTrue(bundleValues.contains("Incomplete"));
    assertTrue(bundleValues.contains("Obsolete"));
    assertTrue(bundleValues.contains("Verified"));
    assertTrue(bundleValues.contains("New"));

    try {
      bundleValues =
          ((BundleValues) client.getStateBundleValues("Unexisted enumeration bundlename"))
              .getValues();
      fail("Exception expected while get unexisted state bundle values.");
    } catch (Exception e) {}

    try {
      bundleValues = ((BundleValues) client.getStateBundleValues(null)).getValues();
      fail("Exception expected while get null bundle values.");
    } catch (Exception e) {}

    bundleValues = ((BundleValues) client.getVersionBundleValues("Versions")).getValues();
    assertEquals(0, bundleValues.size());

    try {
      bundleValues =
          ((BundleValues) client.getVersionBundleValues("Unexisted versions bundle")).getValues();
      fail("Exception expected while get unexisted version bundle values.");
    } catch (Exception e) {}

    try {
      bundleValues = ((BundleValues) client.getVersionBundleValues(null)).getValues();
      fail("Exception expected while get null bundle values.");
    } catch (Exception e) {}

    bundleValues =
        ((BundleValues) client.getUserBundleValues("Mylyn Eclipse Plugin Assignees")).getValues();
    assertEquals(0, bundleValues.size());

    try {
      bundleValues =
          ((BundleValues) client.getUserBundleValues("Unexisted user bundle")).getValues();
      fail("Exception expected while get unexisted user bundle values.");
    } catch (Exception e) {}

    try {
      bundleValues = ((BundleValues) client.getUserBundleValues(null)).getValues();
      fail("Exception expected while get null bundle values.");
    } catch (Exception e) {}
  }

  @Test
  public void testIsStateResolved() {
    boolean resolved = client.isStateResolved("States", "Incomplete");
    assertTrue(resolved);

    resolved = client.isStateResolved("States", "Fixed");
    assertTrue(resolved);

    resolved = client.isStateResolved("States", "Open");
    assertTrue(!resolved);
  }

  @Test
  public void testAddComment() {
    YouTrackIssue issue = client.getIssue(TEST_PROJECT_NAME + "-18");
    int commentsLength = issue.getComments().size();
    client.addComment(TEST_PROJECT_NAME + "-18", "New test comment");
    issue = client.getIssue(TEST_PROJECT_NAME + "-18");
    assertTrue(issue.getComments().size() == commentsLength + 1);

    try {
      client.addComment(TEST_PROJECT_NAME + "-18", null);
      fail("Exception expected while add new comment with null body.");
    } catch (Exception e) {}

    try {
      client.addComment(TEST_PROJECT_NAME + "-0", "New comment");
      fail("Exception expected while add new comment to the unexisted issue.");
    } catch (Exception e) {}

  }

  @Test
  public void testIntellisenseSearch() {

    String[] intellisenseOptions = client.intellisenseFullOptions("created");
    boolean containsCreatedBy = false;
    for (String option : intellisenseOptions) {
      containsCreatedBy = containsCreatedBy || option.equals("created by: ");
    }
    assertTrue(containsCreatedBy);

    try {
      intellisenseOptions = client.intellisenseFullOptions(null);
      fail("Exception expected while get intellisense options for null filter.");
    } catch (Exception e) {}

    LinkedList<IntellisenseItem> items = client.intellisenseItems(" Un");
    boolean containsUnreadOption = false;
    for (IntellisenseItem item : items) {
      if (item.getOption().equals("unread")) {
        assertTrue(item.getCompletionPositions().getStart() == 1);
        assertTrue(item.getCompletionPositions().getEnd() == 3);
        containsUnreadOption = true;
      }
    }
    assertTrue(containsUnreadOption);

    intellisenseOptions = client.intellisenseFullOptions(" Un");
    assertEquals(items.size(), intellisenseOptions.length);

    try {
      items = client.intellisenseItems(null);
      fail("Exception expected while get intellisense items for null filter.");
    } catch (Exception e) {}

    try {
      items = client.intellisenseItems(null, 0);
      fail("Exception expected while get intellisense items for null filter.");
    } catch (Exception e) {}

    items = client.intellisenseItems("unread", 6);
    assertTrue(items.size() == 1);
    items = client.intellisenseItems("unread", 4);
    assertTrue(items.size() == 2);
  }

  @Test
  public void testGetSavedSearches() {
    LinkedList<String> searches = client.getSavedSearchesNames();

    SavedSearch search = client.getSavedSearch("Usability Problem");
    assertEquals("#{Usability Problem}", search.getSearchText());

    LinkedList<UserSavedSearch> userSearches = client.getSavedSearchesForUser(TEST_PROJECT_USER);

    searches = client.getSavedSearchesNamesForUser(TEST_PROJECT_USER);
    assertTrue(searches.contains("Assigned to me"));
    assertTrue(searches.contains("Commented by me"));
    assertTrue(searches.contains("Reported by me"));
    assertTrue(searches.contains("Usability Problem"));

    try {
      searches = client.getSavedSearchesNamesForUser(null);
      fail("Exception expected while get saved searches for null username.");
    } catch (Exception e) {}

    try {
      searches = client.getSavedSearchesNamesForUser("unexisted username");
      fail("Exception expected while get saved searches for wrong username.");
    } catch (Exception e) {}

  }

  @Test
  public void testGetUserTags() {
    String[] tags = client.getAllSuitableTags();
    assertTrue(tags.length == 2);
    LinkedList<String> tagList = new LinkedList<String>();
    for (String tag : tags) {
      tagList.add(tag);
    }
    assertTrue(tagList.contains("New tag"));
    assertTrue(tagList.contains("Yarrr"));
  }

  @Test
  public void testUpdateIssueSummaryAndDescription() {
    String testIssueId = TEST_PROJECT_NAME + "-19";
    YouTrackIssue issue = client.getIssue(testIssueId);
    assertEquals("testUpdateIssueSummaryAndDescription$1", issue.getSummary());
    assertEquals("testUpdateIssueSummaryAndDescription$1", issue.getDescription());
    client.updateIssueSummaryAndDescription(testIssueId, "NEW SUMMARY", "NEW DESCRIPTION");
    issue = client.getIssue(testIssueId);
    assertEquals("NEW SUMMARY", issue.getSummary());
    assertEquals("NEW DESCRIPTION", issue.getDescription());

    try {
      client.updateIssueSummaryAndDescription(testIssueId, "", "Descr");
      fail("Exception expected while send empty issue summary.");
    } catch (Exception e) {}

    try {
      client.updateIssueSummaryAndDescription("not existed id", "Summary", "Description");
      fail("Exception expected while send issue summary and description by wrong issue id.");
    } catch (Exception e) {}

    try {
      client.updateIssueSummaryAndDescription(null, "Summary", "Description");
      fail("Exception expected while send null issue id.");
    } catch (Exception e) {}

    client.updateIssueSummaryAndDescription(testIssueId, "NEW SUMMARY", null);
    issue = client.getIssue(testIssueId);
    assertEquals("NEW DESCRIPTION", issue.getDescription());

    client.updateIssueSummaryAndDescription(testIssueId, "testUpdateIssueSummaryAndDescription$1",
        "testUpdateIssueSummaryAndDescription$1");
  }

  @Test
  public void testUpdateIssue() {
    YouTrackIssue issue = client.getIssue(TEST_PROJECT_NAME + "-20");
    YouTrackProject project = client.getProject(issue.getProjectName());
    project.updateCustomFields(client);
    issue.fillCustomFieldsFromProject(project, client);
    issue.addCustomFieldValue("Type", "Bug");
    client.updateIssue(issue.getId(), issue);
    issue = client.getIssue(TEST_PROJECT_NAME + "-20");
    issue.fillCustomFieldsFromProject(project, client);
    assertEquals("Bug", issue.getSingleCustomFieldValue("Type"));

    try {
      issue.addCustomFieldValue("Type", "");
      client.updateIssue(issue.getId(), issue);
      fail("Exception expected while upload incorrect value of custom field.");
    } catch (Exception e) {}

    issue.addCustomFieldValue("Type", "Task");
    client.updateIssue(issue.getId(), issue);

    try {
      client.updateIssue("unexisted issue id", issue);
      fail("Exception expected while update unexisted issue.");
    } catch (Exception e) {}
  }


  @AfterClass
  public static void removeTestIssues() {}

}
