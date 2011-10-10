package uk.co.corelogic.npa.common

/**
 * Send back host OK to Nagios
 * @author Chris Gilbert
 */
class SubmitHostOK extends Thread {

    SubmitHostOK()
    {
        super()
        this.setName("HostCheckSubmitJob")
    }
    void run() {
        MaintenanceUtil.sendHostOk()
    }
}

