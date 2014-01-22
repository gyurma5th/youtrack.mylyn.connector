/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class YouTrackCorePlugin extends Plugin {

  public static final String ID_PLUGIN = "com.jetbrains.mylyn.yt.core";

  public static final String CONNECTOR_KIND = "youtrack";

  public static final String REPOSITORY_KEY_PATH = ID_PLUGIN + ".path";

  public static final String QUERY_KEY_SUMMARY = ID_PLUGIN + ".summary";

  public static final String QUERY_KEY_PROJECT = ID_PLUGIN + ".project";

  public static final String QUERY_KEY_ISSUES_COUNT = ID_PLUGIN + ".icount";

  public static final String QUERY_KEY_FILTER = ID_PLUGIN + ".filter";

  private static YouTrackCorePlugin plugin;

  public YouTrackCorePlugin() {}

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;

    // File osgiLog = new File(System.getProperty("osgi.logfile"));
    // String baseOsgiPath =
    // osgiLog.getAbsolutePath().substring(0,
    // osgiLog.getAbsolutePath().length() - osgiLog.getName().length());
    // System.setProperty("com.jetbrains.mylyn.yt.logfile", baseOsgiPath +
    // ".youtrack-mylyn-connector"
    // + File.separator + ".log");
    //
    // StdOutErrLog.tieSystemOutAndErrToLog();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  public static YouTrackCorePlugin getDefault() {
    return plugin;
  }
}
