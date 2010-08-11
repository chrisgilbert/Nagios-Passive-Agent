package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.checks.*

/**
 * Test the OSCheck class
 */
class OSCheckTest extends NPATest {

    def chk_name = "chk_disk_free"
    int th_warn = 10
    int th_crit = 5
    def th_type = "LTE"
    def variables = [:]
    


    void testInstatiate() {
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        this.variables.unitType = "percent"
        this.variables.nagiosServiceName="TEST"
    }

    void testRegisterChecks() {
        this.variables.nagiosServiceName="TEST"
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        check.registerChecks()
    }

    void testChk_disk_free() {
        this.variables.volume="ALL"
        this.variables.nagiosServiceName="TEST"
        this.variables.unitType="percent"
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_disk_free()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)

        println("Running two more times for good measure...")
        result = check.chk_disk_free()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
        result = check.chk_disk_free()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)

        this.variables.volume="/"
        this.variables.nagiosServiceName="TEST"
        this.variables.unitType="percent"
        check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        result = check.chk_disk_free()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }

    void testChk_cpu_pct() {
        this.th_type = "GTE"
        this.variables.nagiosServiceName="TEST"
        def check = new OSCheck("chk_cpu_pct", th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_cpu_pct()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }


    void testChk_disk_busy_pct() {
        this.th_type = "GTE"
        this.variables.nagiosServiceName="TEST"
        def check = new OSCheck("chk_disk_busy_pct", th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_disk_busy_pct()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }


}
