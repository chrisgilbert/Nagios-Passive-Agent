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
class WeblogicGathererTest extends NPATest {


    WeblogicGatherer instantiate(){
        def variables = [:]
        variables.username = "weblogic"
        variables.password = "c0r3l0g1c"
        variables.port = "7001"
        variables.host = "rel-wls11g.mscunix.corelogic.local"
        variables.serverName = "AdminServer"
        variables.applicationName = "mosaic"
        return new WeblogicGatherer(variables)
    }
    public testConnect()
    {
        instantiate()
    }

    public void testAttributeGet(){
        println(instantiate().getMbeanAttributeValue("com.bea:Location=AdminServer,ServerRuntime=AdminServer,Name=AdminServer_/mosaic,ApplicationRuntime=mosaic,Type=WebAppComponentRuntime", null, "OpenSessionsCurrentCount"))
    }

    public void testPrintOSInfo() {
        println ( "Name:" + instantiate().getMbeanAttributeValue("java.lang:type=OperatingSystem,Location=AdminServer", null, "Name"))
        println ( "Version:" + instantiate().getMbeanAttributeValue("java.lang:type=OperatingSystem,Location=AdminServer", null, "Version"))
        println ( "Processors:" + instantiate().getMbeanAttributeValue("java.lang:type=OperatingSystem,Location=AdminServer", null, "AvailableProcessors"))
        println ( "Load avg:" + instantiate().getMbeanAttributeValue("java.lang:type=OperatingSystem,Location=AdminServer", null, "SystemLoadAverage"))
    }

    public void testOperationExecution() {
        println(instantiate().executeMbeanOperation("java.lang:Location=AdminServer,type=Memory", "gc", null))
        //println(instantiate().executeMbeanOperation("oc4j:j2eeType=J2EEServer,name=standalone", "getSharedLibraryNames", null))
    }

    public void testGetHTTPSessions() {
        println(instantiate().getMbeanAttributeValue("com.bea:Location=AdminServer,ServerRuntime=AdminServer,Name=AdminServer_/mosaic,ApplicationRuntime=mosaic,Type=WebAppComponentRuntime", null, "OpenSessionsCurrentCount"))
    }

    public void testGetState(){
        println ( "Name:" + instantiate().getMbeanAttributeValue("com.bea:Location=AdminServer,ApplicationRuntime=mosaic,Name=AdminServer_/mosaic,ServerRuntime=AdminServer,Type=WebAppComponentRuntime", null, "Status"))
    }
}

