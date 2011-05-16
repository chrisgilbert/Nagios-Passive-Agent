package uk.co.corelogic.npa.gatherers
import groovy.sql.Sql
import oracle.jdbc.driver.OracleDriver
import uk.co.corelogic.npa.common.Log


/**
 * @deprecated
 * This class is deprecated in favour of the generic DBCheck changes
 */

@Deprecated
class JDBCConnection {

def connection

JDBCConnection(host, port, database, user, password, type) {

if (type.matches("oracle")) {
	oraConn(host, port, database, user, password)
} else {
	ssConn(host, port, database, user, password)
}

}

private void oraConn(host, port, database, user, password) {
    Log.debug("Connecting to database: $database")
	this.connection = Sql.newInstance("jdbc:oracle:thin:@$host:$port:$database", user,
                     password, "oracle.jdbc.driver.OracleDriver")
}

private void ssConn(host, port, database, user, password) {
    Log.debug("Connecting to database: $database")
    this.connection = Sql.newInstance("jdbc:jtds:sqlserver://${host}:${port}/${database}", user,
                     password, "net.sourceforge.jtds.jdbc.Driver")

}

public void dc() {
    this.connection.close();
}

}
