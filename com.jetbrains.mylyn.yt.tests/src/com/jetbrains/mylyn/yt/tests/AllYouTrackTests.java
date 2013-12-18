package com.jetbrains.mylyn.yt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.mylyn.commons.sdk.util.CommonTestUtil;
import org.eclipse.mylyn.commons.sdk.util.ManagedTestSuite;
import org.eclipse.mylyn.commons.sdk.util.TestConfiguration;

/**
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Frank Becker
 */
public class AllYouTrackTests {

  public static Test suite() {
    if (CommonTestUtil.fixProxyConfiguration()) {
      CommonTestUtil.dumpSystemInfo(System.err);
    }

    TestSuite suite = new ManagedTestSuite(AllYouTrackTests.class.getName());
    addTests(suite, TestConfiguration.getDefault());
    return suite;
  }

  public static Test suite(TestConfiguration configuration) {
    TestSuite suite = new TestSuite(AllYouTrackTests.class.getName());
    addTests(suite, configuration);
    return suite;
  }

  public static void addTests(TestSuite suite, TestConfiguration configuration) {
    suite.addTestSuite(YouTrackRepositoryConnectorStandaloneTest.class);
    suite.addTestSuite(YouTrackRepositoryPageTest.class);
    suite.addTestSuite(YouTrackTaskEditorTest.class);
    suite.addTestSuite(CorrectJaxbBindingTest.class);
    suite.addTestSuite(YouTrackClientTest.class);
  }
}
