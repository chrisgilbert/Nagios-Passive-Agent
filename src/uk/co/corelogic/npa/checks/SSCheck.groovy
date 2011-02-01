package uk.co.corelogic.npa.checks
import groovy.sql.Sql
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.Log
import uk.co.corelogic.npa.gatherers.*

/*
 * This class provides checks for SQL Server 2005.
*/

class SSCheck extends Check {

String conn
String result
String host
String port
String database
String user
String password


/*
 * This method initialises the connection to the database and creates a new gatherer to
 * be used for subsequent checks
 */
public init() {
    //this.variables = this.chk_args
    this.host = this.chk_args.host
    this.port = this.chk_args.port
    this.database = this.chk_args.database
    this.user = this.chk_args.user
    this.password = this.chk_args.password

    // Check for nulls
    assert this.host != null, 'Host cannot be null!'
    assert this.port != null, 'Port cannot be null!'
    assert this.database != null, 'Database cannot be null!'
    assert this.user  != null, 'Username cannot be null!'
    assert this.password != null, 'Password cannot be null!'

    this.initiatorID  = UUID.randomUUID();
    this.chk_args.initiatorID = this.initiatorID
    try {
        this.gatherer = new SSGatherer(this.chk_args.clone())
    } catch(e) {
        Log.error("An error occurred when creating the gatherer:", e)
        CheckResultsQueue.add(super.generateResult(this.initiatorID, this.chk_args.nagiosServiceName, this.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a DB connection!"))
        Log.error("Throwing error up chain")
        throw e
    }
    Log.debug("Gatherer initiator ID is ${this.initiatorID}")
}
    synchronized public SSCheck clone() {
        SSCheck clone = (SSCheck) super.makeClone(this.chk_name);
        clone.conn = this.conn;
        clone.result = this.result;
        clone.host = this.host;
        clone.port = this.port;
        clone.database = this.database;
        clone.user = this.user;
        clone.password = this.password;
        return clone;
    }

        SSCheck() {
            super()
        }

    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        CheckRegister.add("chk_ss_blocking_procs", "SS", this.getClass().getName())
        CheckRegister.add("chk_ss_data_files_free", "SS", this.getClass().getName())
    }


    public chk_ss_blocking_procs(){
        init()
        assert this.chk_args.nagiosServiceName != null, "nagiosServiceName is a required parameter!"
        int value = this.gatherer.sample("SS_NUM_BLOCKING_PROCS", this.chk_args.clone())
        def performance = ["SS_NUM_BLOCKING_PROCS":"$value;${this.chk_th_warn};${this.chk_th_crit};;"]

        // Now check for threshold levels
        def status = super.calculateStatus(this.chk_th_warn, this.chk_th_crit, value, this.chk_th_type)
        def message = "Number of blocking processes: " + value
        Log.info(message)

        Log.debug("Status is $status - $message")
        Log.debug(message)
        this.gatherer = null;
        
        return super.generateResult(this.initiatorID, this.chk_args.nagiosServiceName, this.host, status, performance, new Date(), message)
    }

    public chk_ss_data_files_free() {
        init()

        assert this.chk_args.nagiosServiceName != null, "nagiosServiceName is a required parameter!"
        assert this.chk_args.unitType != null, "unitType is a required parameter!"
        double free = this.gatherer.SS_DATA_FILES_FREE(this.chk_args.clone())
        double used = this.gatherer.SS_DATA_FILES_USED(this.chk_args.clone())
        double total = this.gatherer.SS_DATA_FILES_TOTAL(this.chk_args.clone())
        double pct_used = this.gatherer.SS_DATA_FILES_PCT_USED(this.chk_args.clone())
        double pct_free = (100 - pct_used)
        def performance = ["SS_DATA_FILES_PCT_USED":"${pct_used}%;${this.chk_th_warn};${this.chk_th_crit};;"]

        def comp_val

        if ( this.chk_args.unit_type == "megabytes" ) {
            comp_val = free
        } else {
            comp_val = pct_free
        }


        // Now check for threshold levels
        def status = super.calculateStatus(this.chk_th_warn, this.chk_th_crit, comp_val, this.chk_th_type)
        def message = "Datafiles - total: $total used: $used MB free: $free pct free: $pct_free"
        Log.info(message)

        Log.debug("Status is $status - $message")
        Log.debug(message)
        this.gatherer = null;

        return super.generateResult(this.initiatorID, this.chk_args.nagiosServiceName, this.host, status, performance, new Date(), message)
    }

}