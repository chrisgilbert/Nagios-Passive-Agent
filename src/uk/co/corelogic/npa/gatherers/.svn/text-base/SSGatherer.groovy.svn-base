package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*
import groovy.sql.Sql


class SSGatherer extends Gatherer {

    def conn
    String result

    def variables
    def host
    def database
    def port
    def user
    def password
    def statsCached
    def dataFilesCached
    def logFilesCached

    // Define some useful SQL statements here

    // This lists any pending I/O requests.
    // The result set will be empty if there are none
    def SQL_PENDING_IO="""
        select
        database_id,
        file_id,
        io_stall,
        io_pending_ms_ticks,
        scheduler_address
        from    sys.dm_io_virtual_file_stats(NULL, NULL)t1,
        sys.dm_io_pending_io_requests as t2
        where    t1.file_handle = t2.io_handle
    """

    // These are the top 10 slowest queries on the server
    def SQL_TOP10_QUERIES="""
        use master;
        SELECT TOP 10 execution_count, max_elapsed_time/1000/1000 as "max_elapsed_time_secs", total_elapsed_time/1000/1000 as "total_elapsed_time_secs", (total_elapsed_time/execution_count)/1000/1000 as "avg_elapsed_time",
        SUBSTRING(st.text, (qs.statement_start_offset/2)+1,
        ((CASE qs.statement_end_offset
          WHEN -1 THEN DATALENGTH(st.text)
         ELSE qs.statement_end_offset
         END - qs.statement_start_offset)/2) + 1) AS statement_text
        FROM sys.dm_exec_query_stats AS qs
        CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) AS st
        ORDER BY total_elapsed_time/execution_count DESC
     """

     // This is a list of any blocking processes running
     def SQL_BLOCKING_PROCS="""
        use master;
        select count(*)
        from sys.sysprocesses qs
        CROSS APPLY sys.dm_exec_sql_text(qs.sql_handle) AS st
        where blocked > 0
     """

    // A list of lots of useful metrics
    // Parameters required: instance_name, counter_name   
    // Counter names:
    //Data File(s) Size (KB)
    //Log File(s) Size (KB)
    //Log File(s) Used Size (KB)
    //Percent Log Used
    //Active Transactions
    //Transactions/sec
    //Repl. Pending Xacts
    //Repl. Trans. Rate
    //Log Cache Reads/sec
    //Log Cache Hit Ratio
    //Log Cache Hit Ratio Base
    //Bulk Copy Rows/sec
    //Bulk Copy Throughput/sec
    //Backup/Restore Throughput/sec
    //DBCC Logical Scan Bytes/sec
    //Shrink Data Movement Bytes/sec
    //Log Flushes/sec
    //Log Bytes Flushed/sec
    //Log Flush Waits/sec
    //Log Flush Wait Time
    //Log Truncations
    //Log Growths
    //Log Shrinks
    //Cache Hit Ratio
    //Cache Hit Ratio Base
    //Cache Entries Count
    //Cache Entries Pinned Count
    def SQL_PERF_METRICS="""
        SELECT perf1.object_name, perf1.counter_name, perf1.instance_name,
        'value' = CASE perf1.cntr_type
        WHEN 537003008 -- This counter is expressed as a ratio and requires calculation.
        THEN CONVERT(FLOAT,
        perf1.cntr_value) /
        (SELECT CASE perf2.cntr_value
        WHEN 0 THEN 1
        ELSE perf2.cntr_value
        END
        FROM master..sysperfinfo perf2
        WHERE (perf1.counter_name + ' '
        = SUBSTRING(perf2.counter_name,
        1,
        PATINDEX('% Base%', perf2.counter_name)))
        AND (perf1.instance_name = perf2.instance_name)
        AND (perf2.cntr_type = 1073939459))
        ELSE perf1.cntr_value -- The values of the other counter types are
        -- already calculated.
        END
        FROM master..sysperfinfo perf1
        WHERE (perf1.cntr_type <> 1073939459) -- Don't display the divisors.
        and instance_name=?
        and counter_name=?
        """

        // Page life expectancy - a good indication of the health of buffer cache
        // Microsoft recommend over 300, but into the thousands is good.
        def SQL_PAGE_LIFE="""
            use master;  
            select cntr_value from sysperfinfo
            where counter_name='Page life expectancy'
            and object_name='SQLServer:Buffer Manager'
        """

        // Some basic data file stats
        def SQL_DATA_FILES ="""
        select
	[FILE_SIZE_MB] =
		sum(convert(decimal(12,2),round(a.size/128.000,2))),
	[SPACE_USED_MB] =
		sum(convert(decimal(12,2),round(fileproperty(a.name,'SpaceUsed')/128.000,2))),
	[FREE_SPACE_MB] =
		sum(convert(decimal(12,2),round((a.size-fileproperty(a.name,'SpaceUsed'))/128.000,2)))
        from
	dbo.sysfiles a
        where groupid <> 0
        """

        // Some basic log file stats
        def SQL_LOG_FILES ="""
        select
	[FILE_SIZE_MB] =
		sum(convert(decimal(12,2),round(a.size/128.000,2))),
	[SPACE_USED_MB] =
		sum(convert(decimal(12,2),round(fileproperty(a.name,'SpaceUsed')/128.000,2))),
	[FREE_SPACE_MB] =
		sum(convert(decimal(12,2),round((a.size-fileproperty(a.name,'SpaceUsed'))/128.000,2)))
        from
	dbo.sysfiles a
        where groupid = 0
        """

    boolean initialised = false

    public SSGatherer(host, port, database, user, password, initiatorID) {
        this.host = host
        this.database = database
        this.port = port
        this.user = user
        this.password = password
        this.conn = new JDBCConnection(host, port, database, user, password, "ss").connection
        this.initiatorID = initiatorID

        // Check for nulls
        assert this.host != null, 'Host cannot be null!'
        assert this.port != null, 'Port cannot be null!'
        assert this.database != null, 'Database cannot be null!'
        assert this.user  != null, 'Username cannot be null!'
        assert this.password != null, 'Password cannot be null!'

        this.registerMetrics()
    }

    public SSGatherer(variables) {
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

    public SSGatherer() {
        super()
    }

    private init(variables) {
        this.host = variables.host
        this.database = variables.database
        this.conn = new JDBCConnection(variables.host, variables.port, variables.database, variables.user, variables.password, "ss").connection
        this.initiatorID = variables.initiatorID
        this.initialised = true
        this.registerMetrics()
    }

     /*
     * Create a new metric model for a given name, which stores a double
     */
    private MetricModel getDoubleModel(String name, String identifier){
        MetricModel mod = new MetricModel()
        mod.setMetricName(name)
        mod.setMetricType("SS")
        mod.setMetricDataType("Double")
        mod.setIdentifier(identifier)
        mod.setInstanceName(this.database);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)
        return mod
    }

    /*
     * A helper method to persist a metric which should be stored as a Double
     */
    private void persistDouble(String name, String identifier, value) {
        def datestamp = MetricsDB.getNewDateTime()
        def mod = getDoubleModel(name, identifier)
        this.persistMetric(mod, value.toDouble(), datestamp)
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

        try {
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
        } catch(e) {
            Log.error("An exception occurred when running SQL:", e)
        }
        return results
    }

        /**
         * Register thy metrics
        */
       public void registerMetrics() {
            this.metricList.add('SS_STATS_BC_HIT_RATIO')
            this.metricList.add('SS_STATS_BC_PAGE_LIFE')
            this.metricList.add('SS_STATS_NUM_ACT_TRANS')
            this.metricList.add('SS_STATS_TRANS_PER_SEC')
            //this.metricList.add('SS_STATS_LC_HIT_RATIO')
            //this.metricList.add('SS_STATS_NUM_CACHE_ENTRIES')
            //this.metricList.add('SS_STATS_NUM_PIN_CA_ENTRIES')
            this.metricList.add('SS_DATA_FILES_TOTAL')
            this.metricList.add('SS_LOG_FILES_TOTAL')
            this.metricList.add('SS_LOG_FILES_USED')
            this.metricList.add('SS_DATA_FILES_USED')
            this.metricList.add('SS_DATA_FILES_FREE')
            this.metricList.add('SS_LOG_FILES_FREE')
            this.metricList.add('SS_LOG_FILES_PCT_USED')
            //this.metricList.add('SS_DATA_FILES_PCT_USED')
            this.metricList.add('SS_NUM_BLOCKING_PROCS')
            super.addValidMetricList(this.metricList, 'SS', this.getClass().getName())
        }

    public SS_STATS_BC_HIT_RATIO(variables) {
        def ret = ssGetStat(variables, "Cache Hit Ratio")
        this.persistDouble("SS_STATS_BC_HIT_RATIO", null, ret.toDouble())
        return ret
    }

    public SS_STATS_NUM_ACT_TRANS(variables) {
        def ret = ssGetStat(variables, "Active Transactions")
        this.persistDouble("SS_STATS_NUM_ACT_TRANS", null, ret.toDouble())
        return ret
    }

    public SS_STATS_TRANS_PER_SEC(variables) {
        def ret = ssGetStat(variables, "Transactions/sec")
        this.persistDouble("SS_STATS_TRANS_PER_SEC", null, ret.toDouble())
        return ret
    }
    

    /**
     * Generic helper method for all cache statistics.  This is called from other
     * methods to return a particular statistic as specified
    */
    private ssGetStat(variables, statName) {
        if ( this.statsCached != null ) {
            return statsCached[statName]
        } else {
            this.statsCached = getMetricRowResults(SQL_PERF_METRICS, [this.database, statName])
            return statsCached.value[0]
        }
    }

    public SS_STATS_BC_PAGE_LIFE(variables) {
        def ret = getSingleMetricString(SQL_PAGE_LIFE, null).toDouble()
        this.persistDouble("SS_STATS_BC_PAGE_LIFE", null, ret)
        return ret
    }


    public SS_DATA_FILES_TOTAL(variables) {
        if ( this.dataFilesCached == null ) {
            this.dataFilesCached = getMetricRowResults(SQL_DATA_FILES, null)
        }
        def ret = dataFilesCached.file_size_mb[0]
        this.persistDouble("SS_DATA_FILES_TOTAL", null, ret)
        return ret
    }
    public SS_DATA_FILES_FREE(variables) {
        if ( this.dataFilesCached == null ) {
            this.dataFilesCached = getMetricRowResults(SQL_DATA_FILES, null)
        }
        def ret = dataFilesCached.free_space_mb[0]
        this.persistDouble("SS_DATA_FILES_FREE", null, ret)
        return ret
    }

    public SS_DATA_FILES_USED(variables) {
        if ( this.dataFilesCached == null ) {
            this.dataFilesCached = getMetricRowResults(SQL_DATA_FILES, null)
        }
        def ret = dataFilesCached.space_used_mb[0]
        this.persistDouble("SS_DATA_FILES_USED", null, ret)
        return ret
    }

    public SS_LOG_FILES_USED(variables) {
        if ( this.logFilesCached == null ) {
            this.logFilesCached = getMetricRowResults(SQL_LOG_FILES, null)  
        }
        def ret = logFilesCached.space_used_mb[0]
        this.persistDouble("SS_LOG_FILES_USED", null, ret)
        return ret
    }

    public SS_LOG_FILES_FREE(variables) {
        if ( this.logFilesCached == null ) {
            this.logFilesCached = getMetricRowResults(SQL_LOG_FILES, null)
        }
        def ret = logFilesCached.free_space_mb[0]
        this.persistDouble("SS_LOG_FILES_FREE", null, ret)
        return ret
    }

    public SS_LOG_FILES_TOTAL(variables) {
        if ( this.logFilesCached == null ) {
            this.logFilesCached = getMetricRowResults(SQL_LOG_FILES, null)
        }
        def ret = logFilesCached.file_size_mb[0]
        this.persistDouble("SS_LOG_FILES_TOTAL", null, ret)
        return ret
    }
    
    public SS_LOG_FILES_PCT_USED(variables) {
        def result = ( this.SS_LOG_FILES_USED() / this.SS_LOG_FILES_TOTAL() * 100 )
        this.persistDouble("SS_LOG_FILES_PCT_USED", null, result.toDouble())
        return result
    }

    public SS_DATA_FILES_PCT_USED(variables) {
        def result = ( this.SS_DATA_FILES_USED() / this.SS_DATA_FILES_TOTAL() * 100 )
        this.persistDouble("SS_DATA_FILES_PCT_USED", null, result.toDouble())
        return result
    }


    public SS_NUM_BLOCKING_PROCS(variables) {
        def result = getSingleMetricString(SQL_BLOCKING_PROCS, null).toDouble()
        this.persistDouble("SS_NUM_BLOCKING_PROCS", null, result.toDouble())
        return result
    }


    /**
     * Method to retrieve a list of SQL Agent jobs, along with their run details.
    */
    public getJobs() {
        
    }


}