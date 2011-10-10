package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.metrics.MetricsDB

/**
 * TimerTask to run maintenance methods
 * @author Chris Gilbert
 */
class RunMaintenance extends Thread {

    RunMaintenance()
    {
        super()
        this.setName("MaintenanceJob")
    }

    void run() {
        MetricsDB.purgeMetrics()
    }
}

