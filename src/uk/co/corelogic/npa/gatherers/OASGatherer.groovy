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

/**
 * A class to gather information from Oracle Application Server via JMX
 * @author chris
 */
class OASGatherer extends JMXGatherer {
	
    public OASGatherer(variables) {

        super()
        /*
         * Configure the parameters required to connect to OPMN and OC4J
         *
        */
        def host = variables.host
        def port = variables.port
        def username = variables.username
        def password = variables.password
        def instance = variables.instance

    
        def serverPath = 'oc4j:j2eeType=J2EEServer,name=standalone'
        def jvmPath = 'oc4j:j2eeType=JVM,name=single,J2EEServer=standalone'
        def provider = 'oracle.oc4j.admin.jmx.remote'
        def serviceUrl = "service:jmx:rmi:///opmn://$host:$port/$instance"

        def credentials = [
            (JMXConnectorConstant.CREDENTIALS_LOGIN_KEY): username,
            (JMXConnectorConstant.CREDENTIALS_PASSWORD_KEY): password
        ]
        def env = [
            (JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES): provider,
            (JMXConnector.CREDENTIALS): credentials
        ]

        // Connect to OPMN managementServer to get further functionality
        def managementServerUrl = new JMXServiceURL("service:jmx:rmi:///opmn://$host:$port/cluster")
        def opmnConfigPath='ias:type=OpmnConfig'
        def managementServerMbeanPath = "ias:j2eeType=J2EEDomain,name=ias"
        //def instanceMbeanPath = "ias:j2eeType=J2EEServer,name=$instance,J2EEServerGroup=${getOC4JGroupName()},ASInstance=${getIASInstanceName()}"

        init(env, serviceUrl, serverPath, jvmPath, managementServerUrl, managementServerMbeanPath, instanceMbeanPath)
    }


    public listJMSQueues() {
        def query = new javax.management.ObjectName('oc4j:*')
        def allNames = conn.queryNames(query, null)
        def dests1 = allNames.findAll{ name ->
            name.toString().contains('j2eeType=JMSDestinationResource')
        }
        def dests = dests1.collect{ new GroovyMBean(conn, it) }

        println("Found ${dests.size()} JMS destinations. Listing ...")
        dests.each{ d -> println "$d.name: $d.location" }
    }

    public getIASInstanceName() {
        def query = new javax.management.ObjectName('ias:*')
        def allNames = managementServer.queryNames(query, null)
        //println(allNames)
        def dests1 = allNames.findAll{ name ->
            name.toString().contains('ias:type=OpmnConfig,name=')
        }
        println dests1
        def dests = dests1.collect{ new GroovyMBean(managementServer, it) }
        return dests[0].iasInstanceName
    }

    public getOC4JGroupName() {
        def query = new javax.management.ObjectName('ias:*')
        def allNames = managementServer.queryNames(query, null)
        //println(allNames)
        def dests1 = allNames.findAll{ name ->
            name.toString().contains("ias:j2eeType=J2EEServer,name=$instance,J2EEServerGroup=")
        }
        println dests1
        def dests = dests1.collect{ new GroovyMBean(managementServer, it) }
        def group = dests[0].name().toString().tokenize(',').get(2).tokenize('=').get(1)
        return group
    }

    public getState() {
       def stateList = [0:"STARTING",1:"RUNNING",2:"STOPPING",3:"STOPPED",4:"FAILED"]
       return stateList.get(this.opmnOC4J.state)
    }

    public getOracleHome() {
       return this.j2eeServer.oracleHome
    }

    public getLogDir() {
       return this.jvm.getproperty("framework.log.dir")
    }

    public installSharedLibrary(file, name, version, source) {
       this.j2eeServer.installSharedLibrary(file, name, version, source)
    }

    public checkSharedLibraryExists(name, version) {
       this.j2eeServer.installSharedLibrary(name, version)
    }

    public stop() {
       this.opmnOC4J.stop()
    }

    public start() {
       this.opmnOC4J.start()
    }

}

