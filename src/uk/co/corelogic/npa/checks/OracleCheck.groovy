package uk.co.corelogic.npa.checks
import groovy.sql.Sql
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.Log
import uk.co.corelogic.npa.gatherers.*

/**
 * @deprecated
 * This class is deprecated in favour of the generic DBCheck
 */

@Deprecated
class OracleCheck extends Check {

String conn
String result
String host
String port
String database
String user
String password


public init(variables1) {
    this.variables = variables1
    this.host = variables1.host
    this.port = variables1.port
    this.database = variables1.database
    this.user = variables1.user
    this.password = variables1.password

    // Check for nulls
    assert this.host != null, 'Host cannot be null!'
    assert this.port != null, 'Port cannot be null!'
    assert this.database != null, 'Database cannot be null!'
    assert this.user  != null, 'Username cannot be null!'
    assert this.password != null, 'Password cannot be null!'

    this.initiatorID  = UUID.randomUUID();
    variables1.initiatorID = this.initiatorID
    try {
        this.gatherer = new OracleGatherer(variables1)
    } catch(e) {
        Log.error("An error occurred when creating the gatherer:", e)
        CheckResultsQueue.add(super.generateResult(this.initiatorID, variables1.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
        Log.error("Throwing error up chain")
        throw e
    }
    Log.debug("Gatherer initiator ID is ${this.initiatorID}")
}
    synchronized public OracleCheck clone() {
        OracleCheck clone = (OracleCheck) super.makeClone(this.chk_name);
        clone.conn = this.conn;
        clone.result = this.result;
        clone.host = this.host;
        clone.port = this.port;
        clone.database = this.database;
        clone.user = this.user;
        clone.password = this.password;
        clone.instance = this.database;
        return clone;
    }

    OracleCheck(args) {
        init(args)
    }
    OracleCheck() {
        super()
    }

    public reinit() {
                try {
                    this.gatherer = new OracleGatherer(this.variables)
                } catch(e) {
                    Log.error("An error occurred when creating the gatherer:", e)
                    CheckResultsQueue.add(super.generateResult(this.initiatorID, this.variables.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
                    Log.error("Throwing error up chain")
                    throw e
                }
    }

// We need a method which takes a list as an argument for each check type
public chk_tablespace() {
    init(this.chk_args)
    return this.chkTablespace(this.chk_args.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
}
public chk_buffer_cache() {
    init(this.chk_args)
    return this.chkBufferCache(this.chk_args.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
}

/**
 * Register all the checks which this class implements
*/
public registerChecks() {
    CheckRegister.add("chk_tablespace", "ORACLE", this.getClass().getName())
    CheckRegister.add("chk_buffer_cache", "ORACLE", this.getClass().getName())
}


// Check how full a tablespace or tablespaces are based on thresholds
private chkTablespace(variables1, th_warn, th_crit, th_type) {
reinit()
def maxstatus="UNKNOWN"
def message=""
def performance = [:]
def tablespaces = []
def tablespaceRes = []
def avgMessage = ""


    // Get samples of required metrics
    if ( variables1.tablespace_name == "ALL" || !variables1.tablespace_name ) {
        Log.info("Requested all tablespaces.  Looping through each.")
        tablespaces = this.gatherer.getTablespaceNames()
        tablespaces.each {
            def newVars = variables1.clone()
            newVars[('tablespace_name')] = it.name
            Log.debug("Variables being passed: " + newVars)
            def tbsResult = [:]
            tbsResult[('name')] = it.name

            if (newVars.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${newVars.timePeriodMillis}")
                avgMessage = "(average over ${variables.timePeriodMillis} ms)"
                tbsResult[('max')] = this.gatherer.avg("ORA_TBS_MB_TOTAL", newVars).toFloat()
                tbsResult[('used')] = this.gatherer.avg("ORA_TBS_MB_USED", newVars).toFloat()
                tbsResult[('free')] = this.gatherer.avg("ORA_TBS_MB_FREE", newVars).toFloat()
            } else {
                tbsResult[('max')] = this.gatherer.sample("ORA_TBS_MB_TOTAL", newVars).toFloat()
                tbsResult[('used')] = this.gatherer.sample("ORA_TBS_MB_USED", newVars).toFloat()
                tbsResult[('free')] = this.gatherer.sample("ORA_TBS_MB_FREE", newVars).toFloat()
            }
            tablespaceRes.add(tbsResult)
        }
    } else {
        Log.info("Requested tablespace ${variables1.tablespace_name}.")
        def tbsResult = [:]
        tbsResult[('name')] = variables1.tablespace_name
        if (variables1.timePeriodMillis != null ){
            Log.info("Retrieving average results over ${variables1.timePeriodMillis}")
            avgMessage = "(average over ${variables.timePeriodMillis} ms)"
            tbsResult[('max')] = this.gatherer.avg("ORA_TBS_MB_TOTAL", variables1).toFloat()
            tbsResult[('used')] = this.gatherer.avg("ORA_TBS_MB_USED", variables1).toFloat()
            tbsResult[('free')] = this.gatherer.avg("ORA_TBS_MB_FREE", variables1).toFloat()
        } else {
            tbsResult[('max')] = this.gatherer.sample("ORA_TBS_MB_TOTAL", variables1).toFloat()
            tbsResult[('used')] = this.gatherer.sample("ORA_TBS_MB_USED", variables1).toFloat()
            tbsResult[('free')] = this.gatherer.sample("ORA_TBS_MB_FREE", variables1).toFloat()
        }
            tablespaceRes.add(tbsResult)
    }
        def valuesArray = []
	tablespaceRes.each {

		def status
		
                // Calculate percentage free
                def calc = (it.free/it.max)*100

                // Round to 2 decimal places.
                BigDecimal bd = new BigDecimal(calc);
                bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                def pct_free = bd.doubleValue();
                Log.debug("${it.name} - pct free: $pct_free")

                def value
                if (variables1.unitType == null ) { variables1.unitType = "percent" }

                if (variables1.unitType == "megabytes") { 
                    value = it.free
                    // Gather performance figures
                    performance[(it.name)]="${it.free}MB;$th_warn;$th_crit;;"
                }
                else { 
                    value = pct_free
                    // Gather performance figures
                    performance[(it.name)]="${it.free}%;$th_warn;$th_crit;;"
                }

                valuesArray.add(value)

                // Now check for threshold levels
                status = super.calculateStatus(th_warn, th_crit, value, th_type)
                Log.debug("Sub status for ${it.name} is $status")

		// Set the status level for the check at the highest warning level for all tablespaces
		//if (status == "WARNING" && maxstatus != "CRITICAL") { maxstatus = "WARNING" }
		//if (status == "CRITICAL") { maxstatus = "CRITICAL" }
                //if (status == "OK" && maxstatus != "CRITICAL" && maxstatus != "WARNING") { maxstatus = "OK" }
                message = message + ";" + it.name + " free: $value ${variables1.unitType} $avgMessage - status: $status"
	}
        
    maxstatus = calculateStatus(th_warn, th_crit, valuesArray, th_type)
        
    Log.debug("Status for all tablespace checks is $maxstatus. $message")
    Log.debug(message)
    this.gatherer.disconnect()
    this.gatherer = null;
    return super.generateResult(this.initiatorID, this.variables.nagiosServiceName, this.host, maxstatus, performance, new Date(), message)
}


public chkBufferCache(variables1, th_warn, th_crit, th_type) {
reinit()

    def avgMessage = ""
    def calc
    if (variables1.timePeriodMillis != null ){
        Log.info("Retrieving average results over ${variables1.timePeriodMillis}")
        avgMessage = "(average over ${variables.timePeriodMillis} ms)"
        calc = this.gatherer.avg("ORA_STATS_BUFFER_CACHE_HIT_RATIO", variables1).toFloat()
    } else {
        calc = this.gatherer.sample("ORA_STATS_BUFFER_CACHE_HIT_RATIO", variables1).toFloat()
    }
    def status = "UNKNOWN"

    def performance=[('buffer_cache_pct'):"${calc}%;$th_warn;$th_crit;;"]

    // Round to 2 decimal places.
    BigDecimal bd = new BigDecimal(calc);
    bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    def pct_val = bd.doubleValue();

    def message="Results of buffer cache check: $pct_val % $avgMessage"
    Log.debug(message)

    // Now check for threshold levels
    status = super.calculateStatus(th_warn, th_crit, pct_val, th_type)

    this.gatherer.disconnect()
    this.gatherer = null
    return super.generateResult(this.initiatorID, this.variables.nagiosServiceName, this.host, status, performance, new Date(), message)
}

/*

chk_long_running (th, th_type) {


}



chk_no_processes (th) {

}


chk_blocking (th, ) {

}


chk_arc_log () {

}


chk_log_shipping () {

}


// For supplied SQL statements
public chk_sql_value (sql, th_warn, th_crit, th_type) {


}
*/

}
