/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.checks.WeblogicCheck

/**
 *
 * @author chris
 */
class WeblogicCheckTest extends NPATest {


    def chk_name = "chk_weblogic"
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
        variables.mbeanPath="com.bea:Location=AdminServer,ServerRuntime=AdminServer,Name=AdminServer_/mosaic,ApplicationRuntime=mosaic,Type=WebAppComponentRuntime"

        variables.attribute=""
        variables.attributeName="OpenSessionsCurrentCount"
        variables.closureFunction = null
        variables.username = "weblogic"
        variables.password = "c0r3l0g1c"
        variables.port = "7001"
        variables.host = "mscunix.corelogic.co.uk"
        variables.serverName = "AdminServer"
        variables.applicationName = "mosaic"
        variables.nagiosServiceName="TEST"
        return variables
    }

    void testInstatiate() {
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createMock())
        assert check != null
    }

    void testRegisterChecks() {
        createMock()
        this.variables.nagiosServiceName="TEST"
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        check.registerChecks()
    }

    void testAttrChk() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }


    void testAttrAvgChk() {
        createMock()
        variables.timePeriodMillis = "60000"
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }

    void testOptCheck() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="java.lang:Location=AdminServer,type=Memory"
        variables.operationName="gc"
        variables.operation=""
        variables.remove("attributeName")
        variables.remove("attribute")
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }

    void testOptWithClosureCheck() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.mbeanPath="java.lang:Location=AdminServer,type=Memory"
        variables.operationName="gc"
        variables.operation=""
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.closureFunction=/{ it -> println it }/
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }

        void testOptWithAvgClosureCheck() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.timePeriodMillis = "60000"
        variables.mbeanPath="java.lang:Location=AdminServer,type=Memory"
        variables.operationName="gc"
        variables.operation=""
        variables.remove("attributeName")
        variables.remove("attribute")
        variables.closureFunction=/{ it -> println it }/
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }


    void testAttrWithClosureCheck() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, variables)
        variables.closureFunction=/{ it -> it.each { println(it) } }/
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }

}

