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
        variables.instance="fwtest"
        variables.username = "oc4jadmin"
        variables.password = "Alexei12"
        variables.port = "6003"
        variables.host = "oas-lon-proj.dev.corelogic.local"
        variables.instance = "forms_dev"
        variables.nagiosServiceName="TEST"
        return variables
    }

    public createArguments() {
         def check = '''
             <check name="chk_oas_oper" warn="500" crit="1000" type="GTE">
	    <nagiosServiceName>chk_http_sessions_home</nagiosServiceName>
	    <port>6003</port>
	    <username>oc4jadmin</username>
		<password>Alexei12</password>
		
		<instance>forms_dev</instance>
		
		<!-- You may specify multiple mbean operations and attributes.  Each needs to be given a varName element to identify it. 
		     Each may also have a closureFunction to specify how to treat the value returned from the attribute or operation -->
		<operation>
		  <varName>HttpSessions</varName>
	          <mbeanPath>oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone</mbeanPath>
		  <operationName>executeQuery</operationName>
		  <operationArguments>HttpSessions</operationArguments>
		  <closureFunction>{ it -> it.split("\n").find { it =~ "Total Sessions" }.tokenize(' ').find{ it ==~ /^\\d+/ } }</closureFunction>
		</operation>

        	<operation>
		  <varName>HttpSessions2</varName>
	          <mbeanPath>oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone</mbeanPath>
		  <operationName>executeQuery</operationName>
		  <operationArguments>HttpSessions</operationArguments>
		  <closureFunction>{ it -> it.split("\n").find { it =~ "Total Sessions" }.tokenize(' ').find{ it ==~ /^\\d+/ } }</closureFunction>
		</operation>

		<!-- Use this closure to perform an operation on the variables you have collected - this represents the final value(s) which will be compared to your thresholds 
			It will receive a Map of key:values pairs, with the keys being the varName specified for each operation or attribute -->  
		<collectionClosure>{ def sum; it.each { value += it.value }; return sum }</collectionClosure>
   	        <host>oas-lon-proj.dev.corelogic.local</host>
	</check>
        '''
        
        return new XmlSlurper().parseText(check)
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
        c.argsAsXML = createArguments()
        def value = check.chk_oas_attr()
        assert check != null
        assert value != null
    }


    void testAttrAvgChk() {
        createMock()
        variables.timePeriodMillis = "60000"
        def check = new OASCheck(chk_name, th_warn, th_crit, th_type, variables)
        c.argsAsXML = createArguments()
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

