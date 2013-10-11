/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;


public abstract class MyRunnable{
		
	public IStatus execute(String message, String pluginId) {
		try{
			run();
			return Status.OK_STATUS;
		} catch (Exception e){
			return new Status(IStatus.ERROR, pluginId,
					NLS.bind(message+" : {0}", e.getMessage()), e);
		}
	}

	public abstract void run() throws Exception;

}
