package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA
import oracle.oc4j.admin.jmx.remote.api.JMXConnectorConstant
import oracle.oc4j.admin.jmx.remote.api.*

import java.util.Random

/**
 *
 * @author chris
 */
class OASGathererTest extends NPATest {


    OASGatherer instantiate(){
        def variables = [:]
        variables.username = "oc4jadmin"
        variables.password = "Alexei12"
        variables.port = "6003"
        variables.host = "oas-lon-proj.dev.corelogic.local"
        variables.instance = "forms_dev"

        return new OASGatherer(variables)

    }
    public testConnect()
    {
        instantiate()
    }

    public void testAttributeGet(){
       println("Server Version is: " + instantiate().getMbeanAttributeValue("oc4j:j2eeType=J2EEServer,name=standalone", null, "serverVersion") )
    }

    public void testOperationExecution() {
        println(instantiate().executeMbeanOperation("oc4j:j2eeType=J2EEServer,name=standalone", "checkSharedLibraryExists", null, "global.libraries","1.0"))
        println(instantiate().executeMbeanOperation("oc4j:j2eeType=J2EEServer,name=standalone", "getSharedLibraryNames", null))
    }

    public void testGetHTTPSessions() {
        println(instantiate().getCountHTTPSessions())
    }

    public void testGetState(){
        def oas = instantiate()
        assert oas.getState() in [0,1,2,3,4]
        println("State is: " + oas.getStateString())
    }
}

