/*
 * A simple implementation of JMXCheck for Oracle Application Server.
 */
package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.common.*

class OASCheck extends JMXCheck implements CheckInterface {

    public OASCheck() {
    }

    synchronized public OASCheck clone() {
        OASCheck clone = (OASCheck) super.makeClone(this.chk_name);
    }
 
    // Use this constructor for all classes extending Check
    OASCheck(String chk_name, int th_warn, int th_crit, String th_type, Map args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }

    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        super.registerChecks()
        CheckRegister.add("chk_oas_attr", "JMX", this.getClass().getName())
        CheckRegister.add("chk_oas_oper", "JMX", this.getClass().getName())
    }

    public chk_oas_attr() {
        return super.chk_jmx_attr()
    }

    public chk_oas_oper() {
        return super.chk_jmx_attr()
    }

    @Override
    public init() {
        this.required += ["instance"]
        super.init()
        this.gatherer = null
        try {
            this.gatherer = new OASGatherer(variables.host, variables.port, variables.database, variables.user, variables.password, this.initiatorID)
        } catch(e) {
            Log.error("An error occurred when creating the gatherer:", e)
            CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a JMX connection!"))
            Log.error("Throwing error up chain")
            throw e
        }
    }

}