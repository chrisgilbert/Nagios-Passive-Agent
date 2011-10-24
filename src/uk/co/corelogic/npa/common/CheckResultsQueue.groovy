package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.NPA
import uk.co.corelogic.npa.nagios.*

public class CheckResultsQueue {

    private static def queue = []

    // Force singleton
    private CheckResultsQueue() {  }


    synchronized public static void add (result) {
            try {
                Log.debug("Adding $result.check_name from $result.hostname to results queue.")
                queue.add([object:result, submitted:false, failures:0])
                Log.info("There are " + queue.size() + " items in the queue.")
                if (queue.size() > 100 ) {
                    Log.error("Queue size is too high!  It is likely checks are not being correctly submitted.")
                    println("Wrapper - you should restart me!")
                }
            } catch (e) {
                Log.error("Exception occurred when adding check result to queue.", e)
                Log.error("STACK:", e)
            }
    }

    synchronized public static void flush() {

            // Attempt to process queue - if submittion fails, then the check result will stay in the queue
            // and attempt to be processed x number of times.  If an exception is thrown somewhere then the
            // whole queue is flushed to avoid an ever increasing queue length.
            try {
            Log.debug("**BEGIN queue flush()")

                queue.each {
                    if (NPA.config.npa.submit_method == 'http') {
                            if ( SendCheckResultHTTP.submit(it.object) ) {
                                it.submitted = true
                            } else {
                                // Increment fail counter
                                it.failures=it.failures+1
                                Log.warn("Failed to submit check ${it.object.check_name} for ${it.object.hostname}.  Failure ${it.failures}/${NPA.config.npa.allowed_submit_failures.toString().toInteger()} allowed.")
                            }
                    } else {
                        Log.error("Submittion method isn't supported.  Sorry - check was not submitted.")
                    }
                }

                def i = queue.iterator()
                while (i.hasNext()) {
                    def me = i.next()
                    if (me.submitted == true) {
                        // Remove sucessful submitions from queue
                        Log.info("Submitted $me.object.check_name for $me.object.hostname successfully.")
                        i.remove()
                    } else {
                        if (me.failures >= NPA.config.npa.allowed_submit_failures.toString().toInteger() ) {
                            Log.error("ERROR: Failed to submit check request for ${me.object.check_name} ${me.failures} times.  Giving up :(")
                            i.remove()
                        }
                    }
                }
                if ( queue && queue.size() == 0 ) { queue = [] }
                println("There are " + queue.size() + " items in the queue.")
                Log.debug("**END queue flush()")

            } catch(e) {
                    Log.error("Exception occured whilst flushing queue!", e)
                    Log.error("STACK:", e)
                    e.printStackTrace();
                    queue.clear()
                    if ( queue && queue.size() == 0 ) { queue = null }
            }

    }

}
