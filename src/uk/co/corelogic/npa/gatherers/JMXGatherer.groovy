package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import groovy.sql.Sql
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*
import java.lang.management.*
import java.lang.management.remote.*
import java.lang.management.remote.api.*
import oracle.oc4j.admin.jmx.remote.api.JMXConnectorConstant
import oracle.oc4j.admin.jmx.remote.api.*
import javax.management.ObjectName
import javax.management.remote.*


class JMXGatherer extends Gatherer {


String result
String initiatorID
def metrics = [:]
def metricList = []
def groupID = [:]
def sharedMetRes = [:]

String host
String port
String username
String instance
String password

def instanceName
def groupName

def conn
def j2eeServer
def jvm
def managementServer
def instanceServer
def managementServerMbean
def instanceServerMbean
def serverMbean
def jvmMbean
def url

def env, serviceUrl, serverPath, jvmPath, managementServerUrl, managementServerMbeanPath, instanceMbeanPath

    public JMXGatherer() {
        super()
    }


    public init(env, serviceUrl, serverPath, jvmPath, managementServerUrl, managementServerMbeanPath, instanceMbeanPath) {
            this.env = env
            this.serviceUrl = serviceUrl
            this.serverPath = serverPath
            this.jvmPath = jvmPath
            this.managementServerUrl = managementServerUrl
            this.managementServerMbeanPath = managementServerMbeanPath
            this.instanceMbeanPath = instanceMbeanPath
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


    public connect() {

        Log.debug("Connecting to JMX URL: $serviceUrl")
        def serverUrl = new JMXServiceURL(serviceUrl)

        def server = JMXConnectorFactory.connect(serverUrl, env).MBeanServerConnection

        this.managementServer = JMXConnectorFactory.connect(managementServerUrl, env).MBeanServerConnection
        //def configInfo = new GroovyMBean(opmnServer, opmnConfigPath)
        //this.opmnconfig = configInfo

        this.managementServerMbean = getMbean(managementServerMbeanPath) // OPMN Path
        this.instanceMbean = getMbean(instanceMbeanPath) //OC4J Path

        def serverMbean = getMbean(serverPath)
        def jvmMbean = getMbean(jvmPath)
        this.j2eeServer = serverMbean
        this.jvm = jvmMbean

        println """Connected to $serverInfo.node. \
        Server started ${new Date(serverInfo.startTime)}.
        App Server Version: $serverInfo.serverVersion from $serverInfo.serverVendor
        JVM version:   $jvmInfo.javaVersion from $jvmInfo.javaVendor
        Memory usage:  $jvmInfo.freeMemory bytes free, \
        $jvmInfo.totalMemory bytes total
        """
        return server
    }


    public GroovyMBean getMbean(mbeanPath) {
        return new GroovyMBean(this.managementServer, mbeanPath)
    }


    public GroovyMBean getInstanceMbean(mbeanPath) {
        return new GroovyMBean(this.j2eeServer, mbeanPath)
    }





/*
public jvmMemoryChart() {
    def piedata = new org.jfree.data.general.DefaultPieDataset()
    piedata.setValue "Free", jvm.freeMemory
    piedata.setValue "Used", jvm.totalMemory - jvm.freeMemory

    def options = [true, true, true]
    def chart = ChartFactory.createPieChart('OC4J JVM Memory Usage', piedata, *options)
    chart.backgroundPaint = java.awt.Color.white
    def swing = new groovy.swing.SwingBuilder()
    def frame = swing.frame(title:'OC4J Memory Usage', defaultCloseOperation:WC.EXIT_ON_CLOSE) {
        panel(id:'canvas') { rigidArea(width:350, height:250) }
    }
    frame.pack()
    frame.show()
    chart.draw(swing.canvas.graphics, swing.canvas.bounds)
}
*/



public getVMs() {
   return this.j2eeServer.JavaVMs
}

public getNode() {
   return this.j2eeServer.node
}


public listStartupProperties() {
   this.j2eeServer.getPersistentProperties()
}


}

