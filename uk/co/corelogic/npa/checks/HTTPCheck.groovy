package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.gatherers.HTTPGatherer

/**
 * Implements HTTP checks using HTTPGatherer
 * @author Chris Gilbert
 */
class HTTPCheck extends Check {

String result
String host
    
    public init() {
        if ( gatherer == null) {
            this.initiatorID  = UUID.randomUUID();
            this.gatherer = new HTTPGatherer(this.initiatorID)
            Log.debug("Gatherer initiator ID is ${this.initiatorID}")
        }
    }

    synchronized public HTTPCheck clone() {
        HTTPCheck clone = (HTTPCheck)super.makeClone(this.chk_name);
        clone.result = this.result;
        clone.host = this.host;
        return clone;
    }

    HTTPCheck(chk_name, th_warn, th_crit, th_type, args) {
        super(chk_name, th_warn, th_crit, th_type, args)
        init()
    }

    HTTPCheck() {
        super()
    }


    public chk_http() {
        chkHTTP(this.chk_args, this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
    }


public chkHTTP(variables, th_warn, th_crit, th_type) {
    // Check for nulls
    init()
    assert variables != null, 'Variables cannot be null!'
    assert th_warn != null, 'th_warn cannot be null!'
    assert th_crit != null, 'th_crit cannot be null!'

    this.host = variables.host
    def URL = variables.url
    def performance = [:]
    def status
    def message

    Log.debug("Running sample check on URL: " + URL)
    def respTime = this.gatherer.sample("HTTP_MS_RESP_TIME", variables).toDouble()

        if (respTime == -1 ) {
            Log.error("Failed to get valid response from URL :(")
            status = "CRITICAL"
            message = "Failed to get response from URL!"
            performance = [('response_time'):0]
        } else {
            Log.info("Received valid response from URL in ${respTime}ms.")
            if (respTime >= th_warn ) { status = "WARNING" }
            if (respTime >= th_crit ) { status = "CRITICAL" }
            if (respTime < th_warn ) { status = "OK" }


            performance = [('response_time'):respTime]
            message = "Received valid response from URL in ${respTime}ms."
            
        }

    Log.debug("Status is $status")
    Log.debug(message)
    return super.generateResult(this.initiatorID, variables.nagiosServiceName, this.host, status, performance, new Date(), message)

        this.gatherer = null;
}

/**
* Register all the checks which this class implements
*/
public registerChecks() {
    CheckRegister.add("chk_http", "HTTP", this.getClass().getName())
}




}

