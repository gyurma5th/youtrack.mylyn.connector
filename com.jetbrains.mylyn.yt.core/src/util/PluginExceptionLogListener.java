package util;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

public class PluginExceptionLogListener implements ILogListener {

  @Override
  public void logging(IStatus status, String plugin) {
    if (status != null && status.getException() != null) {
      System.out.println("Error: " + (plugin == null ? plugin : ""));
      status.getException().printStackTrace(System.out);
    }
  }
}
