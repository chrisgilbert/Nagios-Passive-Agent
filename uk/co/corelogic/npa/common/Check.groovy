package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.os.*
import uk.co.corelogic.npa.oas.*
import java.util.timer.*
import java.util.Random


abstract class Check extends TimerTask implements Cloneable {

def chk_name
def chk_category
def chk_interval
def chk_th_warn
def chk_th_crit
def chk_th_type
def chk_args = [:]
def sockOutput = new StringBuffer()
def threadID;
def initiatorID
def gatherer
def variables

    Check(chk_name, th_warn, th_crit, th_type, args) {
        this.chk_name = chk_name
        this.chk_th_warn = th_warn
        this.chk_th_crit = th_crit
        this.chk_th_type = th_type
        this.chk_args = args
    }

    Check() {
    }

    synchronized public Check makeClone(chk_name) {
        Check clone = CheckFactory.getCheck(chk_name)
        clone.chk_name = this.chk_name;
        clone.chk_category = this.chk_category;
        clone.chk_interval = this.chk_interval;
        clone.chk_th_warn = this.chk_th_warn;
        clone.chk_th_crit = this.chk_th_crit;
        clone.chk_th_type = this.chk_th_type;
        clone.chk_args = this.chk_args;
        clone.gatherer = null;
        clone.variables = this.variables;
        return clone;
    }



    // Constructor for checks triggered from interactive shell
    Check(cmd) {
        def cmdArray = cmd.split()
        try {
            this.sockOutput << "Output:\n"
            this.chk_name = cmdArray[1].toString()
            this.chk_th_warn = cmdArray[2].toDouble()
            this.chk_th_crit = cmdArray[3].toDouble()
            this.chk_th_type = cmdArray[4].toString()

            // Parse the arguments as value pairs using an '=' sign seperator
            def arrLen = cmdArray.length
            if (arrLen > 5) {
                String[] args = cmdArray[5..arrLen-1]
                args.each {
                    def tokens=it.tokenize("=")
                
                    this.chk_args[(tokens[0])] = tokens[1]
                }
            }
            
            this.run()

        } catch(e) {
            Log.error("Error parsing FMD command. ", e)
            this.sockOutput << e.toString()
            this.sockOutput << "Invalid check arguments!\n"
            this.sockOutput << "Please checks you have supplied all the required arguments for the check."
            this.sockOutput << "Usage: check chk_name(String) th_warn(double) th_crit(double) th_type(String) [argument1=arg1 argument2=arg2 ...]\n"
        }

    }

    String returnString() {
        return this.sockOutput
    }


    /**
     * Generic run method which executes the check
    */
    public void run() {
         try {
            this.threadID = Thread.currentThread().getId();
            CheckScheduler.registerThread(Thread.currentThread(), this.clone());
            Log.debug("Thread $threadID status: " + Thread.currentThread().getState())
            Log.debug("Running check with parameters: ${this.chk_name}, [${this.chk_args}, ${this.chk_th_warn}, ${this.chk_th_crit}, ${this.chk_th_type}]")
                CheckResultsQueue.add(this.invokeMethod(this.chk_name.trim(), null))
        } catch(e) {
            Log.error("Exception occurred whilst running $chk_name check: ", e)
            Log.error("STACK:", e)
        }
    }



    /**
     * Calculate a nagios status based on values vs thresholds
     *
    */
    public String calculateStatus(th_warn, th_crit, value, th_type) {
        def status = "UNKNOWN"
        if (th_type == null ) { th_type = "GTE" }

        switch ( th_type ) {

            case "GTE":     if ( value >= th_warn ) { status = "WARNING" }
                            if ( value >= th_crit ) { status = "CRITICAL" }
                            if ( (th_crit > value) && (th_warn > value) ) { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;

            case "GT":      if ( value > th_warn ) { status = "WARNING" }
                            if ( value > th_crit ) { status = "CRITICAL" }
                            if ( ( value <= th_warn ) && ( value <= th_crit ) ) { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;

            case "LTE":     if ( value <= th_warn ) { status = "WARNING" }
                            if ( value <= th_crit ) { status = "CRITICAL" }
                            if ( (value > th_crit ) && ( value > th_warn ) ) { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;

            case "LT":      if ( value < th_warn ) { status = "WARNING" }
                            if ( value < th_crit ) { status = "CRITICAL" }
                            if ( (value >= th_crit) && (value >= th_warn) ) { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;

            case "EQ":      if ( th_crit == value ) { status = "CRITICAL" }
                            if ( th_crit != value ) { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;

            case "CONTAINS":   if ( value =~ th_crit)  { status = "CRITICAL" }
                            else { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;
                            
            default:        Log.warn("Comparison type did not match known values - will be treated as GTE!")
                            if ( value >= th_warn ) { status = "WARNING" }
                            if ( value >= th_crit ) { status = "CRITICAL" }
                            if ( (th_crit > value) && (th_warn > value) ) { status = "OK" }
                            if ( value == -1 ) { status = "UNKNOWN" }
                            break;
        }
        Log.debug("Status for $chk_name is $status: Type of comparison: $th_type th_warn: $th_warn th_crit: $th_crit value: $value")
        return status
    }

     /**
     * Calculate a nagios status based on a collection of values vs thresholds
     *
    */
    public String calculateStatus(th_warn, th_crit, ArrayList values, th_type) {
        def maxstatus = "UNKNOWN"
        values.each {
            def status = this.calculateStatus(th_warn, th_crit, it, th_type)
            
            if ((status == "OK") && (maxstatus != "CRITICAL")  && (maxstatus != "WARNING")) { maxstatus = "OK" }
            if ((status == "WARNING") && (maxstatus != "CRITICAL")) { maxstatus = "WARNING" }
            if (status == "CRITICAL") { maxstatus = "CRITICAL" }
        }
        return maxstatus
    }


    /**
     * Generate a new CheckResult object for the result of this check
    */
    public CheckResult generateResult(ID, nagiosServiceName, host, status, performance, date, message) {
        try {
            Log.debug("Generating result from: $ID, $nagiosServiceName, $host, $status, $performance, date, $message")
            return new CheckResult(ID, nagiosServiceName, host, status, performance, date, message)
        } catch (e) {
            Log.error("Exception occurred when generating check result.", e)
            Log.error("STACK:", e)
        }
    }

    public schedule(interval) {
        this.chk_interval = interval;
        CheckScheduler.schedule(this.clone());
     }

//    // Method to execute OS commands
//    public runCmd (cmd) {
//
//     def stdout = new StringBuffer()
//     def stderr = new StringBuffer()
//     def p
//     try {
//            Log.debug("Running command: $cmd" )
//            p = cmd.execute()
//
//
//            p.consumeProcessOutput(stdout, stderr)
//            Log.debug("Waiting for command to return...")
//            Log.debug("Trying to get text: $p.text")
//            p.out.flush()
//            //p.out.close()
//            p.waitForOrKill(30000)
//
//            Log.debug("Stream: $stdout $stderr")
//            return [p.exitValue(), stdout]
//
//     } catch (e) {
//         Log.error("Exception thrown running command!", e)
//         Log.error("STACK:", e)
//     }
//
//
//    }
       // Method to execute OS commands
    public runCmd (cmd) {

     def stdout = new StringBuffer()
     def stderr = new StringBuffer()
     def p
     try {
            Log.debug("Running command: $cmd" )
            p = cmd.execute()

            

            Log.debug("Waiting for command to return...")
            Log.debug("Trying to get text: $p.text")
            println("Flushing output.")
            p.out.flush()
            p.waitForProcessOutput(stdout, stderr)
            //p.waitForOrKill(30000)

            if (p.exitValue() == 0 ) {
                Log.debug("Stream: $stdout $stderr")
                println("Exit value was 0.")
                p.out.close()
                return stdout
            } else {
                Log.error("Error occurred: $stderr");
                println("Exit value was not 0.")
                return stdout + stderr
            }
     } catch (e) {
         println("Exception occurred.")
         Log.error("Exception thrown running command!", e)
     }
    }

}