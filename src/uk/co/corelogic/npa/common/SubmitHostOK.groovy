package uk.co.corelogic.npa.common

/**
 * Send back host OK to Nagios
 * @author Chris Gilbert
 */
class SubmitHostOK extends Thread {
    private timer = 30000

    SubmitHostOK(timer)
    {
        super()
        this.timer = timer
        this.setName("HostCheckSubmitJob")
    }
    void run() {
        MaintenanceUtil.sendHostOk()
    }
}

