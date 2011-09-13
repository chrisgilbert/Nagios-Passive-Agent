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
    def th_warn = "10.1.3.4.0"
    def th_crit = "10.1.3.2.0"
    def th_type = "CONTAINS"
    def variables = [:]

    void setup(){
        createMock()
    }

    public createMock() {
        variables = [:]
        //variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.mbeanPath="oc4j:j2eeType=JVM,name=single,J2EEServer=standalone"

        variables.instance="fwtest"
        variables.attribute=""
        variables.attributeName="freeMemory"
        variables.closureFunction = null
        variables.username = "oc4jadmin"
        variables.password = "Alexei12"
        variables.port = "6003"
        variables.host = "oas-lon-proj.dev.corelogic.local"
        variables.instance = "forms_dev"
        variables.nagiosServiceName="TEST"
        return variables
    }

    void testInstatiate() {
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, createMock())
        assert check != null
    }

    void testRegisterChecks() {
        createMock()
        this.variables.nagiosServiceName="TEST"
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        check.registerChecks()
    }

    void testAttrChk() {
        createMock()
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        def value = check.chk_oas_attr()
        assert check != null
        assert value != null
    }


    void testAttrAvgChk() {
        createMock()
        variables.timePeriodMillis = "60000"
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        def value = check.chk_oas_attr()
        assert check != null
        assert value != null
    }

    void testOptCheck() {
        createMock()
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)   
        variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.operationName="checkSharedLibraryExists"
        variables.operation=""
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.operationArguments=["global.libraries","1.0"] as String[]
        def value = check.chk_oas_oper()
        assert check != null
        assert value != null
    }

    void testOptWithClosureCheck() {
        createMock()
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone"
        variables.operationName="executeQuery"
        variables.operation=""
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.closureFunction=/{ it -> it.split("\n").find { it =~ "Total Sessions" }.tokenize(" ").find{ it ==~ \/^\d+\/ } }/
        variables.operationArguments="HttpSessions"
        def value = check.chk_oas_oper()
        assert check != null
        assert value != null
    }

        void testOptWithAvgClosureCheck() {
        createMock()
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone"
        variables.operationName="executeQuery"
        variables.operation=""
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.timePeriodMillis = "60000"
        variables.closureFunction=/{ it -> it.split("\n").find { it =~ "Total Sessions" }.tokenize(" ").find{ it ==~ \/^\d+\/ } }/
        variables.operationArguments="HttpSessions"
        def value = check.chk_oas_oper()
        assert check != null
        assert value != null
    }


    void testAttrWithClosureCheck() {
        createMock()
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.remove("operationName")
        variables.remove("operation")
        variables.remove("operationArguments")
        variables.closureFunction=/{ it -> it.each { println(it) } }/
        variables.attribute=""
        variables.attributeName="serverVersion"
        def value = check.chk_oas_attr()
        assert check != null
        assert value != null
    }

}

