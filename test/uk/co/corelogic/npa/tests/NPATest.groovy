package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA
import uk.co.corelogic.npa.common.Log

/**
 * Superclass for other tests
*/
class NPATest extends GroovyTestCase {

 static config = NPA.getConfigObject()


    void testNothing() {
        def test = 1
        assert test == 1, "Test nothing is testing something!"
    }
}

