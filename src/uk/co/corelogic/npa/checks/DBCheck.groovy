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
class DBCheck extends Check {

String conn
String result
String host
String port
String database
String user
String password
String instance
def metricNames = []


public init(variables1) {
    this.variables = variables1
    this.host = variables1.host
    this.database = variables1.database
    variables1.instance = variables1.database
    this.user = variables1.user
    this.password = variables1.password

    // Check for nulls
    assert this.host != null, 'Host cannot be null!'
    assert this.database != null, 'Database cannot be null!'
    assert this.user  != null, 'Username cannot be null!'
    assert this.password != null, 'Password cannot be null!'

    this.initiatorID  = UUID.randomUUID();
    variables1.initiatorID = this.initiatorID
    try {
        this.gatherer = new DBGatherer(variables1)
    } catch(e) {
        Log.error("An error occurred when creating the gatherer:", e)
        CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
        Log.error("Throwing error up chain")
        throw e
    }

    try {
        gatherer.validateQuery()
        this.metricNames = gatherer.getMetricNames()
        if (this.metricNames == null ) {
            Log.info("No rows returned.  Assuming OK status.")
            CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "OK", [:], new Date(), "No rows returned.  Assuming OK status."))
        }

    } catch(e) {
        Log.error("An error occurred when running the SQL to find metric names:", e)
        CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when running the SQL to find metric names!"))
        Log.error("Throwing error up chain")
        throw e
    }

    Log.debug("Gatherer initiator ID is ${this.initiatorID}")
}
    synchronized public DBCheck clone() {
        DBCheck clone = (DBCheck) super.makeClone(this.chk_name);
        clone.conn = this.conn;
        clone.result = this.result;
        clone.host = this.host;
        clone.database = this.database;
        clone.user = this.user;
        clone.password = this.password;
        clone.instance = this.database;
        return clone;
    }

    DBCheck(args) {
        init(args)
    }
    DBCheck() {
        super()
    }

    DBCheck(chk_name, th_warn, th_crit, th_type, variables) {
        super(chk_name, th_warn, th_crit, th_type, variables)
        init(variables)
    }

    public reinit() {
        try {
            this.gatherer = new DBGatherer(this.variables)
        } catch(e) {
            Log.error("An error occurred when creating the gatherer:", e)
            CheckResultsQueue.add(super.generateResult(this.initiatorID, this.variables.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
            Log.error("Throwing error up chain")
            throw e
        }

        try {
            this.metricNames = gatherer.getMetricNames()

            if (this.metricNames == null ) {
                Log.info("No rows returned.  Assuming OK status.")
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "OK", [:], new Date(), "No rows returned.  Assuming OK status."))
            }
        } catch(e) {
            Log.error("An error occurred when running the SQL to find metric names:", e)
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when running the SQL to find metric names!"))
            Log.error("Throwing error up chain")
            throw e
        }

    }


    public chk_db_metric() {
        init(this.variables)
        return this.chkMetrics(this.variables.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
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
    public chkMetrics(variables1, th_warn, th_crit, th_type) {

        def avgMessage = ""
        def values = []
        def status = "UNKNOWN"
        String allMessages = ""
        def allComparisons = []
        def performance = [:]

        // If we are looking for average values
        if (variables1.timePeriodMillis != null ){

            Log.info("Retrieving average results over ${variables1.timePeriodMillis}")
            avgMessage = "(average over ${variables.timePeriodMillis} ms)"
            this.metricNames.each {
                variables1.identifier = it
                def calc = this.gatherer.avg(it, variables1)
                Log.debug("Avg value: $calc was read.")
                if (gatherer.getMessageForMetric(it) != null ) { allMessages += gatherer.getMessageForMetric(it) }
                if (gatherer.getStatusForMetric(it) != null ) { allComparisons += gatherer.getStatusForMetric(it) }
                values.add(calc)
                performance.put((it),"${calc};$th_warn;$th_crit;;")
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
                performance.put((it),"${calc};$th_warn;$th_crit;;")
                Log.debug("Performance entries: $performance")
            }
        }
        def message="DB Check: $allMessages METRIC_VALUES: $values MESSAGES: $avgMessage STATUS_VALUES(if any): $allComparisons"
        Log.debug(message)

        if ( allComparisons.size > 0 ) {
            status = calculateStatus(th_warn, th_crit, allComparisons, th_type)
        } else {
            status = calculateStatus(th_warn, th_crit, values, th_type)
        }
        this.gatherer.disconnect()
        this.gatherer = null

        if (this.metricNames.size() == 0) {
            Log.info("No rows were returned.")
            status = "OK"
            message = "No rows were returned for query - assuming OK status."
        }
        return super.generateResult(this.initiatorID, this.variables.nagiosServiceName, this.host, status, performance, new Date(), message)
    }
	
}

