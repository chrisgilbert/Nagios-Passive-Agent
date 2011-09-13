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

        MetricModel mod = getJMXMetricModel()
        mod.metricName = "JMX_ATTR_VALUE"
        mod.identifier = variables.identifier
        mod.description = "JMX Attribute Value"

        def value = getMbeanAttributeValue(variables.mbeanPath, variables.closureFunction, variables.attributeName)
        saveMetric(mod, value)
        return value
    }

    public JMX_OPER_VALUE(variables)
    {
        Log.debug("Received variables: $variables")

        MetricModel mod = getJMXMetricModel()
        mod.metricName = "JMX_OPER_VALUE"
        mod.identifier = variables.identifier
        mod.description = "JMX Operation Value"

        def value =  executeMbeanOperation(variables.mbeanPath, variables.operationName, variables.closureFunction, variables.operationArguments)
        saveMetric(mod, value)
        return value
    }

    void finalize() {
        this.disconnect()
    }


    void disconnect() {
        Log.debug("Closing open connections to JMX..")
        try {
            this.j2eeConn.close()
            this.manConn.close()
            this.managementServer = null
            this.j2eeServer = null
        } catch(e) {}
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
            this.manConn = JMXConnectorFactory.connect(jmxUrl, env)
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
            this.j2eeConn =  JMXConnectorFactory.connect(jmxUrl, env)
            this.j2eeServer = j2eeConn.MBeanServerConnection
            this.j2eeServerInfo = getJ2EEMbean(j2eeMbeanPath)
            this.jvmInfo = getJ2EEMbean(jvmMbeanPath)
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
            if (c && c != null) { return processClosure(getJ2EEMbean(mbeanPath)."$attributeName", c)} else { return getJ2EEMbean(mbeanPath)."$attributeName" }
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
            def ret = getJ2EEMbean(mbeanPath).invokeMethod(operationName, args)
            // What on earth is happening here??
            if (ret instanceof Exception) {
                throw ret
            }
            
            if (c && c != null) { return processClosure(ret, c)} else { return ret }

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
        try {
            def c2 = new GroovyShell().evaluate(c)
            return c2(input)
        } catch(e) {
            Log.error("A problem occurred when processing your closure argument: " + e.message)
            throw e
        }
    }



    public GroovyMBean getManagementMbean(String mbeanPath) {
        try {
            return new GroovyMBean(this.managementServer, mbeanPath)
        } catch(e) {
            throw new NPAException("Unable to return Mbean for path: $mbeanPath", e)
        }
    }


    public GroovyMBean getJ2EEMbean(String mbeanPath) {
        try {
            return new GroovyMBean(this.j2eeServer, mbeanPath)
        } catch(e) {
            throw e
            //throw new NPAException("Unable to return Mbean for path: $mbeanPath", e)
        }
    }

    /*
     * Run the Mbean.executeQuery() method on a given Mbean path and set of parameters
    */
    public queryJ2EEMbean(mbeanPath, classParmString) {
        return getJ2EEMbean(mbeanPath).executeQuery(classParmString)
    }


    /*
     * Return an array of strings for the JVM Mbean locations
     */
    public getVMs() {
       return this.j2eeServer.JavaVMs
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
    private saveMetric(mod, value) {
        Log.debug("Metric value: $value")
        def met = persistMetric(mod, value, MetricsDB.getNewDateTime())
    }



}

