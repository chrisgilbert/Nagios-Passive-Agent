package uk.co.corelogic.npa.oas
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.common.*

class OASCheck extends Check {

String result
String host
String port
String user
String password


    public init() {
        if ( gatherer == null) {
            this.initiatorID  = UUID.randomUUID();
            this.gatherer = new ExternalGatherer(this.initiatorID)
            Log.debug("Gatherer initiator ID is ${this.initiatorID}")
        }
    }

    synchronized public OASCheck clone() {
        OASCheck clone = (OASCheck) super.makeClone(this.chk_name);
        clone.result = this.result;
        clone.host = this.host;
        clone.port = this.port;
        clone.user = this.user;
        clone.password = this.password;
        return clone;
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


}