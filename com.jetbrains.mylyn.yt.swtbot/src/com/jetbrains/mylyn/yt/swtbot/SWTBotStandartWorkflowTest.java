package com.jetbrains.mylyn.yt.swtbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SWTBotJunit4ClassRunner.class)
public class SWTBotStandartWorkflowTest extends TestCase {

  private static final String TEST_REPO_NAME = "SWTBot Test REPO";

  public static final String REPOSITORY_URL = "http://nylym.myjetbrains.com/youtrack/";

  public static final String REAL_USER_ID = "tester";

  public static final String REAL_USER_PASSWORD = "12345";

  public static final String TEST_PROJECT_NAME = "autotest";

  public static final String MYLYN_PROJECT_NAME = "mylyn";

  public static final String MINOR_PRIORUTY = "Minor";

  public static final String LAST_MAJOR_VERSION = "1.0";

  public static final String QUERY_FILTER_STRING = "SWTBotStandartWorkflowTest";

  public static final String QUERY_NAME = "SWTBotStandartWorkflowTest Query";

  private final List<String> suitableTitlesForIssues = new ArrayList<String>(Arrays.asList(
      "autotest-238: SWTBotStandartWorkflowTest$1", "autotest-239: SWTBotStandartWorkflowTest$2",
      "autotest-240: SWTBotStandartWorkflowTest$3"));

  private final List<String> issuesInQuery = new ArrayList<String>(Arrays.asList("autotest-238",
      "autotest-239", "autotest-240"));

  private static SWTWorkbenchBot bot = new SWTWorkbenchBot();

  @BeforeClass
  public static void beforeClass() throws Exception {
    new SWTWorkbenchBot().viewByTitle("Welcome").close();
  }

  @AfterClass
  public static void sleep() {
    bot.sleep(2000);
  }

  @Test
  public void canCreateANewJavaProject() throws Exception {

    // close all views
    for (SWTBotView view : bot.views()) {
      view.close();
    }

    // open task repository view
    bot.menu("Window").menu("Show View").menu("Other...").click();
    bot.tree().expandNode("Mylyn").select("Task Repositories");
    bot.tree().select("Task Repositories");
    bot.button("OK").click();
    bot.captureScreenshot("screenshots/openRepositories.jpg");
    assertEquals("Task Repositories", bot.activeView().getTitle());
    SWTBotToolbarButton addRepo = null;
    for (SWTBotToolbarButton button : bot.activeView().getToolbarButtons()) {
      if (button.getToolTipText().equals("Add Task Repository...")) {
        addRepo = button;
      }
    }
    addRepo.click();
    bot.captureScreenshot("screenshots/openConnectorsList.jpg");

    // add new YouTrack repository
    bot.table().select("YouTrack Repository");
    bot.button("Next >").click();
    bot.comboBox().setText(REPOSITORY_URL);
    bot.text().setText(TEST_REPO_NAME);
    bot.checkBox().setFocus();
    bot.checkBox().setFocus();
    bot.textWithLabel("User ID: ").setText(REAL_USER_ID);
    bot.textWithLabel("Password: ").setText(REAL_USER_PASSWORD);
    bot.captureScreenshot("screenshots/fillYTRepositoryData.jpg");
    bot.button("Finish").click();
    bot.button("Yes").click();

    // add new query and perform it
    if (bot.radio(1).getText().equals("Custom query")) {
      bot.radio(1).click();
    }
    bot.textWithLabel("Search Box:").setText(QUERY_FILTER_STRING);
    bot.textWithLabel("Title:").setText(QUERY_NAME);
    bot.button("Finish").click();
    bot.captureScreenshot("screenshots/justCreateNewQuery.jpg");
    bot.menu("Window").menu("Show View").menu("Task List").click();
    assertEquals("Task List", bot.activeView().getTitle());

    // open issues from query in editor
    bot.viewByTitle("Task List").bot().tree().getAllItems()[0].expand().getItems()[0].select()
        .doubleClick();
    assertTrue(suitableTitlesForIssues.contains(bot.activeEditor().getTitle()));
    bot.captureScreenshot("screenshots/justOpenIssueEditor.jpg");
    bot.viewByTitle("Task List").bot().tree().getAllItems()[0].expand().getItems()[1].select()
        .doubleClick();
    assertTrue(suitableTitlesForIssues.contains(bot.activeEditor().getTitle()));
    bot.viewByTitle("Task List").bot().tree().getAllItems()[0].expand().getItems()[2].select()
        .doubleClick();
    assertTrue(suitableTitlesForIssues.contains(bot.activeEditor().getTitle()));

    // try to open link to issue in internal browser
    bot.toolbarButtonWithTooltip("Open issue in internal Eclipse browser").click();
    bot.captureScreenshot("screenshots/openIssueLinkInBrowser.jpg");
    bot.activeEditor().close();
    bot.toolbarButtonWithTooltip("Update project settings").click();
    bot.toolbarButtonWithTooltip("Synchronize Incoming changes").click();

    // create new task, submit and delete from tracker
    SWTBotMenu fileMenu = bot.menu("File");
    fileMenu.menu("New").menu("Task").click();
    bot.tree().select(TEST_REPO_NAME);
    bot.button("Next >").click();
    bot.comboBox().setFocus();
    for (int i = 0; i < bot.comboBox().itemCount(); i++) {
      String option = bot.comboBox().items()[i];
      if (option.contains(TEST_PROJECT_NAME)) {
        bot.comboBox().setSelection(i);
        break;
      }
    }

    bot.button("Finish").click();
    bot.captureScreenshot("screenshots/openNewIssueEditor.jpg");
    bot.styledText(1).setText("Issue created by SWTBOT " + new Date());
    bot.toolbarButtonWithTooltip("Submit").click();
    bot.toolbarButtonWithTooltip("Delete task from tracker").click();
    bot.button("OK").click();
    assertTrue(suitableTitlesForIssues.contains(bot.activeEditor().getTitle()));

    // try to apply revert
    // #1: edit and revert in place
    bot.toolbarButtonWithTooltip("Edit").click();
    String issueSummary = bot.styledText(1).getText();
    bot.styledText(1).setText(issueSummary + "(edited)");
    bot.toolbarButtonWithTooltip("Revert all changes").click();
    assertEquals(issueSummary, bot.styledText(1).getText());

    // #2: edit and revert in after save
    bot.toolbarButtonWithTooltip("Edit").click();
    bot.styledText(1).setText(issueSummary + "(edited)");
    bot.menu("File").menu("Save").click();
    bot.toolbarButtonWithTooltip("Revert all changes").click();
    bot.button("OK").click();
    assertEquals(issueSummary, bot.styledText(1).getText());

    // edit and submit issue
    bot.toolbarButtonWithTooltip("Edit").click();
    bot.styledText(1).setText(issueSummary + "(edited)");
    bot.toolbarButtonWithTooltip("Submit").click();
    bot.toolbarButtonWithTooltip("Edit").click();
    bot.styledText(1).setText(issueSummary);
    bot.toolbarButtonWithTooltip("Submit").click();

    // create new task, edit, save and delete without submit
    fileMenu.menu("New").menu("Task").click();
    bot.tree().select(TEST_REPO_NAME);
    bot.button("Next >").click();
    bot.comboBox().setFocus();
    for (int i = 0; i < bot.comboBox().itemCount(); i++) {
      String option = bot.comboBox().items()[i];
      if (option.contains(TEST_PROJECT_NAME)) {
        bot.comboBox().setSelection(i);
        break;
      }
    }
    bot.button("Finish").click();
    bot.styledText(1).setText("Issue created by SWTBOT " + new Date());
    bot.menu("File").menu("Save").click();
    bot.toolbarButtonWithTooltip("Delete task").click();
  }
}
