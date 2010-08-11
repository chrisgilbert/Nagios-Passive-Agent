package uk.co.corelogic.npa.metrics
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.common.*


class RawMetric extends TimerTask {


def metName
def variables
def chk_interval
def check_name
def initiatorID

    RawMetric(met, variables) {
        // Check for nulls
        assert variables != null, 'Variables cannot be null!'
        assert met != null, 'Metric cannot be null!'

        this.metName = met
        this.initiatorID  = UUID.randomUUID();
        variables.initiatorID = this.initiatorID
        this.variables = variables

    }

    public void run() {
        sample() 
    }

    public sample() {
        return sample(this.metName, this.variables)
    }

    public String sample(met, variables) {
        // Check for nulls
        assert variables != null, 'Variables cannot be null!'

        def gatherer = GathererFactory.getGatherer(met, this.initiatorID)
        
        try {
            def metResult = gatherer.sample(met, variables)
            return metResult
        } catch (e) {
            Log.error("An exception occurred when attempting to sample metric, with arguments " + variables )
            Log.error("STACK:", e)
        }
    }

    public schedule(interval) {
        // Check for nulls
        assert interval  != null, 'Interval cannot be null!'

    /* Schedule a new scheduled check every so many seconds
     * Check will run immediately the first time, and then after the interval specified
     * These threads will run seperately in the background
     */

        long delay = 0   // delay for 0 sec.
        this.chk_interval = interval
        def random = new Random()
        Timer timer = new Timer(this.variables.name + "_" + UUID.randomUUID())
        Log.info("Scheduling ${this.variables.name} for every ${interval} ms ..")
        timer.scheduleAtFixedRate(this, delay, interval.toLong())

     }



}