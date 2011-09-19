package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*

/**
 * Tests for the MaintenanceUtil class
 * @author Chris Gilbert
 */
class MaintenanceUtilTest extends NPATest{

    void testgetNPAVersion() {
        assert MaintenanceUtil.getNPAVersion() != null
    }

    void testgetHostName() {
        assert MaintenanceUtil.getHostName() != null
    }

    void testsendOK() {
        MaintenanceUtil.sendHostOk()
    }

    void testStopAllChecks() {
        def variables = [:]
        variables.volume="/"
        variables.nagiosServiceName="TEST"
        variables.unitType="percent"

        PluginsRegister.registerInternalPlugins()

        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.variables = variables
        c1.chk_th_warn = 1
        c1.chk_th_crit = 0.5
        c1.chk_name = "chk_disk_free"
        c1.chk_th_type= "LTE"
        c1.schedule(10000)
       
        MaintenanceUtil.stopAllTimers()
    }

}

