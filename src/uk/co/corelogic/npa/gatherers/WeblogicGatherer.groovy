package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import groovy.sql.Sql
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*
import javax.management.ObjectName
import javax.management.remote.JMXServiceURL
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXConnector
import javax.management.*
import javax.naming.Context



/**
 * A class to gather information from Weblogic Application Server via JMX
 * @author chris
 */
class WeblogicGatherer extends JMXGatherer {

    def applicationBasePath
    def domainName
    def serverName
    def applicationName
    def domainMbeanPath


    public WeblogicGatherer(){
        
    }
    public WeblogicGatherer(variables) {

        super()
        /*
         * Configure the parameters required to connect to OPMN and OC4J
         *
        */
        host = variables.host
        port = variables.port
        username = variables.username
        password = variables.password
        instance = variables.domain
        serverName = variables.serverName
        applicationName = variables.applicationName

        registerMetrics()
        def protocol = variables.jmxProtocol ?: "iiop"

        def domainRuntime = '/jndi/weblogic.management.mbeanservers.domainruntime'
        def urlRuntime = '/jndi/weblogic.management.mbeanservers.runtime'
        def urlBase = "service:jmx:$protocol://$host:$port"

        def domainUrl = urlBase + domainRuntime
        def runtimeUrl = urlBase + urlRuntime
        domainMbeanPath = "com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean"
        def serverServiceMbeanPath = "com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean,Location=$serverName"
        // A closure to work out the path to the ServerRuntime Mbean
        def serverMbeanPathClosure = { managementServer.getAttribute(new ObjectName(serverServiceMbeanPath), "ServerRuntime") }
        // Closure to work out the path to the JVM Information Mbean
        def jvmMbeanPathClosure = { "com.bea:Location=$serverName,ServerRuntime=$serverName,Name=$serverName,Type=JVMRuntime" }


        def env = new Hashtable()
        env.put(Context.SECURITY_PRINCIPAL, username)
        env.put(Context.SECURITY_CREDENTIALS, password)
        env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, 'weblogic.management.remote')
        env.put("jmx.remote.x.request.waiting.timeout", new Long(10000));

        super.connectManagement(env, domainUrl, domainMbeanPath)
        connectServerMbean(env, runtimeUrl, serverMbeanPathClosure, jvmMbeanPathClosure)
        println("Server ${j2eeServerInfo.Name}, state=${j2eeServerInfo.State}, version=${j2eeServerInfo.WeblogicVersion}")
    }

    /*
     * Connect to the J2EE MbeanServer - this method takes closures, to allow the Mbean path for the Container and JVM to be customised at runtime
     * at runtime.
     */
    public void connectServerMbean(env, j2eeServerUrl, Closure j2eeMbeanPathClosure, Closure jvmMbeanPathClosure) {
        try {
            this.j2eeEnv = env
            this.j2eeServerUrl = j2eeServerUrl
            this.j2eeServer = super.manConn.MBeanServerConnection
            this.j2eeMbeanPath = j2eeMbeanPathClosure().getCanonicalName()
            this.jvmMbeanPath = jvmMbeanPathClosure()
            this.j2eeServerInfo = getJ2EEMbean(j2eeMbeanPath)
            this.jvmInfo = getManagementMbean(jvmMbeanPath)
        } catch(e) {
            Log.error("Unable to connect to server Mbean!")
        }
    }


    /**
     * Register a list of metrics which are provided by this gatherer
     * These registered here are specific to Weblogic - others are also inherrited from JMXGatherer
    */
    public void registerMetrics() {
        super.registerMetrics()
    }


    public setJVMPaths() {
        def serverMbeans = this.ALLSERVERS.collect{ getMbean(it) }
        def vms = serverMbeans.collect { it.javaVMs }
        this.ALLJVMS = vms.flatten()
        Log.debug("All JVMS: " + ALLJVMS)
    }

    public setServerPaths() {
        def servers = getMbean("ias:j2eeType=J2EEDomain,name=ias").collect { it.servers }.flatten()
        this.ALLSERVERS = servers.findAll { it =~ instance }
        Log.debug("All J2EE Servers: " + ALLSERVERS)
    }



}

