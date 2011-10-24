package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import groovy.sql.Sql
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*
//import java.lang.management.*
//import java.lang.management.remote.*
//import java.lang.management.remote.api.*
import javax.management.ObjectName
import javax.management.remote.JMXServiceURL
import javax.management.remote.JMXConnectorFactory
import java.util.concurrent.*


class JMXGatherer extends Gatherer {


String result

String host
String port
String username
String instance
String password

def instanceName
//def groupName

def conn, j2eeConn, manConn
def managementServer
def managementServerInfo
def j2eeServer
def j2eeServerInfo
def jvmServer = []
def jvmInfo = []
def ALLSERVERS
def ALLJVMS

def env, j2eeEnv, serviceUrl, j2eeMbeanPath, j2eeServerUrl, jvmMbeanPath, managementServerUrl, managementServerMbeanPath, instanceMbeanPath

    public JMXGatherer() {
        super()
        registerMetrics()
    }


    /**
    Register a list of metrics which are provided by this gatherer

    Add new valid metrics here
    */

    public void registerMetrics() {
        this.metricList.add('JMX_ATTR_VALUE')
        this.metricList.add('JMX_OPER_VALUE')
        super.addValidMetricList(this.metricList, 'JMX', this.getClass().getName())
    }


    public JMX_ATTR_VALUE(variables)
    {
        Log.debug("Received variables: $variables")
        variables.collect { it.value = it.value.toString() }

        MetricModel mod = getJMXMetricModel()
        mod.metricName = "JMX_ATTR_VALUE"
        mod.identifier = variables.identifier
        mod.description = "JMX Attribute Value"
        
        def value
        def extraPath = ""
        if ( variables.mbeanPath.startsWith("ALLSERVERS") ) {
            if ( variables.mbeanPath.tokenize(",")?.size() > 1 ) {
                extraPath = variables.mbeanPath.tokenize(",")[1]
            }
            value = this.ALLSERVERS.collect { getMbeanAttributeValue(it + extraPath, variables.closureFunction, variables.attributeName) }
        } else if ( variables.mbeanPath.startsWith("ALLJVMS") ) {
            if ( variables.mbeanPath.tokenize(",")?.size() > 1) {
                extraPath =  + variables.mbeanPath.tokenize(",")[1]
            }
            value = this.ALLJVMS.collect { getMbeanAttributeValue(it + extraPath, variables.closureFunction, variables.attributeName) }
        } else {
            value = getMbeanAttributeValue(variables.mbeanPath, variables.closureFunction, variables.attributeName)
        }
        
        value.each { saveMetric(mod, it) }

        if (value.size() == 1) {
            return value[0].clone()
        } else {
            return value.clone()
        }
    }

    public JMX_OPER_VALUE(variables)
    {
        Log.debug("Received variables: $variables")
        variables.collect { it.value = it.value.toString() }

        MetricModel mod = getJMXMetricModel()
        mod.metricName = "JMX_OPER_VALUE"
        mod.identifier = variables.identifier
        mod.description = "JMX Operation Value"
        def value = []
        def extraPath

        if ( variables.mbeanPath.startsWith("ALLSERVERS") ) {
            if ( variables.mbeanPath.tokenize(",")?.size() > 1 ) {
                extraPath = variables.mbeanPath.tokenize(",")[1].replace("oc4j:", "")
            }
            value = this.ALLSERVERS.collect { executeMbeanOperation(it + "," + extraPath, variables.operationName, variables.closureFunction, variables.collectionOperator, variables.operationArguments) }
        } else if ( variables.mbeanPath.startsWith("ALLJVMS") ) {
            if ( variables.mbeanPath.tokenize(",")?.size() > 1 ) {
                extraPath = variables.mbeanPath.tokenize(",")[1].replace("oc4j:", "")
            }
            value = this.ALLJVMS.collect { executeMbeanOperation(it + "," + extraPath, variables.operationName, variables.closureFunction, variables.collectionOperator, variables.operationArguments) }
        } else {
            value =  executeMbeanOperation(variables.mbeanPath, variables.operationName, variables.closureFunction, variables.collectionOperator, variables.operationArguments)
        }

        value.each { saveMetric(mod, it) }

        if (value.size() == 1) {
            return value[0]
        } else {
            return value
        }
    }

    void finalize() {
        this.disconnect()
        env = null
        j2eeEnv = null
        serviceUrl = null
        j2eeMbeanPath = null
        j2eeServerUrl = null
        jvmMbeanPath = null
        managementServerUrl = null
        managementServerMbeanPath = null
        instanceMbeanPath = null
        conn = null
        j2eeConn = null
        manConn = null
        managementServer = null
        managementServerInfo = null
        j2eeServer = null
        j2eeServerInfo = null
        jvmServer = null
        jvmInfo = null
        ALLSERVERS = null
        ALLJVMS = null
        result = null
        host = null
        port = null
        username = null
        instance = null
        password = null
        instanceName = null
    }


    void disconnect() {
        Log.debug("Closing open connections to JMX..")
        try {
            this.j2eeConn?.close()
            this.manConn?.close()
            //this.j2eeServer.close()
            //this.managementServer.close()
            this.managementServer = null
            this.j2eeServer = null
            this.conn = null
            this.j2eeConn = null
            this.manConn = null
            this.managementServerInfo = null
            this.j2eeServerInfo = null
            this.jvmServer = null
            this.jvmInfo = null
            this.ALLSERVERS = null
            this.ALLJVMS = null

        } catch(e) {
            Log.error("Failed to disconnect from JMX", e)
            this.managementServer = null
            this.j2eeServer = null
            this.conn = null
            this.j2eeConn = null 
            this.manConn = null
            this.managementServerInfo = null
            this.j2eeServerInfo = null
            this.jvmServer = null
            this.jvmInfo = null
            this.ALLSERVERS = null
            this.ALLJVMS = null
            throw e
        }
    }

    /*
     * Connect to a management mbean server, such as OPMN for OC4J
     */
    public void connectManagement(env, managementServerUrl, String managementServerMbeanPath) {
        try {
            this.env = env
            this.managementServerUrl = managementServerUrl
            this.managementServerMbeanPath = managementServerMbeanPath
            Log.debug("Connecting to JMX Management URL: $managementServerUrl")
            def jmxUrl = new JMXServiceURL(managementServerUrl)
            this.manConn = connectURLOrTimeout(jmxUrl, env)
            this.managementServer = manConn.MBeanServerConnection
            this.managementServerInfo = getManagementMbean(managementServerMbeanPath)
        } catch(e) {
            Log.error("ERROR: Unable to connect to JMX Management Mbean Server")
        }
    }

    /*
     * Add a management mbean at runtime, where a seperate connection is not required
     */
    public void setManagementMbean(Closure managementServerMbeanClosure){
         this.managementServerMbeanPath = managementServerMbeanClosure()
         this.managementServerInfo = getManagementMbean(this.managementServerMbeanPath)
         Log.debug("Connected to management Mbean.")
         Log.debug("Server: name=$managementServerInfo.Name, state=$managementServerInfo.State, version=$managementServerInfo.WeblogicVersion")
    }

    /*
     * Connect to a J2EE (container level) Mbean server.  This would be an OC4J instance, or Weblogic server.
     */
    public void connectJ2EE(env, j2eeServerUrl, j2eeMbeanPath, String jvmMbeanPath) {
        try {
            this.j2eeEnv = env
            this.j2eeServerUrl = j2eeServerUrl
            this.jvmMbeanPath = jvmMbeanPath
            this.j2eeMbeanPath = j2eeMbeanPath
            Log.debug("Connecting to JMX Management URL: $j2eeServerUrl")
            def jmxUrl = new JMXServiceURL(j2eeServerUrl)
            this.j2eeConn = connectURLOrTimeout(jmxUrl, env)
            
            this.j2eeServer = j2eeConn.MBeanServerConnection
            
            this.j2eeServerInfo = getJ2EEMbean(j2eeMbeanPath)

        } catch(e) {
            Log.error("ERROR: Unable to connect to JMX J2EE Mbean Server")
        }
    }
    

    /*
     * Print some J2EE and JVM information. OAS ONLY
     */
    public void printJ2EEInfo(){
        Log.info("""Connected to $j2eeServerInfo.node. \
        Server started ${new Date(j2eeServerInfo.startTime)}.
        App Server Version: $j2eeServerInfo.serverVersion from $j2eeServerInfo.serverVendor
        JVM version:   $jvmInfo.javaVersion from $jvmInfo.javaVendor
        Memory usage:  $jvmInfo.freeMemory bytes free, \
        $jvmInfo.totalMemory bytes total
        """)
    }

    /*
     * Get a J2EE Mbean attribute using the attribute name and Mbean path as Strings
     */
    public getMbeanAttributeValue(mbeanPath, String c, attributeName) {
        Log.debug("Getting Mbean attribute with: $mbeanPath $c $attributeName")

        try {
            if (c && c != null && c != "null" ) { return getMbean(mbeanPath).collect { processClosure(it."$attributeName", c)} } else { return getMbean(mbeanPath).collect { it."$attributeName" } }
        } catch( exception ) {
            if( exception instanceof MissingPropertyException ) {
                println "invokeGroovyScriptMethod: $exception.message"
                throw new NPAException("Mbean attribute $attributeName does not exist for Mbean $mbeanPath! : $exception.message", e)
            } else {
                println "invokeGroovyScriptMethod: $exception.message"
                //throw new NPAException("$exception.message", exception)
                throw exception
            }
        }
    }


    /*
     * Execute a J2EE Mbean method using an mbeanPath, operation and arguments
     */
    Object executeMbeanOperation(mbeanPath, operationName, String c, String[] args) {
        Log.debug("Executing Mbean attribute with: $mbeanPath $c $operationName $args")
        try {
            def value
            def fixNulls = args.findAll { it != null }.collect { it.toString() } as String[]
            def ret = getMbean(mbeanPath).collect { it.invokeMethod(operationName, fixNulls) }
            // What on earth is happening here??
            if (ret instanceof Exception) {
                throw ret
            }
            // Why???
            if (c && c != null && c != "null" ) { return ret.collect { processClosure(it, c) } } else { return ret }

        } catch( exception ) {
            if( exception instanceof MissingMethodException ) {
                Log.error("...Perhaps Mbean method $operationName does not exist for Mbean $mbeanPath : $exception.message", exception)
                Log.error("Your arguments could also be incorrect")
                exception.printStackTrace()
                throw exception
            } else {
                println "invokeGroovyScriptMethod: $exception.message"
                Log.error("$exception.message", exception)
                exception.printStackTrace()
                throw new NPAException("$exception.message", exception)
            }
        }
    }

    /*
     * Process information using a closure passed as an argument
     */
    public processClosure(input, String c) {

        Log.debug("Processing closure $c ")
        if ( c == null || c.length() == 0 ) {
          return input
        }
        try {
            def c2 = new GroovyShell().evaluate(c)
            Log.debug("Input is: " + input)
            return c2(input)
        } catch(e) {
            Log.error("A problem occurred when processing your closure argument: " + e.message)
            throw e
        }
    }



    public List<GroovyMBean> getManagementMbean(String mbeanPath) {
        try {
            def query = new ObjectName(mbeanPath)
            Log.debug("Retrieving " + mbeanPath)
            def allNames = this.managementServer.queryNames(query, null)
            return allNames.collect{ new GroovyMBean(this.managementServer, it) }
        } catch(e) {
            throw e
            //throw new NPAException("Unable to return Mbean for path: $mbeanPath", e)
        }
    }

    public List<GroovyMBean> getJ2EEMbean(String mbeanPath) {
        try {
            def query = new ObjectName(mbeanPath)
            def allNames = this.j2eeServer.queryNames(query, null)
            return allNames.collect{ new GroovyMBean(this.j2eeServer, it) }
        } catch(e) {
            throw e
            //throw new NPAException("Unable to return Mbean for path: $mbeanPath", e)
        }
    }

    /*
     * Override this method to accept Mbean calls, where they may need to be sent to a different Mbean Server (such as with OC4J)
     *
     */
    public List<GroovyMBean> getMbean(String mbeanPath) {
        return this.getJ2EEMbean(mbeanPath)
    }

    /*
     * Get the OAS node name
     */
    public getNode() {
       return this.j2eeServer.node
    }


    /*
     * Returns a String array with all java startup properties
     */
    public listStartupProperties() {
       this.j2eeServer.getPersistentProperties()
    }


    /*
     * Create a new metric model for a given name which stores a string
     */
    private MetricModel getJMXMetricModel(){
        MetricModel mod = new MetricModel()
        mod.setMetricName(null)
        mod.setMetricType("JMX")
        mod.setMetricDataType("String")
        mod.setIdentifier(null)
        mod.setInstanceName(this.instance);
        mod.setHostName(this.host)
        mod.setInitiatorID(this.initiatorID)
        return mod
    }


    /*
     * Save metric to the database
    */
    protected saveMetric(mod, value) {
        Log.debug("Metric value: $value")
        def met = persistMetric(mod, value, MetricsDB.getNewDateTime())
        met = null
    }


    /*
     * Try to get a JMXConnection, but timeout after 30 seconds to avoid hangs
     */
    private connectURLOrTimeout(jmxUrl, env) {
        ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor();
        def connection
        try {
            // Submit a job and wait up to 30 seconds for the connection to be returned, or timeout
            connection = timer1.submit(new Callable() {
                public call() {
                        return JMXConnectorFactory.connect(jmxUrl, env)
                }
            }).get(30, TimeUnit.SECONDS);
        } catch(e) {
             Log.error("Exception occurred whilst waiting for a JMX connection!")
             throw e
        }
        return connection
    }

}

