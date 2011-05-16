package uk.co.corelogic.npa.metrics
import groovy.sql.Sql
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.NPA

/**

This class contains methods for persisting metrics for disk in a SQLLite database,
and purging data approrpriately.

*/
static class MetricsDB {

     private MetricDB() { }

static config = NPA.getConfigObject()

static conn
static purgeInterval = config.npa.metrics_db_purge_days.toString()
static boolean connected

    public static void connect() {
        def location = new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/" + config.npa.metrics_db_location
        Log.info("Initialising MetricsDB connection..")
        conn = groovy.sql.Sql.newInstance("jdbc:sqlite:${location}","org.sqlite.JDBC")

        conn.execute('CREATE TABLE IF NOT EXISTS metrics(id integer primary key, initiatorID, groupID, hostName, instanceName, metricName, metricType, metricDataType, value, identifier, datestamp date)');
        if ( ! conn.firstRow("SELECT * from metrics").containsKey("description") ) {
            conn.execute("ALTER TABLE metrics ADD COLUMN description")
        }

        conn.execute('CREATE INDEX IF NOT EXISTS met_datestamp_indx on metrics(datestamp)')
        conn.execute('CREATE INDEX IF NOT EXISTS met_grp_indx on metrics(groupID)')
        conn.execute('CREATE INDEX IF NOT EXISTS met_ident_indx on metrics(identifier)')
        conn.execute('CREATE INDEX IF NOT EXISTS met_inst_indx on metrics(instanceName)')
        conn.execute('CREATE INDEX IF NOT EXISTS met_ident_inst_indx on metrics(identifier, instanceName)')
        conn.execute('CREATE INDEX IF NOT EXISTS met_init_indx on metrics(initiatorID)')
        conn.execute('CREATE INDEX IF NOT EXISTS met_name_indx on metrics(metricName)')
        conn.execute('CREATE TABLE IF NOT EXISTS logPositions(id integer primary key, fileName, lineNumber, lastAccessed)')
        conn.execute('CREATE INDEX IF NOT EXISTS log_filename_indx on logPositions(fileName)')
        conn.execute('CREATE TABLE IF NOT EXISTS groups(id integer primary key, groupName, initiatorID)')
        conn.execute('CREATE INDEX IF NOT EXISTS grp_name_indx on groups(groupName)')
        conn.execute('CREATE INDEX IF NOT EXISTS grp_init_indx on groups(initiatorID)')
        connected = true
    }

    public static int persistMetric(Metric m) {
        if ( ! connected ) { connect() }
        // Workaround for bug in SQLlite driver
        if ( m.value.class == Float ) {
            Log.debug("Converting metric value ${m.value} to Double, value m.value.toDouble()}")
            m.value = m.value.toDouble() }
        
        def keys = conn.executeUpdate("""
            INSERT INTO metrics(id, initiatorID, groupID, hostName, instanceName, metricName, metricType, metricDataType, value, identifier, description, datestamp)
            values (null, $m.initiatorID, $m.groupID, $m.hostName, $m.instanceName, $m.metricName, $m.metricType, $m.metricDataType, $m.value, $m.identifier, $m.description, $m.datestamp)
        """)
            return conn.firstRow("SELECT last_insert_rowid()")[0]
    }

    /**
     * Add a new group to the database
    */
    public static addGroup(groupName, initiatorID) {
        if ( ! connected ) { connect() }
        def keys = conn.executeUpdate(
            """INSERT INTO groups(id, groupName, initiatorID) values(null, ?, ?)""", [groupName, initiatorID])
        def ID = conn.firstRow("SELECT last_insert_rowid()")[0]
        Log.debug("Adding group with ID " + ID )
        return ID
    }

    /**
     * Get the group ID of an existing group
    */
    public static retrieveGroupID(groupName, initiatorID) {
        if ( ! connected ) { connect() }
        def result = conn.firstRow("SELECT ID from groups WHERE groupName = $groupName and initiatorID = $initiatorID")
        if ( result != null ) { return result[0] }
        else { return null }
    }


    public static void saveLogPosition(variables, pos) {
        if ( ! connected ) { connect() }
        def fileName = variables.filename
        def entry = conn.firstRow("SELECT * from logPositions WHERE fileName = $fileName")

        if ( entry != null ) {
            conn.executeUpdate("UPDATE logPositions SET lineNumber = ?", [pos])
        } else {
            conn.executeUpdate("""
                INSERT INTO logPositions(id, fileName, lineNumber, lastAccessed)
                values(null, ?, ?, ?)""", [fileName, pos, getNewDateTime()])
        }

        // Purge entries more than 24 hours old
        def oldDate = new Date()-1
        conn.executeUpdate("DELETE FROM logPositions WHERE lastAccessed < ?", [oldDate])
    }

    public static int retrieveLogPosition(variables) {
        if ( ! connected ) { connect() }
        def fileName = variables.filename
        def pos
        def entry = conn.firstRow("SELECT lineNumber FROM logPositions WHERE fileName = ?", [fileName])

        if ( entry != null ) {
            pos = entry.lineNumber
        } else {
            pos = 0
        }
        return pos
    }
    
    public static void  purgeMetrics() {
        if ( ! connected ) { connect() }
        Log.debug("Purging metrics data older than $purgeInterval days.")
        def stmt = /DELETE FROM groups where id in (select id from metrics WHERE datestamp < date('now','-/ + purgeInterval +/ day'))/
        conn.executeUpdate(stmt)
        conn.executeUpdate(/delete from metrics where groupID not in (select id from groups)/)
        conn.execute('vacuum')
    }

    /**
    * Retrieve a persisted metric value using it's ID
    */
    public static retrieveWithID(id) {
        if ( ! connected ) { connect() }
        Log.debug("Retrieving metric from DB with ID $id")
        def row = conn.firstRow("SELECT * from metrics WHERE id = $id")

        def value  = row.value
        assert value != null, "Metric cannot be retrieved!"
        return row
    }
    
    /**
     * Get the current date and time in a db compatible format
    */
    public static getNewDateTime() {
        return new Date().format('yyyy-MM-d HH:mm:ss.SSS')
    }

    /**
     * Subtract given number of milliseconds from the current time and return a SQL Lite timestamp
     */
    public static subtractFromNow(subtract) {
        Log.debug("Subtracting $subtract milliseconds")
        return new Date( new Date().time - Long.parseLong(subtract.toString().trim())).format('yyyy-MM-d HH:mm:ss.SSS')
    }


    /**
     * Search the database for a group ID in the current initiator, when you do not have group name
    */
    public static String findGroupID(initiatorID, identifier, instance) {
        if ( ! connected ) { connect() }
        Log.debug("Retrieving groupID from DB with initiator $initiatorID, identifier $identifier, instance $instance")
        def row = conn.firstRow("SELECT DISTINCT groupID from metrics WHERE initiatorID = $initiatorID AND instanceName = $instance AND identifier = $identifier")
        if ( row != null ) { return row.groupID }
        else { return null }
    }

    /**
     * Search the database for a metric with given groupID
    */
    public static String findGroupMetric(groupID, identifier, instance) {
        if ( ! connected ) { connect() }
        Log.debug("Retrieving metric value from DB with group $groupID, identifier $identifier, instance $instance")
        conn.firstRow("SELECT DISTINCT value from metrics WHERE groupID = ? AND instanceName = ? AND identifier = ?"
            , [groupID, identifier, instance]) {
            def value = it[0]
            assert value != null, "Metric value cannot be retrieved!"
            return value
        }
    }

    /**
     * Get available metrics for a given groupID
    */
    public static getAvailableMetrics(groupID) {
        if ( ! connected ) { connect() }
        Log.debug("Retrieving metric values from DB with group $groupID")
        def row = conn.select("SELECT * from metrics WHERE groupID = ?", [groupID])
        return row
    }

    /**
    * Get metric value for a given groupID and metricName
    */
    public static getGroupMetric(groupID, metricName) {
        if ( ! connected ) { connect() }
        Log.debug("Retrieving metric values from DB with group $groupID, metricName $metricName")
        def row = conn.firstRow("SELECT value from metrics WHERE groupID = ? and metricName = ?", [groupID, metricName])
        if ( row != null ) { return row.value }
        else { return null }
    }

  

    public static void deleteTestData() {
        if ( ! connected ) { connect() }
        Log.debug("Deleting all test metrics..")
        conn.executeUpdate("DELETE FROM metrics WHERE metricType='TEST'")
        conn.executeUpdate("DELETE FROM groups WHERE groupName='TESTGROUP'")
        conn.executeUpdate("DELETE FROM logpositions WHERE fileName='/tmp/test'")
    }

    /*
    * Get average metric value over time
    */
   public static getAvgMetricValue(metricName, variables, period) {
       if ( ! connected ) { connect() }
       assert variables.identifier != null, "Identifier must be specified for check!"

       Log.debug("Retrieving average metric value from DB with identifier ${variables.identifier}, metricName $metricName, host ${variables.host}, instance ${variables.instance}")
       def row
       if (variables.instance == null ) {
           row = conn.firstRow("SELECT avg(value) as value from metrics WHERE identifier = ? and metricName = ? and hostName = ? and instanceName is null and datestamp > ?", [variables.identifier, metricName, variables.host, subtractFromNow(period)])
       } else {
           row = conn.firstRow("SELECT avg(value) as value from metrics WHERE identifier = ? and metricName = ? and hostName = ? and instanceName = ? and datestamp > ?", [variables.identifier, metricName, variables.host, variables.instance, subtractFromNow(period)])
       }
       if ( row != null ) { return row.value }
       else { return null }

   }

}