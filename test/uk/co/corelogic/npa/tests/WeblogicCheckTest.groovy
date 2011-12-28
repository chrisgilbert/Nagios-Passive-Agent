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


        public createOperations() {
         def check = '''
             <check name="chk_oas" warn="500" crit="1000" type="GTE">
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
		  <closureFunction>{ it -> it.split("\\n").find { it =~ "Total Sessions" }.tokenize(' ').find{ it ==~ /^\\d+/ } }</closureFunction>
		</operation>

        	<operation>
		  <varName>HttpSessions2</varName>
	          <mbeanPath>oc4j:j2eeType=ClassLoading,name=singleton,J2EEServer=standalone</mbeanPath>
		  <operationName>executeQuery</operationName>
		  <operationArguments>HttpSessions</operationArguments>
		  <closureFunction>{ it -> it.split("\\n").find { it =~ "Total Sessions" }.tokenize(' ').find{ it ==~ /^\\d+/ } }</closureFunction>
		</operation>

		<!-- Use this closure to perform an operation on the variables you have collected - this represents the final value(s) which will be compared to your thresholds
			It will receive a Map of key:values pairs, with the keys being the varName specified for each operation or attribute -->
		<collectionClosure>{ it -> def sum = 0; it.each { sum += it.value }; return sum }</collectionClosure>
   	        <host>oas-lon-proj.dev.corelogic.local</host>
	</check>
        '''

        return new XmlSlurper().parseText(check)
    }

    def createAttributes(){
        def check = '''
        <check name="chk_weblogic" warn="-1" crit="-1" type="LTE">
	    <nagiosServiceName>chk_heap_free_pct</nagiosServiceName>
	    <port>7001</port>
	    <username>weblogic</username>
		<password>c0r3l0g1c</password>
		<applicationName>mosaic</applicationName>
                <serverName>AdminServer</serverName>
		<attribute>
                  <varName>OpenSessionsCurrentCount</varName>
	          <mbeanPath>ALLSERVERS</mbeanPath>
		  <attributeName>OpenSessionsCurrentCount</attributeName>
		</attribute>
                <collectionClosure>{ it ->  it['OpenSessionsCurrentCount'].sum() }</collectionClosure>
	    <host>rel-wls11g.corelogic.local</host>
	</check>
        '''
        return new XmlSlurper().parseText(check)
    }



    public createMock() {
        variables = [:]
        //variables.mbeanPath="oc4j:j2eeType=J2EEServer,name=standalone"
        variables.mbeanPath="com.bea:Location=AdminServer,ServerRuntime=AdminServer,Name=AdminServer_/mosaic,ApplicationRuntime=mosaic,Type=WebAppComponentRuntime"

        variables.attribute=""
        variables.attributeName="OpenSessionsCurrentCount"
        variables.closureFunction = null

        variables.applicationName = "mosaic"
        variables.nagiosServiceName="TEST"
        return variables
    }

    void testInstatiate() {
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createAttributes())
        assert check != null
    }

    void testRegisterChecks() {
        createMock()
        this.variables.nagiosServiceName="TEST"
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createAttributes())
        assert check != null
        check.registerChecks()
    }

    void testAttrChk() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createAttributes())
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }


    void testAttrAvgChk() {
        createMock()
        variables.timePeriodMillis = "60000"
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createAttributes())
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }

    void testOptCheck() {
        createMock()
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createOperations())
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
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createOperations())
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
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createOperations())
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
        def check = new WeblogicCheck(chk_name, th_warn, th_crit, th_type, createAttributes())
        variables.closureFunction=/{ it -> it.each { println(it) } }/
        def value = check.chk_weblogic()
        assert check != null
        assert value != null
    }

}

