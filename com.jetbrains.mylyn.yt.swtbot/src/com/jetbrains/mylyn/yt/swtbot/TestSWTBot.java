package com.jetbrains.mylyn.yt.swtbot;

import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SWTBotJunit4ClassRunner.class)
public class TestSWTBot {

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

    bot.menu("File").menu("New").menu("Project...").click();

    bot.tree().select("Java Project");

    bot.button("Next >").click();

    bot.textWithLabel("Project name:").setText("MyFirstProject");

    bot.button("Finish").click();
  }

  @Test
  public void title() {
    assertEquals("Java - Eclipse SDK", bot.activeShell().getText());
  }

}
