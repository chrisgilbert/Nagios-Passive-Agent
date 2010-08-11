package uk.co.corelogic.npa.tests.db
import uk.co.corelogic.npa.tests.NPATest
import uk.co.corelogic.npa.gatherers.OracleGatherer
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.NPA

/**
 * This class tests the OracleGatherer
 * @author Chris Gilbert
 */
class OracleCheckTest extends NPATest {

    private Check check;
   

    public getVariables() {
        def variables = [:]
        variables.host = "oracle10g.corelogic.local"
        variables.port = "1521"
        variables.database = "ora10test"
        variables.user = "fw34x"
        variables.password = "fw34x"
        variables.nagiosServiceName="test"
        variables.nagiosHostName="test"
        return variables
    }
    public void testInstatiate() {
        def check = new OracleCheck(this.getVariables())
        assert check != null
    }

    public void testRegister() {
        def check = new OracleCheck(this.getVariables())
        check.registerChecks()
    }

    public void testChkTablespace() {
        def check = new OracleCheck(this.getVariables())

        def variables = this.getVariables()
        
        variables.tablespace_name = "ALL"
        variables.check_name="chk_tablespace"
        
        def th_warn = 20
        def th_crit = 10
        def th_type = "LTE"

        
        def result = check.chkTablespace(variables, th_warn, th_crit, th_type)
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)

        variables.tablespace_name = "SYSTEM"

        result = check.chkTablespace(variables, th_warn, th_crit, th_type)
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }

        public void testChkTablespaceMB() {
        def check = new OracleCheck(this.getVariables())

        def variables = this.getVariables()

        variables.tablespace_name = "ALL"
        variables.check_name="chk_tablespace"
        variables.unitType="megabytes"

        def th_warn = 50
        def th_crit = 20
        def th_type = "LTE"


        def result = check.chkTablespace(variables, th_warn, th_crit, th_type)
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)

        variables.tablespace_name = "SYSTEM"

        result = check.chkTablespace(variables, th_warn, th_crit, th_type)
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
    }
    
    
    public void testChkBufferCache() {
        def check = new OracleCheck(this.getVariables())
        def th_warn = 95
        def th_crit = 90
        def th_type = "LTE"
        def vars = this.getVariables()

        def result = check.chkBufferCache(vars, th_warn, th_crit, th_type)
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)
        
    }

    public void testConnectionFail() {
        def vars = this.getVariables()
        vars.port=123
        try {
            def check = new OracleCheck(vars)
        } catch(e) {
            println("An exception was thrown here:")
            Log.error("STACK:", e)
        }
        def th_warn = 95
        def th_crit = 90
        def th_type = "LTE"

    }

}