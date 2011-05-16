package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.common.MaintenanceUtil

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
}

