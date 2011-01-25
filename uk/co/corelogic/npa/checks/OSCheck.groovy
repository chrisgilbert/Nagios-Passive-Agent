package uk.co.corelogic.npa.checks
import uk.co.corelogic.npa.gatherers.OSGatherer
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.os.*

public class OSCheck extends Check {


public init() {
    if ( this.initiatorID == null ) {
        this.initiatorID  = UUID.randomUUID();
    }
    this.gatherer = new OSGatherer(this.initiatorID)
    Log.debug("Gatherer initiator ID is ${this.initiatorID}")
    }

    OSCheck(chk_name, th_warn, th_crit, th_type, args) {
        super(chk_name, th_warn, th_crit, th_type, args)
        init()
    }
    OSCheck() {
        super()
    }

    synchronized public clone() {
        OSCheck clone = (OSCheck) super.makeClone(this.chk_name);
        return clone;
    }


public chk_disk_free() {
    init()
    if ( this.chk_args.volume == "ALL" ) {
        return this.chkAllDisks(this.chk_args.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
    } else {
        return this.chkDiskFree(this.chk_args.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
    }
}
public chk_cpu_pct() {
    init()
    return this.chkCpuPct(this.chk_args.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
}

public chk_disk_pct() {
    init()
    return this.chkDiskPct(this.chk_args.clone(), this.chk_th_warn, this.chk_th_crit, this.chk_th_type)
}

public CheckResult chkAllDisks(variables, th_warn, th_crit, th_type) {   
    init()
    // Check for null values
    assert variables != null, 'Variables are null!'
    assert th_warn != null, 'th_warn cannot be null!'
    assert th_crit != null, 'th_crit cannot be null!'
    assert th_type != null, 'th_type cannot be null here!'
    assert variables.nagiosServiceName != null, 'nagiosServiceName cannot be null here!'

    def free_space_mb = [:]
    def total_space_mb = [:]
    def used_space_mb = [:]
    def performance = [:]
    def message = ""
    def maxstatus = "UNKNOWN"
    def each_space_free = []
    Log.debug("Running ALL filesystems check.")

        this.gatherer.getVolumes().each{
            def vars = [:]
            vars.volume=it
            vars.identifier=it
            
            free_space_mb[it] = this.gatherer.sample("OS_DISK_MB_FREE", vars).toFloat()
            used_space_mb[it] = this.gatherer.sample("OS_DISK_MB_USED", vars).toFloat()
            total_space_mb[it] = this.gatherer.sample("OS_DISK_MB_TOTAL", vars).toFloat()
            Log.debug("Checks returned: $it - ${free_space_mb[it]} : ${used_space_mb[it]} : ${total_space_mb[it]} ")
            performance[it]="${total_space_mb[it]};${used_space_mb[it]};${free_space_mb[it]}"


            // Get value
            def value
            if (variables.unitType == "percent") {
                if (free_space_mb[it] == 0) {
                    value = 0
                } else {
                    def calc  = (free_space_mb[it] / total_space_mb[it])*100
                    // Round to 2 decimal places.
                    BigDecimal bd = new BigDecimal(calc);
                    bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
                    value = bd.doubleValue();
                    Log.debug("Free on $it $value %")
                    message = message + "- $it - $value %"
                }
            } else {
                value = free_space_mb[it]
                Log.debug("Free on $it $free_space_mb[it] MB")
                message = message + " - $it - $free_space_mb[it] MB"
            }

            each_space_free.add(value)
            // Now check for threshold levels
            def status = super.calculateStatus(th_warn, th_crit, value, th_type)
            Log.debug("Sub status for $it is $status.")


    }
    maxstatus = super.calculateStatus(th_warn, th_crit, each_space_free, th_type)
    Log.debug("Status is $maxstatus")
    Log.debug("Generating result from values: ${this.initiatorID}, ${variables.nagiosServiceName}, ${this.gatherer.host}, $maxstatus, $performance, [new date], $message")
    return super.generateResult(this.initiatorID, variables.nagiosServiceName, this.gatherer.host, maxstatus, performance, new Date(), message)
    this.gatherer = null;
}

public CheckResult chkDiskFree(variables, th_warn, th_crit, th_type) {
    init()
    def output
    def free_space_mb
    def total_space_mb
    def used_space_mb
    def p
    def status
    def message = "Space free -"
    def performance = [:]
    Log.debug("Running single filesystem check.")

    // Get some samples of required metrics
    free_space_mb = this.gatherer.sample("OS_DISK_MB_FREE", variables).toFloat()
    used_space_mb = this.gatherer.sample("OS_DISK_MB_USED", variables).toFloat()
    total_space_mb = this.gatherer.sample("OS_DISK_MB_TOTAL", variables).toFloat()

    Log.debug("Checks returned: $free_space_mb : $used_space_mb : $total_space_mb ")
    performance=[(variables.volume):"${total_space_mb};${used_space_mb};${free_space_mb}"]
    message = "Results of chk_disk_free is null!"

    // Get value

    def value
    if (variables.unitType == "percent") {
        if (free_space_mb == 0) {
            value = 0
        } else {
            value = (free_space_mb / total_space_mb)*100
            Log.debug("Space free on $variables.volume: $value %")
            message = "$variables.volume - $value %"
        }
    } else {
        value = free_space_mb
        Log.debug("Space free on $variables.volume: $free_space_mb MB")
        message = "$variables.volume - $free_space_mb MB"
    }

    // Now check for threshold levels
    status = super.calculateStatus(th_warn, th_crit, value, th_type)
    Log.debug("Generating result from values: ${this.initiatorID}, ${variables.nagiosServiceName}, ${this.gatherer.host}, $status, $performance, [new date], $message")
    return super.generateResult(this.initiatorID, variables.nagiosServiceName, this.gatherer.host, status, performance, new Date(), message)
    this.gatherer = null;
}


public CheckResult chkCpuPct(variables, th_warn, th_crit, th_type) {
    init()
    float cpu_pct
    def message = ""
    String status
    assert variables.nagiosServiceName != null, 'nagiosServiceName is null!'
    def performance = [:]
    
    cpu_pct = this.gatherer.sample("OS_CPU_PCT_USED_TOTAL", [:]).toFloat()
    // Round to 2 decimal places.
    BigDecimal bd = new BigDecimal(cpu_pct);
    bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    def value = bd.doubleValue();
    message = "Results of chk_cpu_pct: $value %"
    performance = ["cpu_pct":cpu_pct]

    status = super.calculateStatus(th_warn, th_crit, cpu_pct, th_type)
    Log.debug("Generating result from values: ${this.initiatorID}, ${variables.nagiosServiceName}, ${this.gatherer.host}, $status, $performance, [new date], $message")
    return super.generateResult(this.initiatorID, variables.nagiosServiceName, this.gatherer.host, status, performance, new Date(), message)
    this.gatherer = null;

}

public CheckResult chkDiskPct(variables, th_warn, th_crit, th_type) {
    init()
    float disk_pct
    def output
    def p
    def performance = [:]
    def message = ""

    String status

    disk_pct = this.gatherer.sample("OS_STATS_DISK_PCT", variables).toFloat()
    Log.debug("Disk time values: $disk_pct $th_warn $th_crit")
    performance = ["disk_pct":disk_pct]
    message = "Results of chk_disk_pct - highest value for all disks: $disk_pct %"

    // Now check for threshold levels
    status = super.calculateStatus(th_warn, th_crit, disk_pct, th_type)
    return super.generateResult(this.initiatorID, variables.nagiosServiceName, this.gatherer.host, status, performance, new Date(), message)
    this.gatherer = null;
}


/**
 * Register all the checks which this class implements
*/
public registerChecks() {
    CheckRegister.add("chk_disk_free", "OS", this.getClass().getName())
    CheckRegister.add("chk_cpu_pct", "OS", this.getClass().getName())
    CheckRegister.add("chk_disk_pct", "OS", this.getClass().getName())
}


/*
chk_load {

}

chk_free_mem {

}

chk_pct_free_mem {

}

chk_io_wait {

}
*/

}



