package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.gatherers.HTTPGatherer

/**
 * Implements HTTP checks using HTTPGatherer
 * @author Chris Gilbert
 */
class HTTPCheck extends Check implements CheckInterface {

    public HTTPCheck() {
    }
    
    // Use this constructor for all classes extending Check
    HTTPCheck(String chk_name, int th_warn, int th_crit, String th_type, Map args) {
        super(chk_name, th_warn, th_crit, th_type, args)
    }

    @Override
    public init() {
        super.init()
        this.gatherer = null
        this.gatherer = new HTTPGatherer(this.initiatorID)
        Log.debug("Gatherer initiator ID is ${this.initiatorID}")
    }

    HTTPCheck(chk_name, th_warn, th_crit, th_type, args) {
        super(chk_name, th_warn, th_crit, th_type, args)
        this.required += ["serverPath", "url", "host"]
        init()
    }

    public chk_http() {
        chkHTTP(this.variables, this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
    }


    private chkHTTP(variables, th_warn, th_crit, th_type) {
        init()

        def variables1 = variables.clone()
        assert th_warn != null, 'th_warn cannot be null!'
        assert th_crit != null, 'th_crit cannot be null!'
        def URL = variables1.url
        def performance = [:]
        def status
        def message
        def avgMessage = ""
        def respTime

        Log.debug("Running sample check on URL: " + URL)

            respTime = this.gatherer.sample("HTTP_MS_RESP_TIME", variables1).toDouble()

            if (respTime == -1 ) {
                Log.error("Failed to get valid response from URL :(")
                status = "CRITICAL"
                message = "Failed to get response from URL!"
                performance = [('response_time_ms'):"${respTime}ms;$th_warn;$th_crit;;"]
            } else {

                if (variables1.timePeriodMillis != null ){
                    Log.info("Retrieving average results over ${variables11.timePeriodMillis}")
                    avgMessage = "(average over ${variables1.timePeriodMillis} ms)"
                    respTime = this.gatherer.avg("HTTP_MS_RESP_TIME", variables1).toDouble()
                }

                Log.info("Received valid response from URL in ${respTime}ms.")
                if (respTime >= th_warn ) { status = "WARNING" }
                if (respTime >= th_crit ) { status = "CRITICAL" }
                if (respTime < th_warn ) { status = "OK" }


                performance = [('response_time_ms'):"${respTime}ms;$th_warn;$th_crit;;"]
                message = "Received valid response from URL in ${respTime}ms. $avgMessage"

            }

        Log.debug("Status is $status")
        Log.debug(message)
        return super.generateResult(this.initiatorID, variables1.nagiosServiceName, variables1.host, status, performance, new Date(), message)

            this.gatherer = null;
    }

    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        CheckRegister.add("chk_http", "HTTP", this.getClass().getName())
    }




}

