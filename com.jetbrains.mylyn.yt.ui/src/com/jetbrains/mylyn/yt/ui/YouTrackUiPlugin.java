/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


public class YouTrackUiPlugin extends Plugin {

	public static final String ID_PLUGIN = "com.jetbrains.mylyn.yt.ui";

	private static YouTrackUiPlugin plugin;

	public YouTrackUiPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static YouTrackUiPlugin getDefault() {
		return plugin;
	}

}
