package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.gatherers.ExternalGatherer

/**
 * This class uses ExternalGatherer to run nagios checks directly.
 * These checks must comply with normal nagios guidelines
 * 
 * @author Chris Gilbert
 */
class NagiosCheck extends Check implements CheckInterface {

    /*
     * A set of arguments used by the check or gatherer.  There should be Lists for required and optional, and Maps for requiredWith and optionalWith.
     * These allow the XML to be appropriately checked for missing configuration
     *
     */
    this.required = ["scriptName", "scriptType", "returnType", "instanceName"]
    this.optional = ["scriptArgs", "saveMetrics"]
    this.requiredWith = ["saveMetrics":"dataType","saveMetrics":"metricName"]
    this.optionalWith = [:]
    

    def identifier = variables.identifier
    def datestamp = MetricsDB.getNewDateTime()


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
        init(variables, required)
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
	


