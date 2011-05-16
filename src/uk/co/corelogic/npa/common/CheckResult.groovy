package uk.co.corelogic.npa.common


/**
 * A CheckResult is the result of a check.  It is geared towards nagios checks.
 * It isn't the same as a metric, and can contain the results of a check which uses one or
 * more metrics.
*/

class CheckResult {

def id
def check_name
def hostname
def identifier
def status
def nagiosServiceName
def performance = [:]
def check_time
def message

CheckResult (check_name, hostname, status, performance, check_time, message) {
    // Check for nulls
    //assert check_name != null, 'Check name cannot be null!'
    assert hostname != null, 'hostname cannot be null!'
    assert status != null, 'status cannot be null!'
    //assert performance != null, 'performance cannot be null!'
    assert check_time != null, 'check_time cannot be null!'
    assert message != null, 'message cannot be null!'

    // Check for valid status type
    assert status == "WARNING" || "UNKNOWN" || "CRITICAL", 'Status type is invalid!'

    this.id=UUID.randomUUID();
	this.check_name=check_name
	this.hostname=hostname
	this.identifier=hostname
	this.status=status
	this.performance=performance
	this.check_time=check_time
	this.message=message
}

CheckResult (id, check_name, hostname, status, performance, check_time, message) {
    // Check for nulls
    assert id != null, 'ID cannot be null!'
    assert check_name != null, 'Check name cannot be null!'
    assert hostname != null, 'hostname cannot be null!'
    assert status != null, 'status cannot be null!'
    assert performance != null, 'performance cannot be null!'
    assert check_time != null, 'check_time cannot be null!'
    assert message != null, 'message cannot be null!'

    // Check for valid status type
    assert status == "WARNING" || "UNKNOWN" || "CRITICAL", 'Status type is invalid!'

    this.id=id
	this.check_name=check_name
	this.hostname=hostname
	this.identifier=hostname
	this.status=status
	this.performance=performance
	this.check_time=check_time
	this.message=message
}
    /**
        Return a string layout representation of the CheckResult object
    */
    public String returnString() {
        def obj = new StringBuffer()
        obj << "check_name: ${this.check_name}\n"
        obj << "hostname:" + this.hostname + "\n"
        obj << "identifier:" + this.identifier + "\n"
        obj << "status:" + this.status + "\n"
        obj << "performance:" + this.performance.toString() + "\n"
        obj << "check_time:" + this.check_time.toString() + "\n"
        obj << "message:" + this.message + "\n"
        return obj.toString()
    }
}
