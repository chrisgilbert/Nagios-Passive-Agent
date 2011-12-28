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
        this.variables.unitType = "percent"
        this.variables.nagiosServiceName="TEST"
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
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

        this.variables.volume="/"
        this.variables.nagiosServiceName="TEST"
        this.variables.unitType="percent"
        check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        result = check.chk_disk_free()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }

    void testChk_disk_free_multi() {
        this.variables.volume="/ /boot /home"
        this.variables.nagiosServiceName="TEST"
        this.variables.unitType="percent"
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_disk_free()
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


    void testChk_disk_free_avg() {
        this.variables.volume="ALL"
        this.variables.nagiosServiceName="TEST"
        this.variables.unitType="percent"
        this.variables.timePeriodMillis="600000"
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_disk_free()
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

    void testChk_cpu_pct_avg() {
        this.th_type = "GTE"
        this.variables.nagiosServiceName="TEST"
        this.variables.timePeriodMillis="600000"
        def check = new OSCheck("chk_cpu_pct", th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_cpu_pct()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }


    void testChk_mem_pct() {
        this.th_type = "LTE"
        this.variables.nagiosServiceName="TEST"
        def check = new OSCheck("chk_mem_used_pct", th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_mem_used_pct()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }

    void testChk_mem_pct_avg() {
        this.th_type = "LTE"
        this.variables.nagiosServiceName="TEST"
        this.variables.timePeriodMillis="600000"
        def check = new OSCheck("chk_mem_used_pct", th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_mem_used_pct()
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

    void testChk_disk_busy_pct_avg() {
        this.th_type = "GTE"
        this.variables.nagiosServiceName="TEST"
        this.variables.timePeriodMillis="600000"
        def check = new OSCheck("chk_disk_busy_pct", th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_disk_busy_pct()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }

}

