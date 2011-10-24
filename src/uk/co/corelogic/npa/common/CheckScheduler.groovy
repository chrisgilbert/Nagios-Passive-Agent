package uk.co.corelogic.npa.common
import uk.co.corelogic.common.*
import java.util.concurrent.*

/**
 *
 * This class helps schedule checks in seperate threads, and attempts to keep checks running and cope with ThreadDeath and other bad things
 */
class CheckScheduler { 

    private static allFutures = []
    private static allTimers = []


    // Enforce singleton
    private CheckScheduler() {    }
     /* 
      * Schedule a new scheduled check every so many seconds
      * Check will run immediately the first time, and then after the interval specified in the Check object
      * These threads will run seperately in the background
      */
     public static void schedule(Check c) {

        try {
            // Add a random delay of up to 30 seconds, which attempts to stop checks running all at once, each cycle.
            // This can cause locking issues on Windows WMI checks otherwise
            Random random = new Random();
            long delay = random.nextInt(30000);

            long interval = c.chk_interval.toString().toLong();

            ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor();
            
            Log.info("Scheduling ${c.chk_name} for every ${interval} ms ..")

            synchronized(this) {
                allFutures.add(timer1.scheduleWithFixedDelay(c, delay, interval, TimeUnit.MILLISECONDS))
                allTimers.add(timer1)
                Log.debug("Completed adding check.")
            }


        } catch(e) {
            Log.error("Oops.  An exception occurred when scheduling a check.  There's probably a problem with the configuration.", e)
            throw e
        }
    }


    public static void schedule(Thread c) {

        try {
            // Add a random delay of up to 30 seconds, which attempts to stop checks running all at once, each cycle.
            // This can cause locking issues on Windows WMI checks otherwise
            Random random = new Random();
            long delay = random.nextInt(30000);

            long interval = c.timer.toString().toLong();

            ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor();

            Log.info("Scheduling thread for every ${interval} ms ..")

            synchronized(this) {
                allFutures.add(timer1.scheduleWithFixedDelay(c, delay, interval, TimeUnit.MILLISECONDS))
                allTimers.add(timer1)
                Log.debug("Completed adding check.")
            }


        } catch(e) {
            Log.error("Oops.  An exception occurred when scheduling a thread.  There's probably a problem with the configuration.", e)
            throw e
        }
    }


    /*
     * Check status of each thread to make sure it hasn't died for some reason, and call warnings if so.
     *
     */
    public static void checkStatus() {
        Log.debug("Checking thread statuses..")
            allFutures.each {
                if (! (it instanceof RunMaintenance)) {
                    if ( it.isDone() ) {
                        Log.error("***** Thread has died or got stuck!")
                        MaintenanceUtil.sendCriticalHost()
                        it.get()
                    } else {
                        Log.debug("Thread is running correctly.")
                    }
                }
            }
        Log.debug("Completed thread checks.")
    }

    synchronized public static void stopAllTimers(){
        allFutures.collect {
            Log.warn("Cancelling check thread..")
            try {
                it.cancel()
            } catch(e) {
                Log.error("Exception was suppressed!", e)
            }
        }

        allTimers.collect {
            Log.warn("Stopping timer thread..")
            try {
                it.shutdownNow()
            } catch(e) {
                Log.error("Exception was suppressed!", e)
            }
        }
    }

}

