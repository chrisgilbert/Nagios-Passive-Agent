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

    public JMXCheck() {
    }
    
    /**
    * Register all the checks which this class implements - dummy method - implemented by subclasses
    */
    public registerChecks() {
    }

    
   // Use this constructor for all classes extending Check
    JMXCheck(String chk_name, int th_warn, int th_crit, String th_type, Map args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }

    @Override
    public init() {
        this.required += ["host", "port", "username", "password", "mbeanPath"]
        this.requiredWith += ["operation":["operationName"], "attribute":["attributeName"]]
        this.optionalWith += ["operation":["operationArguments"], "operation":["closureFunction"], "attribute":["closureFunction"]]
        super.init()
    }


    /*
     *  Check a JMX atrtibute value
     */
    public chk_jmx_attr() {
        init()
        return this.getJMXAttr(this.variables.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
    }

    /*
     *  Run a JMX operation and return the output
     */
    public chk_jmx_oper() {
        init()
        return this.getJMXOper(this.variables.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)

        .getMbeanAttributeValue("oc4j:j2eeType=J2EEServer,name=standalone", null, "serverVersion")

    }

    private chkJMXAttr(vars) {
        def value
        try {
            if (vars.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${vars.timePeriodMillis}")
                value = gatherer.sample("JMX_ATTR_VALUE", vars).toFloat()
                performance = [(vars.attributeName):value]
                value = gatherer.avg("JMX_ATTR_VALUE", vars)
                avgMessage = "(average over ${vars.timePeriodMillis} ms)"
            } else {
                value = gatherer.sample("JMX_ATTR_VALUE", vars)
            }
            Log.debug("Value returned: $avgMessage $value for ${vars.attributeName}")

        } catch(e) {
            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when retrieving JMX attribute!"))
            Log.error("Throwing error up chain")
            throw e
        }

        status = calculateStatus(th_warn, th_crit, value, th_type)

        this.gatherer.disconnect()
        this.gatherer = null

        if (value == null) {
            Log.info("Value was null")
            status = "UNKNOWN"
            message = "No attribute returned - unknown STATUS."
        }

        return super.generateResult(this.initiatorID, this.variables1.nagiosServiceName, this.variables1.host, status, performance, new Date(), message)
    }


    private chkJMXOper(vars) {
        def value
        try {
            if (vars.timePeriodMillis != null ){
                Log.info("Retrieving average results over ${vars.timePeriodMillis}")
                value = gatherer.sample("JMX_OPER_VALUE", vars).toFloat()
                performance = [(vars.operationName):value]
                value = gatherer.avg("JMX_OPER_VALUE", vars)
                avgMessage = "(average over ${vars.timePeriodMillis} ms)"
            } else {
                value = gatherer.sample("JMX_OPER_VALUE", vars)
                performance = [(vars.operationName):value]
            }
            Log.debug("Value returned: $avgMessage $value for ${vars.operationName}")

        } catch(e) {
            Log.error("An error occurred when attempting to retrive JMX attribute:", e)
                CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when retrieving JMX operation!"))
            Log.error("Throwing error up chain")
            throw e
        }

        status = calculateStatus(th_warn, th_crit, value, th_type)

        this.gatherer.disconnect()
        this.gatherer = null

        if (value == null) {
            Log.info("Return value was null")
            message = "No operation value returned."
        }

        if (value == false) {
            Log.info("Return value was FALSE")
            status="CRITICAL"
            message = "Operation failed - returned FALSE."
        }

        return super.generateResult(this.initiatorID, this.variables1.nagiosServiceName, this.variables1.host, status, performance, new Date(), message)
    }


}
