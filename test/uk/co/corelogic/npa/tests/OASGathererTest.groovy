package uk.co.corelogic.npa.tests

import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA
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
        variables.port = "6003      n"
        variables.host = "oas-lon-proj.dev.corelogic.local"
        variables.instance = "forms_dev"

        return new OASGatherer(variables)

    }
    void testConnect()
    {
        instantiate().connect()
    }
	
}

