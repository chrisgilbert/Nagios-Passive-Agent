package uk.co.corelogic.npa.common

/**
 * TimerTask to run maintenance methods
 * @author Chris Gilbert
 */
class RunMaintenance extends TimerTask {

    void run() {
        MetricsDB.purgeMetrics()
    }
}

