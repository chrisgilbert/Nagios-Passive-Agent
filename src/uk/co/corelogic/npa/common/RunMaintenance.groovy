package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.metrics.MetricsDB

/**
 * thread to run maintenance methods
 * @author Chris Gilbert
 */
class RunMaintenance extends Thread {

    private Long timer = 30000

    RunMaintenance(timer)
    {
        super("MaintenanceJob")
        this.timer = timer
    }

    void run() {
        while (true) {
            try {
                Log.info("Running maintenance job..")
                MetricsDB.purgeMetrics()
                CheckScheduler.checkStatus()
                this.sleep(timer)
                Log.info("Completed maintenance job.")
            } catch(e) {
                Log.fatal("Maintenance thread has died!", e)
                MaintenanceUtil.sendCriticalHost()
            }
        }
    }
}

