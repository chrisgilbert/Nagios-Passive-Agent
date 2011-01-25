/* Connect to SQL Server and find out the fwi databases present, along with some version details

Usage: get_database_versions_ss.gr [SERVER] [port] [username] [password] [output type (text/wiki)]


*/


import groovy.sql.Sql
import net.sourceforge.jtds.*
import uk.co.corelogic.npa.common.Log

class getSSDBVersions{

  static void main(String[] args) {
	final String driver = "net.sourceforge.jtds.jdbc.Driver"
	Log.Info("Connecting to master database..")
    	def master = Sql.newInstance("jdbc:jtds:sqlserver://" + args[0] + ":" + args[1] + ";DatabaseName=master", args[2], args[3], driver)

	Log.Info("Getting database list..")
	def AllDBs = getFWiDatabases(master, args[0], args[1], args[2], args[3], driver)


	Log.Info("h2. " + args[0] + " databases")
	def header = ["Database Name", "DB Version","FWi Version", "SP Version", "Reports Version", "Size (GB)", "Last Logon (fwi)"]
	header.each {
		print("|| ${it} ")
	}
	Log.Info("||")

	def dbconn

	AllDBs.each { db ->

		dbconn = Sql.newInstance("jdbc:jtds:sqlserver://" + args[0] + ":" + args[1] + ";DatabaseName=" + db, args[2], args[3], driver)

		def detail = [:]
		detail["Database Name"] = db
		detail["DB Version"] = getVersion(dbconn, "database schema")?: "Not installed"
		detail["FWi Version"] = getVersion(dbconn, "frameworki")?: "Not installed"
		detail["SP Version"] = getVersion(dbconn, "stored procedures")?: "Not installed"
		detail["Reports Version"] = getVersion(dbconn, "reports repository")?: "Not installed"
		detail["Size (GB)"] = getSize(dbconn)?: "Not found"
		detail["Last Logon (fwi)"] = getLastLogon(dbconn)?: "Not found"


		dbconn.close()


		if ( args[4] == "text" ) {

		detail.each{
			Log.Info(it.key + ":" + it.value)
		}


		}
		else if (args[4] == "wiki") {
			printWiki(detail)
		}
	}

	master.close()


  }



  static String getVersion(conn, type) {
	def result = null
	try {
		result = conn.firstRow("select component_version as c from running_configuration where component_control_id = (select id from component_control where component_name=${type})")?.c
		return result
	} catch(e) { return "Error retrieving version" }

  }

  static String getSize(conn) {
	try {
		def row = conn.firstRow("SELECT sum(CAST(size/128.0/1000 AS int)) as size FROM sysfiles").size ?: "Not found"
		return row
	 } catch(e) { return "Error retrieving size" }

  }
  static String getLastLogon(conn) {
	try {
		def row = conn.firstRow("select max(last_logon) as logon from workers where last_logon is not null").logon ?: "Not found"
		return row
 	} catch(e) { return "Error retrieving size" }

  }


  static List getFWiDatabases(conn, server, port, username, password, driver) {
	def DBs = []
	def dbconn
	def count
	conn.eachRow("select name from sysdatabases where mode=0") { db ->
		dbconn = Sql.newInstance("jdbc:jtds:sqlserver://" + server + ":" + port + ";DatabaseName=" + db.name, username, password, driver)
		count = dbconn.firstRow("select count(name) as count from sysobjects where name='people'")
		if ( count.count == 1 ) {
			DBs.add(db.name)
		}
		dbconn.close()
	}
	return DBs
  }

  static void printWiki(db) {
	db.each {
		print("| ${it.value} ")
	}
	Log.Info("|")
  }

}

