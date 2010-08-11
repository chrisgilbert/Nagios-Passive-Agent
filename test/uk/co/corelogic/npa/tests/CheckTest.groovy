package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA
import java.util.Random

/**
 * Tests to run on the Check superclass
*/
class CheckTest extends NPATest {

def c1

    void testInstantiate() {
        PluginsRegister.registerInternalPlugins()
        Log.info("Registerd checkList:" + CheckRegister.getCheckNames())
        Log.info("Registerd classNames:" + CheckRegister.getClassRegister())
        Log.info("Registerd metricList:" + MetricRegister.getMetricNames())
        Log.info("Registerd metric classNames:" + MetricRegister.getClassRegister())
        
        def variables = [:]
        variables = [nagiosServiceName:"TEST"]
        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.chk_name = "chk_disk_free"
        c1.chk_th_warn = 1
        c1.chk_th_crit = 2
        c1.chk_th_type = "GTE"
        c1.chk_args = variables
    }

    /**void testCmdShell() {
        def cmd = ""
        this.c1 = new Check(cmd)
    }*/

    void testCalculateStatus() {
        Log.debug("Testing logger")
        def variables = [:]
        variables = [nagiosServiceName:"TEST"]
        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.chk_name = "chk_disk_free"
        c1.chk_th_warn = 1
        c1.chk_th_crit = 2
        c1.chk_th_type = "GTE"
        c1.chk_args = variables

        // Test greater-than-or-equal (GTE)
        assertEquals("OK", c1.calculateStatus(1, 2, 0, "GTE"))
        //assertEquals("WARNING", c1.calculateStatus(1, 2, 1, "GTE"))
        assertEquals("CRITICAL", c1.calculateStatus(1, 2, 2, "GTE"))
        assertEquals("UNKNOWN", c1.calculateStatus(1, 2, -1, "GTE"))

        // Test greater-than (GT)
        assertEquals("OK", c1.calculateStatus(1, 2, 1, "GT"))
        assertEquals("WARNING", c1.calculateStatus(1, 2, 2, "GT"))
        assertEquals("CRITICAL", c1.calculateStatus(1, 2, 3, "GT"))
        assertEquals("UNKNOWN", c1.calculateStatus(1, 2, -1, "GT"))

        // Test less-than-or-equal (LTE)
        assertEquals("OK", c1.calculateStatus(10, 5, 11, "LTE"))
        assertEquals("WARNING", c1.calculateStatus(10, 5, 10, "LTE"))
        assertEquals("CRITICAL", c1.calculateStatus(10, 5, 5, "LTE"))
        assertEquals("UNKNOWN", c1.calculateStatus(10, 5, -1, "LTE"))

        // Test less-than (LT)
        assertEquals(c1.calculateStatus(10, 5, 10, "LT"), "OK")
        assertEquals(c1.calculateStatus(10, 5, 5, "LT"), "WARNING")
        assertEquals(c1.calculateStatus(10, 5, 4, "LT"), "CRITICAL")
        assertEquals(c1.calculateStatus(10, 5, -1, "LT"), "UNKNOWN")

        // Test collections
        assertEquals("OK", c1.calculateStatus(1, 2, [0,0,0,0,0], "GTE"))
        assertEquals("WARNING", c1.calculateStatus(1, 2, [0,0,0,1,0], "GTE"))
        assertEquals("CRITICAL", c1.calculateStatus(1, 2, [0,2,0,0,0], "GTE"))
        assertEquals("OK", c1.calculateStatus(1, 2, [0,0,0,0,-1], "GTE"))

        // Test string equals
        assertEquals("OK", c1.calculateStatus("Bla", "Bla2", "Bla Bla", "EQ"))
        assertEquals("CRITICAL", c1.calculateStatus("Bla", "Bla Bla", "Bla Bla", "EQ"))
        assertEquals("CRITICAL", c1.calculateStatus(null, "Bla Bla", "Bla Bla", "EQ"))
        assertEquals("CRITICAL", c1.calculateStatus(null, "Bla Bla", ["123", "456", "Bla Bla"], "EQ"))
        
        // Test string contains
        assertEquals("OK", c1.calculateStatus("Bla", "Bla2", "Bla Bla", "CONTAINS"))
        assertEquals("CRITICAL", c1.calculateStatus(null, "Bla", "Bla Bla", "CONTAINS"))
        assertEquals("CRITICAL", c1.calculateStatus(null, "Bla2", "Bla Bla2", "CONTAINS"))
        assertEquals("CRITICAL", c1.calculateStatus(null, "Bla2", ["Bla1", "Bla3", "Bla Bla2"],"CONTAINS"))
        
    }

    void testGenerateResult() {
        def status = "WARNING"
        def variables = [:]
        variables = [nagiosServiceName:"TEST"]
        def performance = ["status":status]
        def message = "A message: 1 1 2 3"
        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.chk_name = "chk_disk_free"
        c1.chk_th_warn = 1
        c1.chk_th_crit = 2
        c1.chk_th_type = "GTE"
        c1.chk_args = variables

        def n = c1.generateResult(UUID.randomUUID(), variables.nagiosServiceName, "TEST", status, performance, new Date(), message)
    }

    void testSchedule() {
        def variables = [:]
        variables = [nagiosServiceName:"TEST"]
        def performance = ["status":"TEST"]
        def message = "A message: 1 1 2 3"
        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.chk_name = "chk_disk_free"
        c1.chk_th_warn = 1
        c1.chk_th_crit = 2
        c1.chk_th_type = "GTE"
        c1.chk_args = variables
        def interval = 10000
        c1.schedule(interval)
    }
}