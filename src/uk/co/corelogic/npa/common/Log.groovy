package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.NPA
import org.apache.log4j.*
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PatternLayout;
import java.io.FileNotFoundException;

class Log {

  static config = NPA.getConfigObject()
  

  private static boolean initialized = false
  
    
  private static logger() {
   def caller = Thread.currentThread().stackTrace
   def loggerClass = null
   def meth = null // capture method name for log4j printout
   for (stackTraceEntry in caller) {
	if ((stackTraceEntry =~ /.*groovy:.*/) && !(stackTraceEntry =~ /.*Log.groovy:.*/)) {
		loggerClass = stackTraceEntry.className
		meth = stackTraceEntry.methodName
		break
	}
   }
   if (!initialized)  {
        logfile()
        initialized = true
   }

   return [Logger.getInstance(loggerClass), meth]
  }


//private static log(String level, Throwable t, Object... messages) {
//  if (messages) {
//    def log = logger()
//    if (level.equals("Warn") || level.equals("Error") || level.equals("Fatal") || log."is${level}Enabled" ()) {
//      log."${level.toLowerCase()}" (messages.join(), t)
//    }
//  }
//}

    private static log(String level, Throwable t, Object... messages) {
    if (messages) {
      def log = logger()
      def logInst = log[0]
      def logMethod = log[1]
      if (level.equals("Warn") || level.equals("Error") || level.equals("Fatal") || logInst."is${level}Enabled" ()) {
        //hard code method name in log4j lines.  Needed by groovy
        logInst."${level.toLowerCase()}" (logMethod + "(): " + messages.join(), t)
      }
    }
  }

  static basic() {
      println("Cannot find log4j config file - logging to STDOUT instead.")
      try {
        BasicConfigurator.configure()
        LogManager.rootLogger.level = "DEBUG"
      } catch (e) {
          println("Failed to start logging!")
          e.printStackTrace()
      }
  }

  static logfile() {
    def confFile = new File(Log.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/" + config.log4j.config_file.toString()
    println("Log4j configuration file specified at: " + confFile)
    try {
        if ( new File(confFile).exists() ) {
            DOMConfigurator.configure(confFile);
        } else {
            basic()
        }
    } catch (Throwable e) {
        if (e instanceof InterruptedException || e instanceof InterruptedIOException) {
          basic()
          Thread.currentThread().interrupt();// I know this is miserable...
          println("Could not parse log: "+ e);
        }
    }

}  

  static setLevel(level) {
    def Level l = null
    if (level instanceof Level) {
      l = level
    } else if (level instanceof String) {
      l = (Level."${level.toUpperCase()}")?: null
    }
    if (l) LogManager.rootLogger.level = l
  }

  static trace(Object... messages) { log("Trace", null, messages) }
  static trace(Throwable t, Object... messages) { log("Trace", t, messages) }

  static debug(Object... messages) { log("Debug", null, messages) }
  static debug(Throwable t, Object... messages) { log("Debug", t, messages) }

  static info(Object... messages) { log("Info", null, messages) }
  static info(Throwable t, Object... messages) { log("Info", t, messages) }

  static warn(Object... messages) { log("Warn", null, messages) }
  static warn(Throwable t, Object... messages) { log("Warn", t, messages) }

  static error(Object... messages) { log("Error", null, messages) }
  static error(Throwable t, Object... messages) { log("Error", t, messages) }

  static fatal(Object... messages) { log("Fatal", null, messages) }
  static fatal(Throwable t, Object... messages) { log("Fatal", t, messages) }

}





