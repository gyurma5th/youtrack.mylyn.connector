package util;

import java.io.PrintStream;

import org.apache.log4j.Logger;

public class StdOutErrLog {

  private static final Logger logger = Logger.getLogger(StdOutErrLog.class.getName());

  private final static String APPENDER_NOT_FOUND = "No appenders could be found for logger";

  private final static String INITIALIZE_APPENDER = "Please initialize the log4j system properly.";

  public static void tieSystemOutAndErrToLog() {
    System.setOut(createLoggingProxy(System.out));
    System.setErr(createLoggingProxy(System.err));
  }

  public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {

    return new PrintStream(realPrintStream) {

      @Override
      public void print(final String string) {
        if (!string.contains(APPENDER_NOT_FOUND) && !string.contains(INITIALIZE_APPENDER)) {
          realPrintStream.print(string);
          logger.info(string);
        }
      }

      @Override
      public void print(Object obj) {
        realPrintStream.print(obj);
        logger.info(String.valueOf(obj));
      }
    };
  }
}
