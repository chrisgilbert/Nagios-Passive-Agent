/*
 * A simple implementation of JMXCheck for Oracle Application Server.
 */
package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.common.*

class WeblogicCheck extends JMXCheck implements CheckInterface {

    public WeblogicCheck() {
    }

    synchronized public WeblogicCheck clone() {
        WeblogicCheck clone = (WeblogicCheck) super.makeClone(this.chk_name);
    }
 
    // Use this constructor for all classes extending Check
    WeblogicCheck(String chk_name, th_warn, th_crit, String th_type, Map args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }
    // Use this constructor for all classes extending Check
    WeblogicCheck(String chk_name, th_warn, th_crit, String th_type, groovy.util.slurpersupport.GPathResult args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }

    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        super.registerChecks()
        CheckRegister.add("chk_weblogic", "JMX", this.getClass().getName())
    }

    public chk_weblogic() {
        return super.chk_jmx()
    }

    @Override
    public init() {
        this.required += ["applicationName", "serverName"]
        this.optional += ["jmxProtocol"]
        super.init()
        this.gatherer = null
        try {
            this.gatherer = new WeblogicGatherer(variables)
        } catch(e) {
            Log.error("An error occurred when creating the gatherer:", e)
            CheckResultsQueue.add(super.generateResult(this.initiatorID, variables.nagiosServiceName, variables.host, "CRITICAL", [:], new Date(), "An error occurred when attempting a JMX connection!"))
            Log.error("Throwing error up chain")
            throw e
        }
    }

}