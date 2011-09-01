/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.checks.OASCheck

/**
 *
 * @author chris
 */
class OASCheckTest extends NPATest {


    def chk_name = "chk_oas_attr"
    int th_warn = 1
    int th_crit = 2
    def th_type = "LTE"
    def variables = [:]

    void setup(){
        createMock()
    }

    void createMock() {
        variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.instance="fwtest"
        variables.attribute=""
        variables.attributeName="serverVersion"
        variables.closureFunction = null
        variables.username = "oc4jadmin"
        variables.password = "Alexei12"
        variables.port = "6003"
        variables.host = "oas-lon-proj.dev.corelogic.local"
        variables.instance = "forms_dev"
        variables.nagiosServiceName="TEST"
        this.variables = variables
    }

    void testInstatiate() {
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
    }

    void testRegisterChecks() {
        this.variables.nagiosServiceName="TEST"
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        check.registerChecks()
    }

    void testAttrChk() {
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        def value = chk_oas_attr()
        assert check != null
        assert value != null
    }

    void testOptCheck() {
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)   
        variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.operationName="checkSharedLibraryExists"
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.operationArguments=["global.libraries","1.0"]
        def value = chk_oas_oper()
        assert check != null
        assert value != null
    }

    void testOptWithClosureCheck() {
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone"
        variables.operationName="executeQuery"
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.closureFunction={ it.split("\n").find { it =~ "Total Sessions" }.tokenize(" ").find{ it ==~ /^\d+/ } }
        variables.operationArguments=["HTTPSessions"]
        def value = chk_oper_attr()
        assert check != null
        assert value != null
    }

    void testAttrWithClosureCheck() {
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.remove("operationName")
        variables.remove("operation")
        variables.remove("operationArguments")
        variables.closureFunction={ it.each { println(it) } }
        variables.attribute=""
        variables.attributeName="serverVersion"
        def value = chk_oas_attr()
        assert check != null
        assert value != null
    }

}

