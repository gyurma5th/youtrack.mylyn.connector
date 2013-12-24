package com.jetbrains.mylyn.yt.ui.utils;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;
import com.jetbrains.mylyn.yt.ui.YouTrackTaskEditorPage;

public class WebViewAction extends Action {

  private YouTrackTaskEditorPage page;

  public WebViewAction(YouTrackTaskEditorPage page) {
    this.page = page;
    setToolTipText("Open issue in internal Eclipse browser");
    setImageDescriptor(CommonImages.WEB);
    if (page.getModel().getTaskData().isNew()) {
      setEnabled(false);
    }
  }

  @Override
  public void run() {
    IWebBrowser browser;
    try {
      browser =
          PlatformUI.getWorkbench().getBrowserSupport()
              .createBrowser(page.getModel().getTaskData().getRoot().getId());
      URL issueURL =
          YouTrackTaskDataHandler.getIssueURL(page.getModel().getTaskData(),
              page.getTaskRepository());
      if (issueURL != null) {
        browser.openURL(issueURL);
      } else {
        page.getEditor().setMessage("Problem with browser.", SWT.ERROR);
      }
    } catch (PartInitException e) {
      throw new RuntimeException(e);
    }
  }


}
