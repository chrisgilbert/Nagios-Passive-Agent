package uk.co.corelogic.npa.common

class FlushQueue extends Thread {

    private Long timer = 30000

    FlushQueue(timer)
    {
        super()
        this.timer = timer
        this.setName("FlushQueue")
    }
    void run() {
        Log.debug("Beginning flush queue..")
        CheckResultsQueue.flush()
        Log.debug("Completed flush queue.")
    }
}