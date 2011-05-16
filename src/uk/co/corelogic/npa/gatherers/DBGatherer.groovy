/*
 * Generic database gatherer which requires a url string, and driver, username and password to connect to any JDBC compliant DB
 * the JDBC driver must be in the classpath of NPA to function correctly
 *
 */

package uk.co.corelogic.npa.gatherers
import groovy.sql.Sql
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.*

/**
 * Gatherer class for generic databases accessed via JDBC
 * @author chris
 */
class DBGatherer extends Gatherer {

    String result
    def variables
    def url
    def host
    def driver
    def user
    def password
    boolean initialised = false
    def results
    def sql
    def database
    def conn

    // Prevent instantiation without arguments
    public DBGatherer () { }
    
    public DBGatherer(variables) {
        super()
       
        this.url = variables.url
        this.driver = variables.driver
        this.user = variables.user
        this.password = variables.password   
        this.host = variables.host
        this.variables = variables
        this.sql = variables.sql
        this.database = variables.database

        // Check for nulls
        assert this.url != null, 'JDBC URL cannot be null!'
        assert this.driver != null, 'JDBC driver class cannot be null!'
        assert this.user  != null, 'Username cannot be null!'
        assert this.password != null, 'Password cannot be null!'
        assert this.sql != null, 'SQL statement cannot be null!'

        conn(this.url, this.driver, this.user, this.password)

    }


    private void conn(url, driver, username, password) {
    Log.debug("Connecting to database: $database")
    this.conn = Sql.newInstance(url, user,
                     password, driver)

    }

    public void dc() {
        this.conn.close();
    }

    public void disconnect() {
        this.conn.close();
    }

    /*
     * Create a new metric model for a given name which stores a string
     */
    private MetricModel getDBMetricModel(){
        MetricModel mod = new MetricModel()
        mod.setMetricName(null)
        mod.setMetricType("DATABASE")
        mod.setMetricDataType("String")
        mod.setIdentifier(null)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)
        mod.setGroupID(getMetricGroup("DB_METRICS", this.metricList).groupID)
        return mod
    }

     /*
      * Return a new metric group created with specified groupName
      */
     private MetricGroup getMetricGroup(String name, metricList) {
        MetricGroup grp = new MetricGroup(name, this.initiatorID, metricList)
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
     * This method is used for all check queries
    */
    private getMetricRowResults(variables) {

        def results = []
        Log.debug("Executing statement: " + sql + " with values: " + variables)

        if ( ! variables ) {
            this.conn.eachRow(sql) {
                Log.debug(it)
                results << it.toRowResult()
            }
        } else {
            this.conn.eachRow(sql, variables) {
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
     * This is a dummy method, as metrics are registered dynamically by this class
    */
    private registerMetrics() {
    }

    /**
     * Register a metric using the SQL values
     */
    private registerMetric(metricName) {
        this.metricList.add(metricName)
        super.addValidMetricList(this.metricList, "DATABASE", this.getClass().getName())
    }

    /**
     * Returns resultset with all metrics gathered
     */
    protected getRowResults() {
        def results = getMetricRowResults(null)
        def mod = getDBMetricModel()
        saveMetrics(mod, results)
        return results
    }

    /*
     * Return any metric by name as a String
    */
    public String getSingleMetric(metricName) {
        if ( results == null ) {
           validateQuery()
           this.results = getRowResults()
        }
        return this.results.find { it.METRIC_IDENTIFIER == metricName }.METRIC_VALUE
    }

    /*
     * Return the description message from the query for a given metric
    */
    public getMessageForMetric(metricName) {
        if ( this.results[0].containsKey('MESSAGE')) {

            String messages = ""
            
            this.results.each {
                if  ( it.METRIC_IDENTIFIER == metricName ) {
                  Log.debug("Message found: $it.MESSAGE")
                  messages += it.MESSAGE
                }
            }
            return messages;
        } else {
            return null
        }
    }

    /*
     * Return the check status comparsion value from the query for a given metric (if this column exists)
    */
    public getStatusForMetric(metricName) {
        if ( this.results[0].containsKey('STATUS_VALUE')) {

            def status = []

            this.results.each {
                if  ( it.METRIC_IDENTIFIER == metricName ) {
                  Log.debug("Status value found: $it.STATUS_VALUE")
                  status += it.STATUS_VALUE
                }
            }
            return status;
        } else {
            return null
        }
    }

    /**
     * Run a query which gets the names of the metrics being retrieved by the query
     */
    public getMetricNames() {
        def results = getMetricRowResults(null)
        def metricNames = []
        results.each {
            metricNames.add(it.METRIC_IDENTIFIER)
            registerMetric(it.METRIC_IDENTIFIER)
        }
        return metricNames
    }

    /*
     * Validate the query results to ensure it contains the correct columns
    */
     public void validateQuery() {
         def results = getMetricRowResults(null)
         assert results.size() != 0, "0 rows returned by query!"
         
         if ( ! results[0].containsKey('METRIC_IDENTIFIER') ) {
             throw new NPAException("Does not contain METRIC_IDENTIFIER column in SQL Output!")
         }
         if ( ! results[0].containsKey('METRIC_VALUE')) {
             throw new NPAException("Does not contain METRIC_VALUE column in SQL Output!")
         }
        
     }

    /*
     * Save metric(s) to the database and return row results
    */
    private saveMetrics(mod, results) {
        def value
        Log.debug("Metric Row results: $results")

        results.each {
            mod.metricName = it.METRIC_IDENTIFIER
            mod.identifier = it.METRIC_IDENTIFIER

            if ( it.containsKey('MESSAGE') ) {
                mod.description = it.MESSAGE
            }
            value = it.METRIC_VALUE
            def met = persistMetric(mod, value, MetricsDB.getNewDateTime())
        }
        return results
    }    

}

