package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA

/**
 * Superclass for other tests
*/
abstract class NPATest extends GroovyTestCase {

def config

    public NPATest() {
        System.setProperty("runPath", new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString())
        this.config = NPA.getConfigObject()
    }

    //public void testTest(){
    //    println("Test")
    //}

}

