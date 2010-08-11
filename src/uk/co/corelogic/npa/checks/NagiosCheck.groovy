package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.gatherers.ExternalGatherer

/**
 * This class uses ExternalGatherer to run nagios checks directly.
 * These checks must comply with normal nagios guidelines
 * 
 * @author Chris Gilbert
 */
class NagiosCheck extends Check {

  
    public init() {
        if ( gatherer == null) {
            this.initiatorID  = UUID.randomUUID();
            this.gatherer = new ExternalGatherer(this.initiatorID)
            Log.debug("Gatherer initiator ID is ${this.initiatorID}")
        }
    }

    NagiosCheck(chk_name, args) {
        init()
    }
    NagiosCheck() {
        super()
    }

    synchronized public clone() {
        NagiosCheck clone = (NagiosCheck) super.makeClone(this.chk_name);
        return clone;
    }

    // Just ignore threshold stuff if it's specified
    public chk_nagios(variables, th_warn, th_crit, th_type) {
        init()
        return chk_nagios(variables)
    }

    public chk_nagios() {
        init()
        chk_nagios(this.chk_args)
    }

    /**
     * The method for running a nagios check and returning a CheckResult object
    */
    public CheckResult chk_nagios(variables) {
        init()
        // Check for null values
        assert variables != null, 'Variables are null!'
        assert variables.nagiosServiceName != null, 'A nagiosServiceName must be specified!'

        int scriptResult
        def output
        def p
        def performance = [:]
        def message
        String status

        def returnValues = gatherer.sample("NAGIOS_SCRIPT_VALUES", variables)

        Log.debug("Nagios script check values: $returnValues")

        performance = returnValues[2]
        message = returnValues[1]

        if ( message.length() > 200 ) {
            message = message.substring(0,200) + "...message truncated."
        }
        
        scriptResult = returnValues[0]

        // Now check for exit status of the script
        if (scriptResult == 1 ) { status="WARNING"}
        if (scriptResult == 2 ) { status="CRITICAL"}
        if (scriptResult == 0 ) { status="OK"}
        else if (scriptResult > 2 || scriptResult < 0 ) { status="UNKNOWN" }

        Log.debug("Status is $status")
        return new CheckResult(this.initiatorID, variables.nagiosServiceName, this.gatherer.host, status, performance, new Date(), message)
        this.gatherer = null;
    }

    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        CheckRegister.add("chk_nagios", "NAGIOS", this.getClass().getName())
    }


}
	


