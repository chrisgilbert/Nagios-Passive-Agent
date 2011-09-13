package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import groovy.sql.Sql
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*
import oracle.oc4j.admin.jmx.remote.api.JMXConnectorConstant
import oracle.oc4j.admin.jmx.remote.api.*
import oracle.oc4j.admin.jmx.remote.*
import javax.management.ObjectName
import javax.management.remote.JMXServiceURL
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXConnector


/**
 * A class to gather information from Oracle Application Server via JMX
 * @author chris
 */
class OASGatherer extends JMXGatherer {

    def opmnConfigMBean


    public OASGatherer(){
        
    }
    public OASGatherer(variables) {

        super()
        /*
         * Configure the parameters required to connect to OPMN and OC4J
         *
        */
        host = variables.host
        port = variables.port
        username = variables.username
        password = variables.password
        instance = variables.instance
        registerMetrics()

   
        def provider = 'oracle.oc4j.admin.jmx.remote'

        def credentials = [
            (JMXConnectorConstant.CREDENTIALS_LOGIN_KEY): username,
            (JMXConnectorConstant.CREDENTIALS_PASSWORD_KEY): password
        ]
        def env = [
            (JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES): provider,
            (JMXConnector.CREDENTIALS): credentials
        ]

        def managementServerUrl = "service:jmx:rmi:///opmn://$host:$port/cluster"
        def managementServerMbeanPath = "ias:j2eeType=J2EEDomain,name=ias"

        j2eeServerUrl = "service:jmx:rmi:///opmn://$host:$port/$instance"
        j2eeMbeanPath = "oc4j:j2eeType=J2EEServer,name=standalone"
        jvmMbeanPath = "oc4j:j2eeType=JVM,name=single,J2EEServer=standalone"
        

        super.connectManagement(env, managementServerUrl, managementServerMbeanPath)
        super.connectJ2EE(env, j2eeServerUrl, j2eeMbeanPath, jvmMbeanPath)
    }



    /**
    Register a list of metrics which are provided by this gatherer

    These registered here are specific to Oracle Application Server - others are also inherrited from JMXGatherer
    */

    public void registerMetrics() {
        super.registerMetrics()
        this.metricList.add('OAS_HTTP_SESSIONS')
        this.metricList.add('OAS_OC4J_STATE')
        super.addValidMetricList(this.metricList, "JMX", this.getClass().getName())
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
        Log.debug("Getting iAS Instance Name")
        def data =  getManagementMbean("ias:j2eeType=J2EEDomain,name=ias").servers.find { it =~ instance }.toString()[4..-1]

        // Make a map using the Mbean path string to find an appropriate value
        def map = [:]
        data.split(",").each {param ->
            def nameAndValue = param.split("=")
            map[nameAndValue[0]] = nameAndValue[1]
        }
        return map.ASInstance
    }

    public getOC4JGroupName() {
        Log.debug("Getting OC4J group Name")
        def data = getManagementMbean("ias:j2eeType=J2EEDomain,name=ias").servers.find { it =~ instance }.toString()[4..-1]

        // Make a map using the Mbean path string to find an appropriate value
        def map = [:]
        data.split(",").each {param ->
            def nameAndValue = param.split("=")
            map[nameAndValue[0]] = nameAndValue[1]
        }
        return map.J2EEServerGroup
    }



    public getStateString() {
       def stateList = [0:"STARTING",1:"RUNNING",2:"STOPPING",3:"STOPPED",4:"FAILED"]
       return stateList.get(getManagementMbean("ias:j2eeType=J2EEServer,name=$instance,J2EEServerGroup=${getOC4JGroupName()},ASInstance=${getIASInstanceName()}").state)
    }


    public getState() {
       return getManagementMbean("ias:j2eeType=J2EEServer,name=$instance,J2EEServerGroup=${getOC4JGroupName()},ASInstance=${getIASInstanceName()}").state
    }

    /*
     * This returns the path to the ORACLE_HOME as a String
    */
    public getOracleHome() {
       return this.j2eeServer.oracleHome
    }

    /*
     * Return an integer count of all the sessions on the container.  This covers all deployed applications.
    */
    public getCountHTTPSessions() {
       Log.debug("Getting HTTP Sessions value.")
       def value = queryJ2EEMbean("oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone", "HttpSessions").split("\n").find { it =~ "Total Sessions" }.tokenize(" ").find{ it ==~ /^\d+/ }
       Log.debug(value)
       return value
    }

    public getLogDir() {
       return this.jvmInfo[0].getproperty("framework.log.dir")
    }

    public installSharedLibrary(file, name, version, source) {
       this.j2eeServer.installSharedLibrary(file, name, version, source)
    }

    public checkSharedLibraryExists(name, version) {
       this.j2eeServer.checkSharedLibraryExists(name, version)
    }

    public stop() {
       this.j2eeServer.stop()
    }

    public start() {
       this.j2eeServer.start()
    }

    public retrieveJVMs() {
        this.jvmInfo = getJVMPaths().collect { getJ2EEMbean(it) }
    }

    public getJVMPaths() {
        return this.j2eeServer.javaVMs
    }

}

