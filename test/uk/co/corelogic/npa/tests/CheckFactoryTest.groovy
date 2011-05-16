/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA

/**
 *
 * @author chris
 */
class CheckFactoryTest extends NPATest {

    public void testgetCheck() {
        PluginsRegister.registerInternalPlugins()
        println CheckRegister.getCheckNames()
        CheckRegister.getCheckNames().each {
            assert CheckFactory.getCheck(it.key) != null, "Unable to create check!"
        }
    }

}

