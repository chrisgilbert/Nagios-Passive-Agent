package uk.co.corelogic.npa
import java.net.ServerSocket
import uk.co.corelogic.npa.nagios.MessageHandler
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.reports.*
import uk.co.corelogic.npa.common.StartChecks
import uk.co.corelogic.npa.common.*

class NPA {

def static config

    static void main(String[] args) {
            System.setProperty("runPath", new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString())
              config = this.getConfigObject()

            try {
              StartChecks.start()
              StartChecks.startMaintenance()
            }
            catch (e) {
                Log.fatal("Eek.  A horrible exception made it all the way back here!", e)
                e.getStackTrace()
                MaintenanceUtil.sendShutdownHost(e.message)
                throw e
            }
    }

    synchronized static ConfigObject getConfigObject() {

            if (config) { 
                return config
            }
            else {
                if ( System.getProperty("npa.testMode") == "true" ) {
                        println("Test mode ENABLED.")
                        return new ConfigSlurper().parse(
                            new File(new File(
                                NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/../config/defaults.groovy").toURL())
                } else {
                    return new ConfigSlurper().parse(
                                new File(new File(
                                        NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/config/defaults.groovy").toURL())
                }
            }
    }

}

