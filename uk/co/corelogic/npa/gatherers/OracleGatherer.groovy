package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import groovy.sql.Sql
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*


class OracleGatherer extends Gatherer   {

def conn
String result

def variables
def host
def database
def port
def user
def password
def tbsMetricList = ["ORA_TBS_MB_FREE", "ORA_TBS_MB_USED", "ORA_TBS_MB_TOTAL"]

def sharedMetRes = [:]
boolean initialised = false

final String tbs_sql="""
        select df.tablespace_name
        ,sum(max_size_mb-total_used_mb) as ORA_TBS_MB_FREE
        ,sum(max_size_mb) as ORA_TBS_MB_TOTAL
        ,sum(total_size_mb) as CURRENT_DF_SIZE_MB
        ,sum(total_used_mb) as ORA_TBS_MB_USED
        ,(sum(total_used_mb)/sum(max_size_mb))*100 as PCT_USED
        FROM (select distinct df1.file_id, (decode(df1.autoextensible, 'NO', nvl(df1.bytes,1), 'YES', maxbytes)/1024/1024) as max_size_mb,
        (nvl(df1.bytes,1)/1024/1024) as total_size_mb,
        ((nvl(df1.bytes,0)-nvl(fr1.bytes,0))/1024/1024) as total_used_mb,
        df1.autoextensible as auto_ext
        from dba_data_files df1
        left outer join
        (select sum(bytes) bytes, file_id from dba_free_space group by file_id) fr1
        on df1.file_id = fr1.file_id) calc, dba_data_files df
        where tablespace_name like ?
        and df.file_id = calc.file_id
        group by tablespace_name
        order by tablespace_name
        """

    OracleGatherer(host, port, database, user, password, initiatorID) {
        this.host = host
        this.database = database
        this.port = port
        this.user = user
        this.password = password
        this.conn = new JDBCConnection(host, port, database, user, password, "oracle").connection
        this.initiatorID = initiatorID

        // Check for nulls
        assert this.host != null, 'Host cannot be null!'
        assert this.port != null, 'Port cannot be null!'
        assert this.database != null, 'Database cannot be null!'
        assert this.user  != null, 'Username cannot be null!'
        assert this.password != null, 'Password cannot be null!'

        this.registerMetrics()
    }

    OracleGatherer(variables) {
            this.variables = variables
            this.host = variables.host
            this.port = variables.port
            this.database = variables.database
            this.user = variables.user
            this.password = variables.password
            this.initiatorID  = variables.initiatorID

            // Check for nulls
            assert this.host != null, 'Host cannot be null!'
            assert this.port != null, 'Port cannot be null!'
            assert this.database != null, 'Database cannot be null!'
            assert this.user  != null, 'Username cannot be null!'
            assert this.password != null, 'Password cannot be null!'

            this.registerMetrics()
            init(this.variables)
    }

    OracleGatherer() {
        super()
    }
    
    private init(variables) {
        this.host = variables.host
        this.database = variables.database
        this.conn = new JDBCConnection(variables.host, variables.port, variables.database, variables.user, variables.password, "oracle").connection
        this.initiatorID = variables.initiatorID
        this.initialised = true
        this.registerMetrics()
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
        this.metricList.add('ORA_TBS_MB_FREE')
        this.metricList.add('ORA_TBS_MB_USED')
        this.metricList.add('ORA_TBS_MB_TOTAL')
        this.metricList.add('ORA_STATS_BUFFER_CACHE_HIT_RATIO')
        super.addValidMetricList(this.metricList, "ORACLE", this.getClass().getName())
    }


    private getTablespaceNames() {
        def sql = "select name from v\$tablespace where name not like ?"
        return getMetricRowResults(sql, ["TEMP%"])
    }


    private getTablespaceMetrics(mod, grp) {
        def value
        def results = getMetricRowResults(this.tbs_sql, [mod.identifier.toString()])
        Log.debug("TBS Row results: $results")
        mod.metricName = "ORA_TBS_MB_FREE"
        value = results.ORA_TBS_MB_FREE[0].toDouble()
        def met = persistMetric(mod, value, MetricsDB.getNewDateTime())

        mod.metricName = "ORA_TBS_MB_USED"
        value = results.ORA_TBS_MB_USED[0].toDouble()
        def met2 = persistMetric(mod, value, MetricsDB.getNewDateTime())

        mod.metricName = "ORA_TBS_MB_TOTAL"
        value = results.ORA_TBS_MB_TOTAL[0].toDouble()
        def met3 = persistMetric(mod, value, MetricsDB.getNewDateTime())

    }

    /**
     * Attempt to retrive a tablespace metric for group.  If it doesn't yet exist, then collect metrics for whole group
    */
    private getTBSMetric(grp, mod) {

    def val = grp.getMetric(mod.metricName)
    if ( val == null ) {
        Log.debug("No results yet recorded for ${mod.metricName} with group ID ${grp.groupID}")
        getTablespaceMetrics(mod, grp)
        def value = grp.getMetric(mod.metricName)
        return value
    } else {
        Log.debug("Results found for ${mod.metricName} with group ID ${grp.groupID} - value: $val")
        return val.toDouble()
    }

    }




    // This section implements the metrics


    private ORA_TBS_MB_FREE(variables) {
        // Configure standard metric variables
        def identifier = variables.tablespace_name

        // Create a MetricModel object and set the metric properties
        MetricModel mod = new MetricModel()
        mod.setMetricName("ORA_TBS_MB_FREE")
        mod.setMetricType("ORACLE")
        mod.setMetricDataType("Double")
        mod.setIdentifier(variables.tablespace_name)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)

        // Define group identifiers to ensure efficient database retrieval
        MetricGroup grp = new MetricGroup("ORATBS_" + mod.identifier, this.initiatorID, this.tbsMetricList)
        mod.setGroupID(grp.groupID)

        return getTBSMetric(grp, mod).toDouble()
        this.disconnect();
    }

    private ORA_TBS_MB_USED(variables) {
        // Configure standard metric variables
        def identifier = variables.tablespace_name

        // Create a MetricModel object and set the metric properties
        MetricModel mod = new MetricModel()
        mod.setMetricName("ORA_TBS_MB_USED")
        mod.setMetricType("ORACLE")
        mod.setMetricDataType("Double")
        mod.setIdentifier(variables.tablespace_name)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)

        // Define group identifiers to ensure efficient database retrieval
        MetricGroup grp = new MetricGroup("ORATBS_" + mod.identifier, this.initiatorID, this.tbsMetricList)
        mod.setGroupID(grp.groupID)

        return getTBSMetric(grp, mod).toDouble()
        this.disconnect();
    }

    private ORA_TBS_MB_TOTAL(variables) {
        // Configure standard metric variables
        def identifier = variables.tablespace_name

        // Create a MetricModel object and set the metric properties
        MetricModel mod = new MetricModel()
        mod.setMetricName("ORA_TBS_MB_TOTAL")
        mod.setMetricType("ORACLE")
        mod.setMetricDataType("Double")
        mod.setIdentifier(variables.tablespace_name)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)

        // Define group identifiers to ensure efficient database retrieval
        MetricGroup grp = new MetricGroup("ORATBS_" + mod.identifier, this.initiatorID, this.tbsMetricList)
        mod.setGroupID(grp.groupID)

        return getTBSMetric(grp, mod).toDouble()
        this.disconnect();
    }


    private double ORA_STATS_BUFFER_CACHE_HIT_RATIO(variables) {
        // Configure standard metric variables
        def identifier = null
        def datestamp = MetricsDB.getNewDateTime()

        // Create a MetricModel object and set the metric properties
        MetricModel mod = new MetricModel()
        mod.setMetricName("ORA_STATS_BUFFER_CACHE_HIT_RATIO")
        mod.setMetricType("ORACLE")
        mod.setMetricDataType("Double")
        mod.setIdentifier(identifier)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)

        def stmt="""\
        select
        100*(1 - (v3.value / (v1.value + v2.value))) "cache_hit_ratio"
        from
        v\$sysstat v1, v\$sysstat v2, v\$sysstat v3
        where
        v1.name = 'db block gets' and
        v2.name = 'consistent gets' and
        v3.name = 'physical reads'
        """

        def value = getSingleMetricString(stmt, [:])
        def met = persistMetric(mod, value.toDouble(), MetricsDB.getNewDateTime())
        return value.toDouble()
        this.disconnect();
    }


    private double ORA_CUSTOM_SQL(variables) {
        // Configure standard metric variables
        def identifier = variables.identifier
        def datestamp = MetricsDB.getNewDateTime()

        // Create a MetricModel object and set the metric properties
        MetricModel mod = new MetricModel()
        mod.setMetricName(variables.metricName)
        mod.setMetricType("ORACLE")
        mod.setMetricDataType(variables.metricDataType)
        mod.setIdentifier(identifier)
        mod.setInstance(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)

        def stmt=variables.sql

        def value = getSingleMetricString(stmt, [:])
        def met = persistMetric(mod, value.toDouble(), MetricsDB.getNewDateTime())
        return value.toDouble()
        this.disconnect();
    }

    private disconnect() {
        this.conn.close();
        this.initialised = false
    }







}