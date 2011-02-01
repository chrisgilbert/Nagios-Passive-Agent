/*
 * Generic database gatherer which requires a url string, and driver, username and password to connect to any JDBC compliant DB
 * the JDBC driver must be in the classpath of NPA to function correctly
 *
 */

package uk.co.corelogic.npa.gatherers
import groovy.sql.Sql
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.Log

/**
 *
 * @author chris
 */
class DBGatherer {

    String result
    def variables
    def url
    def host
    def driver
    def user
    def password
    def connection
    boolean initialised = false

    // Prevent instantiation without arguments
    public DBGatherer () { }
    
    public DBGatherer(variables) {
        super()
       
        this.url = variables.url
        this.driver = variables.driver
        this.user = variables.user
        this.password = variables.password
        this.conn = conn(url, driver, user, password)
        
        this.host = "" //get it here

        // Check for nulls
        assert this.url != null, 'JDBC URL cannot be null!'
        assert this.driver != null, 'JDBC driver class cannot be null!'
        assert this.user  != null, 'Username cannot be null!'
        assert this.password != null, 'Password cannot be null!'
    }


    private void conn(url, driver, username, password) {
    Log.debug("Connecting to database: $database")
    this.connection = Sql.newInstance(url, user,
                     password, driver)

    }

    public void dc() {
        this.connection.close();
    }


    /*
     * Create a new metric model for a given name, which stores a double
     */
    private MetricModel getDoubleModel(String name, String identifier){
        MetricModel mod = new MetricModel()
        mod.setMetricName(name)
        mod.setMetricType("DATABASE")
        mod.setMetricDataType("Double")
        mod.setIdentifier(identifier)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)
        return mod
    }

     /*
      * Return a new metric group created with specified groupName
      */
     private MetricGroup getMetricGroup(String name, groupList) {
        MetricGroup grp = new MetricGroup(name, this.initiatorID, groupList)
        return grp
     }


    /**
     * Return a single value as a string from the database
    */
    private String getSingleMetricString(stmt, variables) {
        Log.debug("Executing statement: " + stmt + " with values: " + variables)
        def row
        if ( ! variables ) {
            row = this.conn.firstRow(stmt)
        } else { row = this.conn.firstRow(stmt, variables) }
        Log.debug("Row result: $row")
        return row[0].toString()
    }

    /**
     * Return a collection of rows as GroovyRowResult
    */
    private getMetricRowResults(stmt, variables) {

        def results = []
        Log.debug("Executing statement: " + stmt + " with values: " + variables)

        if ( ! variables ) {
            this.conn.eachRow(stmt) {
                Log.debug(it)
                results << it.toRowResult()
            }
        } else {
            this.conn.eachRow(stmt, variables) {
                Log.debug(it)
                results << it.toRowResult()
            }
        }
        Log.debug("Row results: $results")
        return results
    }

    
    /**
     * Register a list of metrics which are provided by this gatherer
     *
     * To add new metrics, they should have their names registered here.
    */
    private registerMetrics() {
        this.metricList.add('DB_SINGLE_METRIC')
        this.metricList.add('DB_MULTI_METRIC')
        super.addValidMetricList(this.metricList, "DATABASE", this.getClass().getName())
    }

    /**
     * Run a query which returns standard columns
     */
    private DB_SINGLE_METRIC(variables) {
        return getSingleMetricString(variables.sql, variables)
    }

    /**
     * Run a query which returns standard columns
     */
    private DB_MULTI_METRIC(variables) {
        return getSingleMetricString(variables.sql, variables)
    }

}

