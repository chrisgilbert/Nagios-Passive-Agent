package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.CheckResult
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.*

/**
 * This is the main osGatherer class which provides methods for retreiving OS metrics
 *
 */
class OSGatherer extends Gatherer {

String host
String ip_address
String os_name
String os_version
String kernel_version
String result

// These hold the return values temporarily
def free_space_mb = [:]
def used_space_mb = [:]
def total_space_mb = [:]

    public OSGatherer(initiatorID) {
        super(initiatorID);
        this.host = "hostname".execute().text.trim()
        //this.ip_address = "hostname".execute().text.trim()
        this.os_name = System.getProperty("os.name")
        this.os_version = System.getProperty("os.version")
        this.registerMetrics();
    }


    /**
    Register a list of metrics which are provided by this gatherer

    Add new valid metrics here
    */

    public void registerMetrics() {
        this.metricList.add('OS_DISK_MB_FREE')
        this.metricList.add('OS_DISK_MB_USED')
        this.metricList.add('OS_DISK_MB_TOTAL')
        this.metricList.add('OS_STATS_DISK_PCT')
        this.metricList.add('OS_CPU_PCT_USED_TOTAL')
        //this.metricList.add('OS_MEM_MB_FREE')
        //this.metricList.add('OS_MEM_MB_USED')
        //this.metricList.add('OS_MEM_MB_TOTAL')
        super.addValidMetricList(this.metricList, 'OS', this.getClass().getName())
    }



    /*
     * Get a percentage I/O wait on the overall physical disk system - this will return the highest value across all disks
     * so as to give a warning when required.  It could be more sophisticated...
     */
    public OS_STATS_DISK_PCT(variables) {

          def disk_pct
          def output
          def datestamp = MetricsDB.getNewDateTime()
          // This is used to group together related metrics
          def groupID  = UUID.randomUUID();

          // Create a MetricModel object and set the metric properties
          MetricModel mod = new MetricModel()
          mod.setMetricName("OS_STATS_WAIT_IO")
          mod.setMetricType("OS")
          mod.setMetricDataType("Double")
          mod.setIdentifier("TOTAL")
          mod.setHostName(this.host)
          mod.setInitiatorID(this.initiatorID)
          mod.setGroupID(groupID)


        // We need a command for each os type
        Log.debug("Detected OS type: ${this.os_name}")
	// Windows
	if (this.os_name ==~ /Window.*/ ) {
		def cmd = ["cmd", "/c", "wmic /node:localhost path Win32_PerfFormattedData_PerfDisk_PhysicalDisk where name='_Total' get PercentDiskTime /format:csv"]
            output = runCmd(cmd)
            Log.debug("Cmd output: $output")
            def output1 = output.toString().trim().tokenize(",")
            // Get the integer out of the output - in a groovy way :)
            def outputf = output1.find{ it ==~ /^\d+/ }
            Log.debug("Captured integer: $outputf")
            disk_pct = outputf.toString().toFloat()
	}
	// Linux
	if (this.os_name == 'Linux' ) {
		def cmd = ["sh", "-c", "iostat -dtx 5 2 | grep -v \"dm-\" | awk 'BEGIN {max=0} !/sd[a-z][0-9]/ && !/Device/ && !/Time/ && !/Linux/  { if(NR!=2) { if(/^\$/){print max;max=0}else if(max<\$12){max=\$12}  } }' | tail -1"]
            output = runCmd(cmd)
            Log.debug(output)
            disk_pct = (100 - output.toString().toFloat())
	}
	// Solaris
        // TODO: Make this more efficient
	if (this.os_name ==~ /SunO.*/ ) {

                def cmd1 = ["sh", "-c", "iostat -xT d 1 2 | grep -n %b | tail -1 | cut -d ':' -f 1"]
		def cmd2 = ["sh", "-c", "iostat -xT d 1 2 | wc -l"]
                def remLines = runCmd(cmd1)
                def tailLines =  ( runCmd(cmd2).toString().toDouble() - remLines)
                def cmd3 = ["sh", "-c", "iostat -xT d 5 2 | awk '{print \$10}' | tail -$tailLines | sort -n | tail -1"]

                output = runCmd(cmd3)
                Log.debug(output)
                disk_pct = (100 - output.toString().toFloat())
	}
    // Save the metric and return the value
        def m3 = persistMetric(mod, disk_pct, datestamp)
        return disk_pct

    }


    /**
     * Get all the volumes on the system
    */
    public getVolumes() {
    def output

    // We need a command for each os type
    Log.debug("Detected OS type: ${this.os_name}")
	// Windows
	if( this.os_name ==~ /Window.*/ ) {
    Log.debug("Getting windows filesystems.")
        def cmd1 = ["cmd","/u/c","fsutil fsinfo drives"]

            output = runCmd(cmd1)
            
            
            Log.debug("Cmd output: ${output.toString()}")

            def s1 = output.toString().trim().tokenize(":\\")
            def s2 = s1.each{ it.trim() }
            Log.debug("Split output: $s2")
            def outputf = []
            outputf = s2.findAll{ it != /Drives/ }
            Log.debug("Drive list: $outputf")

            def volumes = []
            outputf.each {
                def cmd2 = ["cmd","/u/c","fsutil fsinfo drivetype  ${it.trim()}:"]
                def output2 = runCmd(cmd2)
                if ( output2 =~ /Fixed/ ) {
                    volumes.add(it + ":")
                }
            }
            Log.debug("All fixed disks: $volumes")
            return volumes
	}
	// Linux
	if (this.os_name == 'Linux' ) {
        Log.debug("Getting linux filesystems.")

        def cmd = ["sh", "-c", "df -PlT -x tmpfs | grep -v blocks | awk '{print \$7}'"]

        output = runCmd(cmd)
        Log.debug(output)
        def volumes = output.toString().split()
        Log.debug("All local volumes (no tmpfs): $volumes" )
        return volumes
	}
	// Solaris
	if (this.os_name == 'SunOS' ) {
        Log.debug("Getting solaris filesystems.")
        // This could give unpredictable results... 
        def cmd = ["sh", "-c", "grep dsk /etc/vfstab | grep -v swap | awk '{print \$1}'"]

        output = runCmd(cmd)
        Log.debug(output)
        def volumes = output.toString().split()
        Log.debug("All dsk volumes in vfstab (no swap): $volumes" )
        return volumes

	}
    }


    /**
     * This method runs the appropriate command to retrieve OS disk space information
    */ 
    private getDiskSpace(variables) {

    def output
    def p
    def volume = variables.volume
    def metTOTAL
    def metFREE
    def metUSED

    def metricType = "OS"
    def metricDataType = "Double"
    def identifier = variables.volume

    def datestamp = MetricsDB.getNewDateTime()
    // This is used to group together related metrics
    def groupID  = UUID.randomUUID();

    // Create a MetricModel for the disk total metric
    MetricModel modTot = new MetricModel()
    modTot.setMetricName("OS_DISK_MB_TOTAL")
    modTot.setMetricType("OS")
    modTot.setMetricDataType("Double")
    modTot.setIdentifier(identifier)
    modTot.setHostName(this.host)
    modTot.setInitiatorID(this.initiatorID)
    modTot.setGroupID(groupID)

    // Create a MetricModel for the disk used metric
    MetricModel modUsed = new MetricModel()
    modUsed.setMetricName("OS_DISK_MB_USED")
    modUsed.setMetricType("OS")
    modUsed.setMetricDataType("Double")
    modUsed.setHostName(this.host)
    modUsed.setInstanceName(null)
    modUsed.setIdentifier(identifier)
    modUsed.setInitiatorID(this.initiatorID)
    modUsed.setGroupID(groupID)

    // Create a MetricModel for the disk free metric
    MetricModel modFree = new MetricModel()
    modFree.setMetricName("OS_DISK_MB_FREE")
    modFree.setMetricType("OS")
    modFree.setMetricDataType("Double")
    modFree.setIdentifier(identifier)
    modFree.setHostName(this.host)
    modFree.setInstanceName(null)
    modFree.setInitiatorID(this.initiatorID)
    modFree.setGroupID(groupID)


	// We need a command for each os type

    Log.debug("Detected OS type: ${this.os_name}")
	// Windows
	if(this.os_name ==~ /Window.*/ ) {
        Log.debug("Running windows disk check.")
		def cmd = ["cmd","/c","fsutil volume diskfree  ${volume.trim()}"]
        
            output = runCmd(cmd).toString().trim().tokenize("\n")
            Log.debug(output)
            def free = output.find{ it =~ /avail free bytes/ }
            def freef = free.toString().tokenize().findAll{ it =~ /\d+/ }
            Log.debug("Windows disk avail output:" + freef)

            def total = output.find{ it =~ /of bytes/}
            def totalf = total.toString().tokenize().findAll{ it =~ /\d+/}
            Log.debug("Windows disk total output:" + totalf)
            def used = totalf[0].toFloat() - freef[0].toFloat()

            metTOTAL = totalf[0].toFloat()/1024/1024
            metUSED = used.toFloat()/1024/1024
            metFREE = freef[0].toString().toFloat()/1024/1024
	}
	// Linux
	if (this.os_name == 'Linux' ) {

        def cmd = ["sh", "-c", "df -Pk $volume | grep -v blocks"]

        output = runCmd(cmd)
        Log.debug(output)
        def all = output.toString().split()

        Log.debug("Split output: $all")
        // Create new metrics
        metTOTAL = all[1].toFloat()/1024
        metUSED = all[2].toFloat()/1024
        metFREE = all[3].toFloat()/1024

	}

	// Solaris
	if (this.os_name == 'SunOS' ) {
        def cmd = ["sh", "-c", "df -k $volume | grep -v avail"]

        output = runCmd(cmd)
        Log.debug(output)
        def all = output.toString().split()
        Log.debug("Split output: $all")

        // Assign results
        metTOTAL = all[1].toFloat()/1024
        metUSED = all[2].toFloat()/1024
        metFREE = all[3].toFloat()/1024

	}

    // Create new metrics using metric models and values
    def m1 = persistMetric(modTot, metTOTAL, datestamp)
    def m2 = persistMetric(modUsed, metUSED, datestamp)
    def m3 = persistMetric(modFree, metFREE, datestamp)

    this.total_space_mb[(variables.identifier)] = metTOTAL
    this.used_space_mb[(variables.identifier)] = metUSED
    this.free_space_mb[(variables.identifier)] = metFREE
    
    }



    /**
     * This method gets a CPU busy percentage for all processors
    */
    private OS_CPU_PCT_USED_TOTAL(variables) {

    def cpu_pct
    def output
    def datestamp = MetricsDB.getNewDateTime()
    // This is used to group together related metrics
    def groupID  = UUID.randomUUID();

    // Create a MetricModel object and set the metric properties
    MetricModel mod = new MetricModel()
    mod.setMetricName("OS_CPU_PCT_USED_TOTAL")
    mod.setMetricType("OS")
    mod.setMetricDataType("Double")
    mod.setIdentifier("TOTAL")
    mod.setHostName(this.host)
    mod.setInitiatorID(this.initiatorID)
    mod.setGroupID(groupID)


    // Now get on with the actual work of collecting cpu data
    //
 
    // We need a command for each os type
    Log.debug("Detected OS type: ${this.os_name}")
	// Windows
	if (this.os_name ==~ /Window.*/ ) {
		def cmd = ["cmd", "/c", "wmic /node:localhost path Win32_PerfFormattedData_PerfOS_Processor where name='_Total' get PercentProcessorTime /format:csv"]
            output = runCmd(cmd)
            Log.debug("Cmd output: $output")
            def output1 = output.toString().trim().tokenize(",")
            // Get the integer out of the output - in a groovy way :)
            def outputf = output1.find{ it ==~ /^\d+/ }
            Log.debug("Captured integer: $outputf")
            cpu_pct = outputf.toString().toFloat()
	}
	// Linux
	if (this.os_name == 'Linux' ) {
		def cmd = ["sh", "-c", "iostat -c 2 2 | tail -2  | grep -v '^\$' | awk '{print \$6}'"]
            output = runCmd(cmd)
            Log.debug(output)
            cpu_pct = (100 - output.toString().toFloat())
	}
	// Solaris
	if (this.os_name ==~ /SunO.*/ ) {
		def cmd = ["sh", "-c", "iostat -szcr 2 2 | grep -v cpu | grep -v id | cut -f 4 -d ',' | tail -1"]
            output = runCmd(cmd)
            Log.debug(output)
            cpu_pct = (100 - output.toString().toFloat())
	}
    // Save the metric and return the value
        def m3 = persistMetric(mod, cpu_pct, datestamp)
        return cpu_pct
    }
    

    private OS_DISK_MB_FREE(variables) {
        Log.debug(this.free_space_mb)
        if ( ! this.free_space_mb.containsKey((variables.identifier)) ) {
            Log.debug("Running disk check on OS - ${variables.identifier}")
            getDiskSpace(variables)
        }
        return this.free_space_mb[variables.identifier]
    }

    private OS_DISK_MB_USED(variables) {
        Log.debug(this.used_space_mb)
        if ( ! this.used_space_mb.containsKey((variables.identifier))  ) {
            Log.debug("Running disk check on OS - ${variables.identifier}")
            getDiskSpace(variables)
        }
        return this.used_space_mb[variables.identifier]
    }

    private OS_DISK_MB_TOTAL(variables) {
        Log.debug(this.total_space_mb)
        if ( ! this.free_space_mb.containsKey((variables.identifier)) ) {
            Log.debug("Running disk check on OS - ${variables.identifier}")
            getDiskSpace(variables)
        }
        return this.total_space_mb[variables.identifier]
    }

    
    // Method to execute OS commands
    public runCmd (cmd) {

     def stdout = new StringBuffer()
     def stderr = new StringBuffer()
     def p
     try {
            Log.debug("Running command: $cmd" )
            p = cmd.execute()
            p.waitForProcessOutput(stdout, stderr)
            
            Log.debug("Waiting for command to return...")
            //Log.debug("Trying to get text: $p.text")
            
            p.out.flush()
            
            p.waitForOrKill(30000)

            if (p.exitValue() == 0 ) { 
                Log.debug("Stream: $stdout $stderr")
                p.out.close()
                return stdout
            } else {
                Log.error("Error occurred: $stderr");
                return stdout + stderr
            }
     } catch (e) {
         Log.error("Exception thrown running command!", e)
     }
    }

    // Method to execute OS commands and retunr exit status and output
    public runExternalCmd(cmd) {

     def stdout = new StringBuffer()
     def stderr = new StringBuffer()
     def p
     try {
            Log.debug("Running command: $cmd" )
            p = cmd.execute().waitForProcessOutput(stdout, stderr)    

            Log.debug("Waiting for command to return...")
            //Log.debug("Trying to get text: $p.text")
            //p.out.flush()

            //p.waitForOrKill(30000)
            //p.out.close()
            Log.debug("Stream: $stdout $stderr")
            return ([p.exitValue(), "$stdout $stderr"])

     } catch (e) {
         Log.error("Exception thrown running command!", e)
     }
    }

}