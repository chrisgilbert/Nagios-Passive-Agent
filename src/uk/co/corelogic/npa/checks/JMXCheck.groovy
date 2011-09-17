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

    @Override
    public init() {
        this.required += ["host", "port", "username", "password", "mbeanPath"]
        this.optional += ["collectionClosure"]
        this.requiredWith += ["operation":["operationName","varName"], "attribute":["attributeName","varName"]]
        this.optionalWith += ["operation":["operationArguments", "closureFunction"], "attribute":["closureFunction"]]
        super.init()

        this.operations.put( { argsAsXML.each { new NPAMBeanOperation() << it } })
        this.attributes.put( { argsAsXML.each { new NPAMBeanAttribute() << it } })
    }


    /*
     *  Run a selection of operations/attributes and check the resulting values
     */
    public chk_jmx() {
        init()
        return this.chkJMX(this.variables.clone(), this.arguments, this.operations)
    }

    /*
     * Evaluate arguments and operations specified, and retrieve from gatherer, then generate check result
    */
    private chkJMX(vars, arguments, operations) {
        def results = []
        def timePeriod = null
        def avgMessage
        def message

        if (vars.timePeriodMillis != null ) {
            timePeriod = vars.timePeriodMillis
            avgMessage = "(average over ${timePeriod} ms)"
        }

        arguments.each {
            def localVars = vars.clone()
            results += getJMXAttr(timePeriod, it)
        }
        
        operations.each {
            def localVars = vars.clone()
            results += getJMXOper(timePeriod, it)
        }

        def values
        if ( vars.collectionClosure != null) {
            Log.debug("Processing collection closure on all retrieved values.")
            values = this.processClosure(results, vars.collectionClosure)
        } else {
            Log.debug("No collection closure specified - comparing all retrieved values with thresholds")
            values = result.collect { it.value }
        }

        def status = calculateStatus(chk_th_warn, chk_th_crit, values, chk_th_type)

        message = "Values returned: ${avgMessage ?: ""} $results"

        this.gatherer.disconnect()
        this.gatherer = null

        if (values.isEmpty()) {
            Log.info("Value was null")
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
            return c2(input)
        } catch(e) {
            Log.error("A problem occurred when processing your closure argument: " + e.message)
            throw e
        }
    }


    private Map getJMXAttr(timePeriod, attribute) {
        def value, avgMessage
        def performance = [:]
        def message
        vars.identifier = attribute.mbeanPath + "." + attribute.attributeName + ",Closure:" + attribute.closureFunction
        try {
            if (vars.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${timePeriod}")
                gatherer.sample("JMX_ATTR_VALUE", attribute)
                value = gatherer.avg("JMX_ATTR_VALUE", attribute)
                performance = [(attribute.varName):value]
                avgMessage = "(average over ${timePeriod} ms)"
            } else {
                value = gatherer.sample("JMX_ATTR_VALUE", attribute)
                performance = [(vars.varName):value]
            }
            Log.debug("Value returned: ${avgMessage ?: ""} $value for ${attribute.varName}")
            

        } catch(e) {
            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
            Log.error("Throwing error up chain")
            throw e
        }
        return performance
    }

    private Map getJMXOper(vars) {
        def value, avgMessage
        def performance = [:]
        def message
        vars.identifier = vars.mBeanPath + "." + vars.attributeName + ",Closure:" + vars.closureFunction
        try {
            if (vars.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${vars.timePeriodMillis}")
                gatherer.sample("JMX_OPER_VALUE", vars)
                value = gatherer.avg("JMX_OPER_VALUE", vars)
                performance = [(vars.varName):value]
                avgMessage = "(average over ${vars.timePeriodMillis} ms)"
            } else {
                value = gatherer.sample("JMX_OPER_VALUE", vars)
                performance = [(vars.varName):value]
            }
            Log.debug("Value returned: ${avgMessage ?: ""} $value for ${vars.varName}")

        } catch(e) {
            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
            Log.error("Throwing error up chain")
            throw e
        }
        return performance
    }


//    private chkJMXAttr(vars) {
//        def value, avgMessage
//        def performance = [:]
//        def message
//        vars.identifier = vars.mBeanPath + "." + vars.attributeName + ",Closure:" + vars.closureFunction
//        try {
//            if (vars.timePeriodMillis != null ){
//                Log.info("Retrieving average results over ${vars.timePeriodMillis}")
//                value = gatherer.sample("JMX_ATTR_VALUE", vars).toFloat()
//                performance = [(vars.attributeName):value]
//                value = gatherer.avg("JMX_ATTR_VALUE", vars)
//                avgMessage = "(average over ${vars.timePeriodMillis} ms)"
//            } else {
//                value = gatherer.sample("JMX_ATTR_VALUE", vars)
//                performance = [(vars.attributeName):value]
//            }
//            Log.debug("Value returned: ${avgMessage ?: ""} $value for ${vars.attributeName}")
//            message = "Value returned: ${avgMessage ?: ""} $value for ${vars.attributeName}"
//
//        } catch(e) {
//            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
//                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when retrieving JMX attribute!"))
//            Log.error("Throwing error up chain")
//            throw e
//        }
//
//        def status = calculateStatus(chk_th_warn, chk_th_crit, value, chk_th_type)
//
//        this.gatherer.disconnect()
//        this.gatherer = null
//
//        if (value == null) {
//            Log.info("Value was null")
//            status = "UNKNOWN"
//            message = "No attribute returned - unknown STATUS."
//        }
//
//        return super.generateResult(this.initiatorID, vars.nagiosServiceName, vars.host, status, performance, new Date(), message)
//    }


//    private chkJMXOper(vars) {
//        def value, avgMessage
//        def performance = [:]
//        def message
//        vars.identifier = vars.mBeanPath + "." + vars.operationName + ",Closure:" + vars.closureFunction + ',args:' + vars.operationArguments
//        try {
//            if (vars.timePeriodMillis != null ){
//                Log.info("Retrieving average results over ${vars.timePeriodMillis}")
//                value = gatherer.sample("JMX_OPER_VALUE", vars).toFloat()
//                performance = [(vars.operationName):value]
//                value = gatherer.avg("JMX_OPER_VALUE", vars)
//                avgMessage = "(average over ${vars.timePeriodMillis} ms)"
//            } else {
//                value = gatherer.sample("JMX_OPER_VALUE", vars)
//                performance = [(vars.operationName):value]
//            }
//            Log.debug("Value returned: ${avgMessage ?: ""} $value for ${vars.operationName}")
//            message = "Value returned: ${avgMessage ?: ""} $value for ${vars.operationName}"
//
//        } catch(e) {
//            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
//                CheckResultsQueue.add(super.generateResult(this.initiatorID, vars.nagiosServiceName, vars.host, "CRITICAL", [:], new Date(), "An error occurred when retrieving JMX operation!"))
//            Log.error("Throwing error up chain")
//            throw e
//        }
//
//        def status = calculateStatus(chk_th_warn, chk_th_crit, value, chk_th_type)
//
//        this.gatherer.disconnect()
//        this.gatherer = null
//
//        if (value == null) {
//            Log.info("Return value was null")
//            message = "No operation value returned."
//        }
//
//        if (value == false) {
//            Log.info("Return value was FALSE")
//            status="CRITICAL"
//            message = "Operation failed - returned FALSE."
//        }
//
//        return super.generateResult(this.initiatorID, vars.nagiosServiceName, vars.host, status, performance, new Date(), message)
//    }


}
