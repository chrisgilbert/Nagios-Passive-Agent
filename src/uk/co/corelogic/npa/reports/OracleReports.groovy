package uk.co.corelogic.npa.reports
import groovy.sql.Sql
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.reports.*
import uk.co.corelogic.npa.gatherers.*

class OracleReport extends Report {


String host
String port
String user
String password
String conn
String result
def methods

    OracleReport(rep_name, host, port, database, user, password) {
        this.rep_name = rep_name
        conn = new JDBCConnection(host, port, database, user, password, "oracle")
    }

    // Using Map of variables from FMD interface
    OracleReport(args) {
        this.rep_name = args.rep_name
        this.user = args.user
        this.password = args.password
        this.host = args.host
        this.database = args.database
        this.port = args.port
        try {
            conn = new JDBCConnection(args.host, args.port, args.database, args.user, args.password, "oracle")
        } catch (e) {
            e.printStackTrace
        }

    }


    public add_db_jobs_list() {
        def jobsList = []
        def jobsObj
        def sql="""\
        /* Get a list of jobs from the database - requires DBA view rights */
        select job, what, next_date, interval from dba_jobs
        """

        sql.eachRow(sql, tbs_name) {
                jobsObj << it.toRowResult()
        }

        jobsObj.each {
            def job=["job":it.job, "what":it.what, "next_date":it.next_date, "interval":it.interval]
            jobsList.add(job)
        }
        // Add performance figures to reports lists
        reportsLists["Database Jobs"] = tbsList

    }

    public add_tablespace_list() {

        def message=""
        def tbsList = []
        def tbsObj = []

        def sql
        sql="""\
        /* A more sophisticated way of deciding free tablespace, takes into account whether autoextend is on, or not.
           The file system space needs to be checked instead here, as the tablespace may be able to extend more, but that doesn't mean there is adequate disk space for that extension.  As such, pct_free represents the percentage free from oracle's point of view only, based on the maximum size of the underlying datafiles
        */
        select df.tablespace_name
        ,      sum(max_size_mb-total_used_mb) as free_space_mb
        ,      sum(max_size_mb) as max_size_mb
        ,      sum(total_size_mb) as current_size_mb
        ,      (sum(total_used_mb)/sum(max_size_mb))*100 as pct_used
        FROM (select distinct df1.file_id, (decode(df1.autoextensible, 'NO', nvl(df1.bytes,1), 'YES', maxbytes)/1024/1024) as max_size_mb,
        (nvl(df1.bytes,1)/1024/1024) as total_size_mb,
        ((nvl(df1.bytes,0)-nvl(fr1.bytes,0))/1024/1024) as total_used_mb,
        df1.autoextensible as auto_ext
        from dba_data_files df1
        left outer join
        (select sum(bytes) bytes, file_id from dba_free_space group by file_id) fr1
        on df1.file_id = fr1.file_id) calc, dba_data_files df
        where tablespace_name like '?'
        and df.file_id = calc.file_id
        group by tablespace_name
        order by tablespace_name
        """
            if (tbs_name == null) { tbs_name = '%' }
            sql.eachRow(sql, tbs_name) {
                    tbsObj << it.toRowResult()
            }


            tbsObj.tablespace_name.findAll(tbs_name).each {
                // Gather space figures
                def tbsDetail = ["Tablespace Name":it.tablespace_name, "Max Size (MB)":it.max_size_mb, "Current Size (MB)":it.current_size, "Free Space (MB)":it.pct_free]
                tbsList.add(tbsDetail)
            }

            // Add performance figures to reports lists
            reportsLists["Tablespace Summary"] = tbsList
    }

          

}