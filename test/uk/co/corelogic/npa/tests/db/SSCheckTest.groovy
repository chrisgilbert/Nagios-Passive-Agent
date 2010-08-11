package uk.co.corelogic.npa.tests.db
import uk.co.corelogic.npa.tests.NPATest
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.checks.*

/**
 *
 * @author chris
 */
class SSCheckTest extends NPATest {

    def th_warn = 1
    def th_crit = 2
    def th_type = "GTE"
	
    public getVars(){
       def variables = [:]
       variables.host = "ss2005-test1.corelogic.local"
       variables.port = "1433"
       variables.database = "fw35x"
       variables.user = "fw"
       variables.password = "fw"
       variables.initiatorID = "TEST"
       variables.nagiosServiceName="chk_blocking_procs"
       return variables.clone()
    }

    public void testInstantiate(){
        SSCheck c = new SSCheck();
        c.chk_th_warn = 1
        c.chk_th_crit = 2
        c.chk_th_type = "GTE"
        c.chk_args = getVars()
        c.variables = getVars()
        c.init()
    }

    public void testRegister(){
        SSCheck c = new SSCheck();
        c.registerChecks()
    }
    
    public void testChk_ss_blocking_procs(){
        SSCheck c = new SSCheck();
        c.chk_th_warn = 1
        c.chk_th_crit = 2
        c.chk_th_type = "GTE"
        c.chk_name = "chk_ss_blocking_procs"
        c.chk_args = getVars()
        c.variables = getVars()
        c.variables.nagiosServiceName = "chk_ss_blocking_procs"
        c.chk_args.nagiosServiceName = "chk_ss_blocking_procs"
        def result = c.chk_ss_blocking_procs().toString()
        println(result)
        assert result != null, "Did not return a valid value (is null)"
    }

    public void testChk_ss_data_files_free_pct(){
        SSCheck c = new SSCheck();
        c.chk_th_warn = 10
        c.chk_th_crit = 5
        c.chk_th_type = "LTE"
        c.chk_name = "chk_ss_data_files_free"
        c.chk_args = getVars()
        c.variables = getVars()
        c.variables.nagiosServiceName = "chk_ss_data_files_free"
        c.chk_args.nagiosServiceName = "chk_ss_data_files_free"
        c.chk_args.unitType = "percent"
        println("Testing for data file free space")
        def result = c.chk_ss_data_files_free().toString()
        println(result)
        assert result != null, "Did not return a valid value (is null)"
    }

    public void testChk_ss_data_file_free_meg(){
        SSCheck c = new SSCheck();
        c.chk_th_warn = 10
        c.chk_th_crit = 5
        c.chk_th_type = "LTE"
        c.chk_name = "chk_ss_data_file_free"
        c.chk_args = getVars()
        c.variables = getVars()
        c.variables.nagiosServiceName = "chk_ss_data_file_free"
        c.chk_args.nagiosServiceName = "chk_ss_data_file_free"
        c.chk_args.unitType = "megabytes"
        println("Testing for data file free space")
        def result = c.chk_ss_data_files_free().toString()
        println(result)
        assert result != null, "Did not return a valid value (is null)"
    }

    public void testConnectionFail() {
    
    def vars = this.getVars()
    vars.port=123
    try {
        def check = new SSCheck(vars)
        c.chk_th_warn = 1
        c.chk_th_crit = 2
        c.chk_th_type = "GTE"
    } catch(e) {
        println("An exception was thrown here:")
        Log.error("STACK:", e)
        assert true
    }
    def th_warn = 95
    def th_crit = 90
    def th_type = "LTE"

    }
}

