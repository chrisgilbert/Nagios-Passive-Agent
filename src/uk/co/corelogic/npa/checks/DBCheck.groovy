/*
 * A generic Database Check class, to replace OracleCheck and SSCheck.  Emphasis is instead placed on being able to
 * run custom SQL and compare results
 */
package uk.co.corelogic.npa.checks

import groovy.sql.Sql
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.Log
import uk.co.corelogic.npa.gatherers.*

/**
 *
 * @author Chris Gilbert
 */
class DBCheck extends Check implements CheckInterface {

String conn
def metricNames = []

    public DBCheck() {
    }

    // Use this constructor for all classes extending Check
    DBCheck(String chk_name, th_warn, th_crit, String th_type, Map args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }


    @Override
    public init() {
        super.init()
        // Make sure this is set..
        variables.instance=variables.database
        if (! gatherer ) {
            try {
                this.gatherer = new DBGatherer(variables.clone())
            } catch(e) {
                Log.error("An error occurred when creating the gatherer:", e)
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
                Log.error("Throwing error up chain")
                throw e
            }
        }
        
        try {
            gatherer.validateQuery()
            this.metricNames = gatherer.getMetricNames()
            if (this.metricNames == null ) {
                Log.info("No rows returned.  Assuming OK status.")
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "OK", [:], new Date(), "No rows returned.  Assuming OK status."))
            }

        } catch(e) {
            Log.error("An error occurred when running the SQL to find metric names:", e)
            CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when running the SQL to find metric names!"))
            Log.error("Throwing error up chain")
            throw e
        }

        Log.debug("Gatherer initiator ID is ${this.initiatorID}")
    }

    // Standard clone implementation
    @Override
    synchronized public DBCheck clone() {
        DBCheck clone = (DBCheck) super.makeClone(this.chk_name);
        return clone;
    }
   
    public reinit() {
        try {
            this.gatherer = new DBGatherer(variables)
        } catch(e) {
            Log.error("An error occurred when creating the gatherer:", e)
            CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
            Log.error("Throwing error up chain")
            throw e
        }

        try {
            this.metricNames = gatherer.getMetricNames()

            if (this.metricNames == null ) {
                Log.info("No rows returned.  Assuming OK status.")
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "OK", [:], new Date(), "No rows returned.  Assuming OK status."))
            }
        } catch(e) {
            Log.error("An error occurred when running the SQL to find metric names:", e)
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when running the SQL to find metric names!"))
            Log.error("Throwing error up chain")
            throw e
        }

    }


    public chk_db_metric() {
        this.required += ["host", "database", "user", "password", "sql", "driver", "url"]
        init()
        return this.chkMetrics()
    }


    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        CheckRegister.add("chk_db_metric", "DATABASE", this.getClass().getName())
    }


    /**
     * Run samples and averages by calling gatherer, then calculate status
     * If there is a status_value column, this will be compared against th_warn and th_crit
     * instead of metric_value.  Metric_value will still be submitted as performance info though.
     */
    private chkMetrics() {

        def avgMessage = ""
        def values = []
        def status = "UNKNOWN"
        String allMessages = ""
        def allComparisons = []
        def performance = [:]
        def variables1 = variables.clone()

        // If we are looking for average values
        if (variables1.timePeriodMillis != null ){

            Log.info("Retrieving average results over ${variables1.timePeriodMillis}")
            avgMessage = "(average over ${variables1.timePeriodMillis} ms)"
            this.metricNames.each {
                variables1.identifier = it
                def calc = this.gatherer.avg(it, variables1)
                Log.debug("Avg value: $calc was read.")
                if (gatherer.getMessageForMetric(it) != null ) { allMessages += gatherer.getMessageForMetric(it) }
                if (gatherer.getStatusForMetric(it) != null ) { allComparisons += gatherer.getStatusForMetric(it) }
                values.add(calc)
                performance.put((it),"${calc};$chk_th_warn;$chk_th_crit;;")
                Log.debug("Performance entries: $performance")
            }

        // No average values, just single readings
        } else {
            this.metricNames.each {
                variables1.identifier = it
                def calc = this.gatherer.sample(it, variables1)
                Log.debug("Single value: $calc was read.")
                if (gatherer.getMessageForMetric(it) != null ) { allMessages += gatherer.getMessageForMetric(it) }
                if (gatherer.getStatusForMetric(it) != null ) { allComparisons += gatherer.getStatusForMetric(it) }
                values.add(calc)
                performance.put((it),"${calc};$chk_th_warn;$chk_th_crit;;")
                Log.debug("Performance entries: $performance")
            }
        }
        def message="DB Check: $allMessages METRIC_VALUES: $values MESSAGES: $avgMessage STATUS_VALUES(if any): $allComparisons"
        Log.debug(message)

        if ( allComparisons.size > 0 ) {
            status = calculateStatus(chk_th_warn, chk_th_crit, allComparisons, chk_th_type)
        } else {
            status = calculateStatus(chk_th_warn, chk_th_crit, values, chk_th_type)
        }
        this.gatherer.results = null

        if (this.metricNames.size() == 0) {
            Log.info("No rows were returned.")
            status = "OK"
            message = "No rows were returned for query - assuming OK status."
        }
        return super.generateResult(this.initiatorID, variables1.nagiosServiceName, variables1.host, status, performance, new Date(), message)
    }
	
}

