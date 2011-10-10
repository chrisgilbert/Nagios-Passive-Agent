package uk.co.corelogic.npa.common

class FlushQueue extends Thread {

    FlushQueue()
    {
        super()
        this.setName("FlushQueue")
    }
    void run() {
        CheckResultsQueue.flush()
    }
}