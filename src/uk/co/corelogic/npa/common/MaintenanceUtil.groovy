/*
 * This
 */

package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.NPA
import uk.co.corelogic.npa.nagios.*

/**
 * This class deals with maintenance jobs that need to be run regularly and utility methods.
 * It also contains the host check ping, which simply generates an ok result for the current host, and returns this to Nagios.
 *
 * @author Chris Gilbert
 */
static class MaintenanceUtil {
	
    static config = NPA.getConfigObject()
    static npa_version = "1.2_test1"


    public static getNPAVersion() {
        return npa_version;
    }
    public static getHostName() {
        String hostname

        if ( config.use_os_hostname_command == "true" ) {
            hostname = "hostname".execute().text.trim()
       } else {

        try {
            InetAddress addr = InetAddress.getLocalHost();
            byte[] ipAddr = addr.getAddress();
            hostname = addr.getHostName();
                Log.debug("Hostname detected as $hostname")
            } catch (UnknownHostException e) {
                Log.error("Hostname lookup failed!  Please check name resolution is working, or set use_os_hostname_command to true")
                throw e
            }
       }
        return hostname;
    }

    /*
     * Generate an OK host check and send to Nagios
    */
    public static void sendHostOk() {
        def hostChk = new CheckResult (null, MaintenanceUtil.getHostName(), "OK", null, "none", "Host is running Nagios Passive Agent version " + MaintenanceUtil.getNPAVersion())
        def result = SendCheckResultHTTP.submit(hostChk)
        if (!result) {
            Log.error("Failed to submit host check back to server!")
        }
    }
}

