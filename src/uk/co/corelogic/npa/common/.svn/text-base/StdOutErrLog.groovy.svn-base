package uk.co.corelogic.npa.common;
import org.apache.log4j.*
/*
 * Handle stderr and stdout by logging them with Log4j.
*/
public class StdOutErrLog {

    private static final Logger logger = Logger.getLogger(StdOutErrLog.class);

    public static void tieSystemOutAndErrToLog() {
        System.setOut(createLoggingProxy(System.out));
        System.setErr(createLoggingProxy(System.err));
    }

    public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
        return new PrintStream(realPrintStream) {
            def print = { realPrintStream.print(string); logger.info(string); }
        }
    }
    
}