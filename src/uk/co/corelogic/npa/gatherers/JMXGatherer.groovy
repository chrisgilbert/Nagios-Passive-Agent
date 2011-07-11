package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import groovy.sql.Sql
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*


class JMXGatherer extends Gatherer {

def conn
String result
String initiatorID
def metrics = [:]
def metricList = []
def groupID = [:]
def sharedMetRes = [:]


    JMXGatherer(host, port, oc4jInstance, user, password, initiatorID) {
        //this.conn = new jdbcConnection(host, port, database, user, password, "oracle").connection
        this.initiatorID = initiatorID
        this.registerMetrics()
        // Assign null group IDs
        this.groupID[('')] = null
        this.groupID[('STAT')] = null
    }

    public void finalize() {
        this.conn.close()
    }
    
    /**
    Register a list of metrics which are provided by this gatherer

    Add new valid metrics here
    */

    public void registerMetrics() {
        //this.metricList.add('MBEAN_VALUE')
    }

}