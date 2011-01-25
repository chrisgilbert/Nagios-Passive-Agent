package uk.co.corelogic.npa.nagios
import uk.co.corelogic.npa.common.*

import uk.co.corelogic.npa.NPA
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.AuthConfig
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.HTML
import org.apache.http.auth.*


/* This is an implementation of the HTTP curl submission script in groovy.
   It avoids having to use the external program to submit nagios results.
   In the future we may be able to use out own listener instead of the nagios CGI interface.
   TODO: Implement NTLM auth for proxies.  

*/

class SendCheckResultHTTP {

static boolean submit(CheckResult) {


def args
Log.debug("Submitting to URL: " + NPA.config.npa.submit_url)
def http = new HTTPBuilder(NPA.config.npa.submit_url)

// If a proxy is needed.
if (NPA.config.npa.proxy_enable == "true") {
    http.setProxy(NPA.config.npa.proxy_host, NPA.config.npa.proxy_port, null)

    // If authentication is needed
    if (NPA.config.npa.proxy_auth_type == 'basic') {
        if ( NPA.config.npa.proxy_user == null || NPA.config.npa.proxy_user == "none" ) {
            http.auth.basic(NPA.config.npa.proxy_host, NPA.config.npa.proxy_port)
        } else {
            http.auth.basic(NPA.config.npa.proxy_host, NPA.config.npa.proxy_port,
            NPA.config.npa.proxy_user,NPA.config.npa.proxy_password)
        }
    }
        // Stick some NTLM auth in here
}

    // Set auth for HTTP
    http.auth.basic(NPA.config.npa.submit_auth_server, NPA.config.npa.submit_port, NPA.config.npa.submit_http_user, NPA.config.npa.submit_http_passwd)
    //http.auth.basic(NPA.config.npa.submit_http_user, NPA.config.npa.submit_http_passwd)
    Log.debug("Auth details:" + NPA.config.npa.submit_url +" " + NPA.config.npa.submit_port)


// Clean up perf stats for submission
def perfStats = ""
CheckResult.performance.each {
    perfStats = perfStats + " " + "${it.key}=${it.value}"
}
// Check that the nagios response codes are set correctly
def nagiosStatus

   switch(CheckResult.status) {
            case "OK": 
                nagiosStatus = 0
                break
            case "WARNING": 
                nagiosStatus = 1
                break
            case "CRITICAL": 
                nagiosStatus = 2
                break
            case "UNKNOWN": 
                nagiosStatus = -1
                break
            case -1..3 : 
                nagiosStatus = CheckResult.status
                break
            default : 
                nagiosStatus = -1
                break
   }

http.request( GET, HTML ) {
  uri.path = NPA.config.npa.submit_path
  // Here we make up the GET request using the results from the check.
  uri.query = [
  cmd_typ:'30',
  cmd_mod:'2', 
  host:CheckResult.hostname,
  service:CheckResult.check_name,
  plugin_state:nagiosStatus,
  //plugin_output:URLEncoder.encode(CheckResult.message, "UTF-8") + "|" + URLEncoder.encode(perfStats, "UTF-8"),
  //performance_data:URLEncoder.encode(perfStats,"UTF-8"),
  plugin_output:CheckResult.message,
  performance_data:perfStats,
  btnSubmit:'Commit']

  response.success = { resp, html ->
    assert resp.statusLine.statusCode == 200
    Log.debug "Got response: ${resp.statusLine}"
    Log.debug  "Content-Type: ${resp.headers.'Content-Type'}"
    
    // Iterate through returned HTML and search for a specific Nagios errors
    if ( html =~ /Sorry, but you are not authorized to commit the specified command/ ){
        Log.error("Failed to submit check!  Nagios returned unauthorised command error.")
        Log.error(html)
        return false
    } else if ( html =~ /and verify that you entered all required information correctly/  ) {
        Log.error("Failed to submit check! Nagios returned invalid properties error.  Check the DEBUG log for the GET string being used.")
        Log.error(html)
    } else { return true }
  }

    response.failure = { resp ->
        Log.error("Unexpected failure: ${resp.statusLine}")
        Log.debug(resp)
        return false
    }
    response.'404' = { resp ->
        Log.error('Page Not Found')
        return false
    }
    response.'401' = { resp ->
        Log.error('Credentials NOT accepted.')
        return false
    }
}
}
   static test_submit() {
       // A quick unit test to check HTTP submission works
       def cr = new CheckResult("check_http_fwlive", "dummyhost", "WARNING", ['result':'800ms'], new Date(), "bla bla test")
       assert submit(cr)
   }
}

