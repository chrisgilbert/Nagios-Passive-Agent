package uk.co.corelogic.npa.nagios
import uk.co.corelogic.npa.common.*

import uk.co.corelogic.npa.NPA
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.AuthConfig
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.HTML
import org.apache.http.auth.*
import org.apache.http.*

/* This is an implementation of the HTTP curl submission script in groovy.
   It avoids having to use the external program to submit nagios results.
   In the future we may be able to use our own listener instead of the nagios CGI interface.

*/

class SendCheckResultHTTP {

static boolean submit(CheckResult) {

def config = NPA.getConfigObject()

def args
Log.debug("Submitting to URL: " + config.npa.submit_url)
def http = new HTTPBuilder(config.npa.submit_url)

// If a proxy is needed.
if (config.npa.proxy_enable == "true") {
    Log.info("Using proxy for HTTP submission.")
    http.setProxy(config.npa.proxy_host, config.npa.proxy_port, null)

    // If authentication is needed
    if (config.npa.proxy_auth_type == 'basic') { 
        if ( config.npa.proxy_user == null || config.npa.proxy_user == "none" ) {
            Log.info("Using proxy ${config.npa.proxy_host} with no authentication.")
            http.auth.basic(config.npa.proxy_host, config.npa.proxy_port)
        }
        else (config.npa.proxy_auth_type == null ) {
            Log.info("Using basic/digest auth for proxy.")
            http.auth.basic(config.npa.proxy_host, config.npa.proxy_port,
            config.npa.proxy_user,config.npa.proxy_password)
        }
    } else if (config.npa.proxy_auth_type == 'ntlm') {
            Log.info("Using NTLM auth for proxy.")
            def client = http.getClient();
            Credentials defaultcreds = new NTCredentials(config.npa.proxy_user, config.npa.proxy_password);
            client.getState().setCredentials(new AuthScope(config.npa.proxy_host, config.npa.proxy_port, AuthScope.ANY_REALM), defaultcreds);
    } else {
        Log.debug("Using proxy ${config.npa.proxy_host} with no authentication.")
        
    }
 } else {
     Log.info("NOT Using proxy for HTTP submission.  Make sure proxy is configured correctly in defaults.groovy if this is required.")
 }

    // Set auth for HTTP
    http.auth.basic(config.npa.submit_auth_server, config.npa.submit_port, config.npa.submit_http_user, config.npa.submit_http_passwd)
    //http.auth.basic(config.npa.submit_http_user, config.npa.submit_http_passwd)
    //Log.debug("Auth details:" + config.npa.submit_url +" " + config.npa.submit_port)


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

        // Cleanup message to make sure no dodgy characters have snuck in
        String message = CheckResult.message.toString().replaceAll("\\s\\s+|\\n|\\r|\\t", " ").replaceAll("\u0000", " ");
        Log.debug("Message string after character replacement is: " + message);


http.request( GET, HTML ) {
  uri.path = config.npa.submit_path
  // Here we make up the GET request using the results from the check.

  if ( CheckResult.check_name == null ) {
    uri.query = [
    cmd_typ:'87',
    cmd_mod:'2',
    host:CheckResult.hostname,
    plugin_state:nagiosStatus,
    plugin_output:message,
    btnSubmit:'Commit']
  } else {
    uri.query = [
    cmd_typ:'30',
    cmd_mod:'2',
    host:CheckResult.hostname,
    service:CheckResult.check_name,
    plugin_state:nagiosStatus,
    plugin_output:message,
    performance_data:perfStats,
    btnSubmit:'Commit']
  }

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
    }  else if ( html =~ /An error occurred/ ) {
        Log.error("Failed to submit check! Nagios/Icinga returned a general processing error.  Check the DEBUG log for the GET string being used.")
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


