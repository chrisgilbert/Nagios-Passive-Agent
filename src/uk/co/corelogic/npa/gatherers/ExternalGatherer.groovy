package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.*
import java.io.FileNotFoundException

/**
 * This class allows external scripts to be used to gather metrics
 *
**/
class ExternalGatherer extends Gatherer {

    def scriptName
    def scriptType
    def returnType
    def returnValue
    def dataType
    def scriptArgs
    def metricName
    def instanceName
    def identifier
    def initiatorID
    def host
    def saveMetrics
    def metricList = []

    String os_name
    String os_version
    String result


    ExternalGatherer(initiatorID) {
        this.initiatorID = initiatorID;
        this.host = "hostname".execute().text.trim()
        this.os_name = System.getProperty("os.name")
        this.os_version = System.getProperty("os.version")
    }

    ExternalGatherer() {
        super()
        this.host = "hostname".execute().text.trim()
        this.os_name = System.getProperty("os.name")
        this.os_version = System.getProperty("os.version")
    }

    public void registerMetrics() {
        this.metricList.add('EXT_OUTPUT_VALUE')
        this.metricList.add('NAGIOS_SCRIPT_VALUES')
        super.addValidMetricList(this.metricList, "EXTERNAL", this.getClass().getName())
    }
    private executeScript() {

        // First try to create a new file object

       try {
           File script = new File(scriptName)
           if (script.exists()) {
               if ( scriptType == "shell" ) {
                   return executeShellScript()
               }
               else if ( scriptType == "groovy" ) {
                   return executeGroovyScript()
               }
               else {
                   throw TypeNotSupportedException()
               }

           } else {
               throw new FileNotFoundException()
           }

       } catch (e) {
           Log.error("An exception occurred when trying to execute " + scriptName, e)
           Log.error("STACK:", e)
       }

    }

    private executeShellScript() {

        def cmd
        if (this.os_name ==~ /Window.*/ ) {
            cmd = ["cmd","/u/c", scriptName + " " + scriptArgs]
        } else if (this.os_name == 'Linux' ) {
            cmd = ["sh","-c", scriptName + " " + scriptArgs]
        } else if (this.os_name ==~ /SunO.*/ ) {
            cmd = ["sh","-c", scriptName + " " + scriptArgs]
        } else {
            Log.error("No settings available for OS: ${this.os_name}")
            throw Exception()
        }

        def g = new OSGatherer()

        def returnData = g.runExternalCmd(cmd)

        Log.debug(returnData)
        return returnData
    }

    /**
     * This method will be called for scripts which simply return one value, or a string
     *
    */
    public EXT_OUTPUT_VALUE(variables) {

    // Configure standard metric variables
    def identifier = variables.identifier
    def datestamp = MetricsDB.getNewDateTime()

    //
    this.scriptName = variables.scriptName
    this.scriptType = variables.scriptType
    this.returnType = variables.returnType
    this.scriptArgs = variables.scriptArgs
    this.dataType = variables.dataType
    this.metricName = variables.metricName
    this.instanceName = variables.instanceName
    this.saveMetrics = variables.saveMetrics

    // Create a MetricModel object and set the metric properties
    MetricModel mod = new MetricModel()
    mod.setMetricName(metricName)
    mod.setMetricType("EXTERNAL")
    mod.setMetricDataType(returnType)
    mod.setIdentifier(identifier)
    mod.setInstance(instanceName)
    mod.setHostName(this.host)
    mod.setInitiatorID(this.initiatorID)

    assert variables.scriptName != null, "Supplied scriptname must not be null!"
    assert variables.scriptType != null, "Supplied script type must not be null!"
    assert variables.returnType != null, "Supplied return type must not be null!"
    assert variables.metricName != null, "Supplied metric name must not be null!"
    assert variables.identifier != null, "Supplied identifier must not be null!"

    def returnValue = executeScript()[1]
    
    // Define type of returnValue depending on specified returnType
    switch ( returnType ) {
            case "Boolean": returnValue = (boolean)
                            break
            case "Float":   returnValue = (float)
                            break
            case "String":  returnValue = (String)
                            break
            default:        returnValue = (String)
                            break
    }


    if ( saveMetrics == "true" ) {
        persistMetric(mod, returnValue, datestamp)
    }

        return returnValue
    }

    /**
     * Simple method to persist metrics which are gathered from Nagios
     * compatible scripts
    */
    private parseNagiosScriptOutput(output) {

    def perf = [:]
    if ( this.saveMetrics == "true" ) {
        
        //try {
        // This is used to group together related metrics
        def groupID  = UUID.randomUUID();
        // Get performance figures if they exist
        if ( output =~ /\|/ ) {
            output.tokenize("|")[1..-1].each { s2 ->

                Log.debug("Found nagios performance figures: $s2")
                s2.tokenize(" ").each {

                    // Get the instance name from the bit before the equals sign
                    def anInstance = it.split("=")[0]
                    perf[anInstance] = it.split("=")[1]

                        // The bit after the equals sign contains the metrics
                        def i = 0
                        it.split("=")[1].tokenize(";").each {
                         i++

                         // Make up an indentifier using instance name and a number
                         def aKey = (anInstance + "_" + i)
                         def datestamp = MetricsDB.getNewDateTime()

                         // Create a MetricModel for the nagios figure
                         MetricModel mod = new MetricModel()
                         mod.setMetricName(aKey)
                         mod.setMetricType("NAGIOS")
                         mod.setMetricDataType("String")
                         mod.setIdentifier(this.scriptName)
                         mod.setHostName(this.host)
                         mod.setInitiatorID(this.initiatorID)
                         mod.setGroupID(groupID)

                         persistMetric(mod, it, datestamp)
                        }
                }
            }

        } else {
            Log.info("No performance figures found in nagios check.")
        }
        //} catch (e) {
        //        Log.error("An exception occurred when logging nagios metrics: ", e)
        //}
        }
        return perf
    }
    

    /**
     * This method should be called when specifying a script which produces nagios
     * compatible output
    */
    public NAGIOS_SCRIPT_VALUES(variables) {

    this.scriptName = variables.scriptName
    this.scriptType = variables.scriptType
    this.scriptArgs = variables.scriptArgs
    this.saveMetrics = variables.saveMetrics

    assert variables.scriptName != null, "Supplied scriptname must not be null!"
    assert variables.scriptType != null, "Supplied script type must not be null!"

    def returnValues = executeScript()
    def nagiosOutput = returnValues[1]

    // Get any performance stats and save in metrics database
    def perf = parseNagiosScriptOutput(nagiosOutput)

    return [returnValues[0], returnValues[1], perf]
    }


}