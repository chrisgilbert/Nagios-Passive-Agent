package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.NPA
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.AuthConfig
import static groovyx.net.http.Method.GET
import static groovyx.net.http.ContentType.HTML
import org.apache.http.auth.*

class HTTPGatherer extends Gatherer {

def host
def path
def groupID
def metricList = []

    public HTTPGatherer(initiatorID) {
        super(initiatorID);
        this.registerMetrics();
    }

    /**
    Register a list of metrics which are provided by this gatherer

    Add new valid metrics here
    */

    public void registerMetrics() {
        this.metricList.add('HTTP_MS_RESP_TIME')
        super.addValidMetricList(this.metricList, 'HTTP', this.getClass().getName())
    }

/**
 * Return the response time in ms for a given URL, or -1 if the request results in an error
*/
private HTTP_MS_RESP_TIME(variables) {

     // Configure standard metric variables
     def identifier = variables.serverpath + variables.url
     def datestamp = MetricsDB.getNewDateTime()
     this.groupID  = UUID.randomUUID();

     def success = false
     def startTime
     def endTime
     def http

     // Create a MetricModel for the disk used metric
     MetricModel mod = new MetricModel()
     mod.setMetricName("HTTP_MS_RESP_TIME")
     mod.setMetricType("HTTP")
     mod.setMetricDataType("Double")
     mod.setIdentifier(identifier)
     mod.setHostName(variables.host)
     mod.setInstance(null)
     mod.setInitiatorID(this.initiatorID)
     mod.setGroupID(this.groupID)


     try {

     http = new HTTPBuilder(variables.serverpath)

     http.request( GET, HTML ) {
     uri.path = variables.url
     this.host = variables.host

      response.success = { resp, html ->
            assert resp.statusLine.statusCode == 200
            endTime = System.currentTimeMillis();
            Log.debug "Got response: ${resp.statusLine}"
            Log.debug  "Content-Type: ${resp.headers.'Content-Type'}"
            success=true
      }

        response.failure = { resp ->
            Log.error("Unexpected failure: ${resp.statusLine}")
            Log.debug(resp)
            success=false
        }
        response.'404' = { resp ->
            Log.error('Page Not Found')
            success=false
        }
        response.'401' = { resp ->
            Log.error('Credentials NOT accepted.')
            success=false
        }
        
     startTime = System.currentTimeMillis()

     }
     
    if ( success ) {
        def responseTime = endTime - startTime
        def m1 = persistMetric(mod, responseTime, datestamp)
      
        return responseTime
    } else {
        return -1
    }

    } catch (e) {
         Log.error("An exception was thrown when requesting the URL")
         Log.error("STACK:", e)
         return -1
     }
}



}