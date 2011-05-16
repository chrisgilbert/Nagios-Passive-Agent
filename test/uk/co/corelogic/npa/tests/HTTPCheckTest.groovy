/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*

/**
 *
 * @author chris
 */
class HTTPCheckTest extends NPATest {

    def chk_name = "chk_http"
    int th_warn = 1000
    int th_crit = 5000
    def th_type = "GTE"
    def variables = [:]

        public getVariables() {
            def variables = [:]
            variables.url = ""
            variables.nagiosServiceName = "chk_http_google"
            variables.serverpath="http://www.google.co.uk"
            variables.url="/"
            variables.host="www.google.co.uk"
            return variables
        }
        public void testInstatiate() {
            def check = new HTTPCheck(this.chk_name, this.th_warn, this.th_crit, this.th_type, this.getVariables())
            assert check != null
        }

        public void testRegister() {
            def check = new HTTPCheck(this.chk_name, this.th_warn, this.th_crit, this.th_type, this.getVariables())
            check.registerChecks()
        }

        public void testChk() {
            def variables = this.getVariables()
            def check = new HTTPCheck(this.chk_name, this.th_warn, this.th_crit, this.th_type, this.getVariables())
            check.registerChecks()
            def result = check.chk_http()
            assert result != null
        }

}

