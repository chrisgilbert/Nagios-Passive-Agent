package uk.co.corelogic.npa.common
import uk.co.corelogic.common.*
import java.util.concurrent.*

/**
 *
 * This class helps schedule checks in seperate threads, and attempts to keep checks running and cope with ThreadDeath and other bad things
 */
class CheckScheduler { //implements Thread.UncaughtExceptionHandler {

    static synchronized allThreads = []
    static synchronized checkThreadMap = [:]
    static synchronized allFutures = []
    static synchronized int unhandledExceptionsCounter
    static synchronized allTimers = []

    public CheckScheduler() {
    //    Thread.setUncaughtExceptionHandler(this)

    }
     /* 
      * Schedule a new scheduled check every so many seconds
      * Check will run immediately the first time, and then after the interval specified in the Check object
      * These threads will run seperately in the background
      */
     synchronized public static void schedule(Check c) {

        try {
            // Add a random delay of up to 60 seconds, which attempts to stop checks running all at once, each cycle.
            // This can cause locking issues on Windows WMI checks otherwise
            Random random = new Random();
            long delay = random.nextInt(60000);
            //long delay = 0   // delay for 0 sec.
            long interval = c.chk_interval.toString().toLong();

            //allThreads.add(c)

            ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor();
            
            Log.info("Scheduling ${c.chk_name} for every ${interval} ms ..")
            try {
                allFutures.add(timer1.scheduleWithFixedDelay(c, delay, interval, TimeUnit.MILLISECONDS))
            } catch(e) {
                e.printStackTrace()
                Log.error("Failed to schedule check - trying once more.")
                allFutures.add(timer1.scheduleWithFixedDelay(c, delay, interval, TimeUnit.MILLISECONDS))
            }
            allTimers.add(timer1)


        } catch(e) {
            Log.error("Oops.  An exception occurred when scheduling a check.  There's probably a problem with the configuration.", e)
            Log.error("STACK TRACE:", e)
            //printThreadState()
            throw e
        }
    }


//
//    /*
//     * Tries to handle uncaught thread exceptions by restarting the thread.
//    */
//    synchronized public static void uncaughtException(Thread t, Throwable e) throws ThreadDeath {
//        // Limit to the number of exceptions which can be thrown before checks are no longer restarted.
//        int limit = 50;
//
//        Log.error("Thread has thrown an unhandled exception!", e)
//        if ( unhandledExceptionsCounter < limit ) {
//            restartThread(t.getID());
//        } else {
//            Log.error("Checks have thrown over $limit exceptions.  Will not attempt to restart thread.")
//        }
//        unhandledExceptionsCounter++
//        throw t.ThreadDeath();
//    }
//
//
//    synchronized private static Check getCheck(id){
//            try {
//                return checkThreadMap[id].clone()
//            } catch(e) {
//                Log.error("Not a registered thread...", e)
//                throw new NPAException("Not a registered thread..." + id)
//            }
//    }
//
//    /*
//     * Restarts a check in a new thread, if given an ID of a failed thread.
//    */
//    synchronized public static void restartThread(id) {
//            Log.warn("Restarting thread with ID " + id )
//            Check c = getCheck(id);
//            unregisterThread(id)
//            long delay = 0   // delay for 0 sec.
//            long interval = c.chk_interval.toString().toLong();
//            Timer timer = new Timer(c.chk_name + "_" + new Date().format("mmssS"))
//            Log.info("Scheduling ${c.chk_name} for every ${interval} ms ..")
//            timer.scheduleAtFixedRate(c, delay, interval)
//    }
}

