/*
 * A generic JMX Check class.  Other classes subclass this class to add specific application server functionality.
 */
package uk.co.corelogic.npa.checks

import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.Log
import uk.co.corelogic.npa.gatherers.*

/**
 *
 * @author Chris Gilbert
 */
class JMXCheck extends Check implements CheckInterface {


    def operations = []
    def attributes = []
    def collectionClosure
   

    public JMXCheck() {
    }
    
    /**
    * Register all the checks which this class implements - dummy method - implemented by subclasses
    */
    public registerChecks() {
    }

    
   // Use this constructor for all classes extending Check
    JMXCheck(String chk_name, th_warn, th_crit, String th_type, Map args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }
    // Use this constructor for all classes extending Check
    JMXCheck(String chk_name, th_warn, th_crit, String th_type, groovy.util.slurpersupport.NodeChild args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }


    @Override
    public init() {
        this.required += ["host", "port", "username", "password"]
        this.optional += ["collectionClosure"]
        this.requiredWith += ["operation":["operation.operationName","operation.varName","operation.mbeanPath"], "attribute":["attribute.attributeName","attribute.varName", "attribute.mbeanPath"]]
        this.optionalWith += ["operation":["operation.operationArguments", "operation.closureFunction"], "attribute":["attribute.closureFunction"]]
        super.init()
    }


    /*
     *  Run a selection of operations/attributes and check the resulting values
     */
    public chk_jmx() {
        init()

        if ( attributes.size() == 0 && operations.size() == 0 )
        {
            argsAsXML.attribute.each {
                def argsmap = [:]
                it.children().each {
                    argsmap["${it.name()}"]=it.text()
                }
                this.attributes.add(new NPAMBeanAttribute(argsmap))
            }
            argsAsXML.operation.each {
                def argsmap = [:]
                it.children().each {
                    argsmap["${it.name()}"]=it.text()
                }
                this.operations.add(new NPAMBeanOperation(argsmap))
            }
        }
        return this.chkJMX(this.variables.clone(), this.attributes, this.operations)
    }

    /*
     * Evaluate arguments and operations specified, and retrieve from gatherer, then generate check result
    */
    private chkJMX(vars, attributes, operations) {
        def results = [:]
        def timePeriod = null
        def avgMessage
        def message

        if (vars.timePeriodMillis != null ) {
            avgMessage = "(average over ${timePeriod} ms)"
        }

        attributes.each {
            def localVars = vars.clone()
            results.putAll(getJMXAttr(vars, it.properties))
        }
        
        operations.each {
            def localVars = vars.clone()
            results.putAll(getJMXOper(vars, it.properties))
        }

        def values = []
        if ( vars.collectionClosure != null) {
            Log.debug("Processing collection closure on all retrieved values.")
            values = this.processClosure(results, vars.collectionClosure)
        } else {
            Log.debug("No collection closure specified - comparing all retrieved values with thresholds")
            values = results.collect { it.value }
        }

        def status = calculateStatus(chk_th_warn, chk_th_crit, values, chk_th_type)

        message = "Values returned: ${avgMessage ?: ""} $results"

        this.gatherer.disconnect()
        this.gatherer = null

        if (! values) {
            Log.info("Value was null or empty")
            status = "UNKNOWN"
            message = "No values returned - unknown STATUS!"
        }

        return super.generateResult(this.initiatorID, vars.nagiosServiceName, vars.host, status, results, new Date(), message)
    }


    /*
     * Process information using a closure passed as an argument
     */
    public processClosure(input, String c) {
        Log.debug("Processing closure $c ")
        try {
            def c2 = new GroovyShell().evaluate(c)
            Log.debug("INPUT is *******************: " + input)
            return c2(input)
        } catch(e) {
            Log.error("A problem occurred when processing your closure argument: " + e.message)
            throw e
        }
    }


    protected Map getJMXAttr(vars, attribute) {
        def value, avgMessage
        def combined = [:]
        combined.putAll(vars)
        combined.putAll(attribute)
        def performance = [:]
        def message
        combined.identifier = combined.mbeanPath + "." + combined.attributeName + ",Closure:" + combined?.closureFunction
        try {
            if ( combined.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${combined.timePeriodMillis}")
                gatherer.sample("JMX_ATTR_VALUE", combined)
                value = gatherer.avg("JMX_ATTR_VALUE", combined)
                performance = [(combined.varName):value]
                avgMessage = "(average over ${combined.timePeriodMillis} ms)"
            } else {
                value = gatherer.sample("JMX_ATTR_VALUE", combined)
                performance = [(combined.varName):value]
            }
            Log.debug("Value returned: ${avgMessage ?: ""} $value for ${combined.varName}")
            

        } catch(e) {
            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
            Log.error("Throwing error up chain")
            throw e
        }
        return performance
    }

    protected Map getJMXOper(vars, operation) {
        def value, avgMessage
        def combined = [:]
        combined.putAll(vars)
        combined.putAll(operation)
        def performance = [:]
        def message
        combined.identifier = operation.mbeanPath + "." + combined.operationName + ",Closure:" + combined?.closureFunction
        try {
            if ( combined.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${combined.timePeriodMillis}")
                gatherer.sample("JMX_OPER_VALUE", combined)
                value = gatherer.avg("JMX_OPER_VALUE", combined)
                performance = [(combined.varName):value]
                avgMessage = "(average over ${combined.timePeriodMillis} ms)"
            } else {
                value = gatherer.sample("JMX_OPER_VALUE", combined)
                performance = [(combined.varName):value]
            }
            Log.debug("Value returned: ${avgMessage ?: ""} $value for ${combined.varName}")

        } catch(e) {
            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
            Log.error("Throwing error up chain")
            throw e
        }
        return performance
    }


}
