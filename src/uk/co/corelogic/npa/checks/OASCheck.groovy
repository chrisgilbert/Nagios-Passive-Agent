package uk.co.corelogic.npa.oas
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.common.*

class OASCheck extends JMXCheck {

    synchronized public OASCheck clone() {
        OASCheck clone = (OASCheck) super.makeClone(this.chk_name);
    }
    OASCheck(chk_name, th_warn, th_crit, th_type, args) {
        super(chk_name, th_warn, th_crit, th_type, args)
        init()
    }
    OASCheck() {
        super()
    }
    
public initJMX(variables) {
    this.host = variables.host
    this.port = variables.port
    this.oc4jInstance = variables.oc4jInstance
    this.user = variables.user
    this.password = variables.password
    //bla bla
    //this.gatherer = new OracleGatherer(this.host, this.port, this.database, this.user, this.password, this.initiatorID)
}


    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        CheckRegister.add("chk_jmx_attr", "JMX", this.getClass().getName())
        CheckRegister.add("chk_jmx_oper", "JMX", this.getClass().getName())
    }

}