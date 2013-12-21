package com.jetbrains.mylyn.yt.swtbot;

import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SWTBotJunit4ClassRunner.class)
public class SWTBotStandartWorkflowTest {

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

    bot.checkBox("Anonymous").deselect();

    bot.textWithLabel("User ID: ").setText(REAL_USER_ID);

    bot.textWithLabel("Password: ").setText(REAL_USER_PASSWORD);

    bot.checkBox("Save Password").select();

    bot.button("Validate Settings").click();

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
  }
}
