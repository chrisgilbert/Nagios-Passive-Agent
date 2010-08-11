package uk.co.corelogic.npa.common;
import uk.co.corelogic.npa.metrics.*



/**
 * This class is the superclass for all Gatherers.
 * It has some static methods to register metric names, and some more generic messages which
 * saves these being implemented each time.
 *
*/
class Gatherer {

    // Generic instance variables shared by all Gatherers
    def initiatorID
    def metrics = [:]
    def metricList = []

    /**
    * Generic constructor which creates new initiatorID
    */
   public Gatherer() {
       this.initiatorID = UUID.randomUUID();
       Log.debug("Gatherer initiator ID is ${this.initiatorID}")
   }

    /**
     * Generic constructor which takes inititatorID
    */
   public Gatherer(initiatorID) {
       this.initiatorID = initiatorID
       Log.debug("Gatherer initiator ID is ${this.initiatorID}")
   }

    /**
     * Add supplied metrics to public list
    */
    public void addValidMetricList(ArrayList metricList, String type, String classObj) {
        metricList.each {
            if ( ! MetricRegister.isRegistered(it) ) {
                MetricRegister.add(it, type, classObj)
            }
        }

    }

    /**
     * Add supplied metric to public list
    */
    public void addValidMetric(metric, type, classObj) {
        if ( ! MetricRegister.isRegistered(metric) ) {
                MetricRegister.add(metric, type, classObj)
        }
    }

    /**
     * Return a list of all valid metrics
    */
    public getValidMetricNames() {
        return MetricRegister.getMetricNames();
    }

    /**
        Sample a stat using a valid metric ID and return the value - with variables
    */
    public sample(String metricName, variables) throws NPAException {
        if (MetricRegister.getClassName(metricName)) {
            return this.invokeMethod(metricName, variables)
        } else {
            Log.error("No valid metric type registered for " + metricName)
            Log.error("Metric list contents: ${this.metricList}")
            throw new NPAException("No valid metric type registered for " + metricName)
        }
    }

    /**
     * Sample a stat using a valid metric ID and return the value - no variables
    */
    public sample(String metricName) throws NPAException {
        if (MetricRegister.getClassName(metricName)) {
            try {
                return this.invokeMethod(metricName)
            } catch(e) {
                Log.error("Sampling a stat threw an exception.  Oops.", e)
                Log.error("STACK:", e)
                throw e;
            }
        } else {
            Log.error("No valid metric type registered for " + metricName)
            Log.error("Metric list contents: ${this.metricList}")
            throw new NPAException("No valid metric type registered for " + metricName)
        }
    }

    /**
     * Return a collection of all metrics
    */
    public Collection getAll() {
        return metrics
    }

    /**
     * Return a patricular metric using it's key name
    */
    public Metric getMetric(searchFor) {
        return metrics[searchFor]
    }

    /**
    * Persist a metric to disk using a metric model, and values
    */
    public persistMetric(MetricModel mod, value, datestamp) {
        try {
            // Create new metrics
            Metric m1 = new Metric(mod, value, datestamp)
            this.metrics[m1.ID] = m1;
            Log.debug("Metric value: ${m1.value} was recorded for Initiator ID ${m1.initiatorID} with ID ${m1.ID}")
            return m1.ID
        } catch(e) {
            Log.error("Something awful happened when trying to persist a metric.  Sorry.", e)
            Log.error("STACK:", e)
            throw e;
        }
    }

    /**
     * Retrieve an existing metric value where the groupID is known
    */
    public retrieveGroupMetric(groupID, metricName, identifier, instance) {
        try {
            return MetricsDB.findGroupMetric(groupID, identifier, instance)
        } catch(e) {
            Log.error("Unable to retrieve group metric because an exception occurred. Oops.", e)
            Log.error("STACK:", e)
            throw e;
        }
    }

    /**
     * Retrieve an existing metric value with the ID
     */
    public retrieveMetric(metricID) {
        try {
            return MetricsDB.retrieveWithID(metricID)
        } catch(e) {
            Log.error("Unable to retrieve metric because an exception occured!", e)
            Log.error("STACK:", e)
            throw e;
        }
    }
    

    /**
     * Check for an existing group ID in the database for the supplied details.
    */
    public findGroupID(initiatorID, identifier, instance) {
        try {
            return MetricsDB.findGroupID(initiatorID, identifier, instance)
        } catch(e) {
            Log.error("Unable to find group ID because an exception occurred!", e)
            Log.error("STACK:", e)
            throw e;
        }
    }

    


}