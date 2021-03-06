package uk.co.corelogic.npa.common
import java.io.InputStream
import uk.co.corelogic.npa.NPA
import java.util.timer.*
import uk.co.corelogic.npa.metrics.MetricsDB
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import uk.co.corelogic.npa.metrics.*
import java.util.concurrent.*


public class StartChecks {


    private static def checkList = []
    private static config

    //Force singleton
    private StartChecks() { }

    synchronized private static parseConfig() {

        /*def factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        def schema = factory.newSchema(new StreamSource(new StringReader(xsd)))
        def validator = schema.newValidator()
        validator.validate(new StreamSource(new StringReader(xml)))
        */

        config = NPA.getConfigObject()

        def npa_version = MaintenanceUtil.getNPAVersion()

        println "Nagios Passive Agent - version $npa_version started."
        Log.debug("Nagios Passive Agent - version $npa_version started.")
        def configFile = new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/" + config.npa.configfile
        def npachecks = new XmlSlurper().parse(new File(configFile))
        def allGroups = npachecks.'check-group'

        // Register internal plugins
        PluginsRegister.registerInternalPlugins()
        Log.info("Registerd checkList:" + CheckRegister.getCheckNames())
        Log.info("Registerd classNames:" + CheckRegister.getClassRegister())
        Log.info("Registerd metricList:" + MetricRegister.getMetricNames())
        Log.info("Registerd metric classNames:" + MetricRegister.getClassRegister())

        allGroups.each {
            Log.debug("Parsing group " + it.@name + " of type " + it.@type)
            def g = it
            def checks 
            if (it.@type == "nagios" ) {
                checks = it.check
            } else {
                Log.debug("Group type ${it.@type} not supported - ignoring.")

            }
                checks.each {
                    Log.info("Adding check ${it.@name} to list.")

                Log.debug("Parsing arguments.")
                def argsmap = [:]
                def args = it.children()
                Log.debug(it.text())
                args.each {
                    def m = [{it.name()}:{it.text()}]
                    Log.debug("Found arguments")
                    Log.debug(m)
                    argsmap["${it.name()}"] =it.text()
                }
                    def c
                    // If it's a nagios check, we don't need to worry about thresholds seperately, just parse the other arguments
                    if (it.@name.toString().trim() == "chk_nagios" ) {
                        c = CheckFactory.getCheck(it.@name.toString())
                        c.chk_name = it.@name.toString()
                        c.variables = argsmap
                        c.argsAsXML = it
                    } else {
                        c = CheckFactory.getCheck(it.@name.toString())
                        c.chk_name = it.@name.toString()
                        c.chk_th_warn = it.@warn.toDouble()
                        c.chk_th_crit = it.@crit.toDouble()
                        c.chk_th_type = it.@type.toString()
                        //c.chk_args = argsmap
                        c.variables = argsmap
                        c.argsAsXML = it
                    }
                    c.chk_interval = g.@interval.toString().toInteger();
                    checkList.add(c);
                    Log.debug(checkList)
                }
        }
    }

    static start() {
        try {
            parseConfig()
        } catch(e) {
            Log.fatal("******* Failed to parse npa.xml config file! **********")
            println("******* Failed to parse npa.xml config file! **********")
            System.exit(1)
        }


        try {
            // Inititialise MetricDB
            MetricsDB.connect()
            MetricsDB.purgeMetrics()
        } catch(e) {
            Log.fatal("Failed to init MetricsDB!")
            throw new NPAException("Failed to init MetricsDB!!!", e)
        }

        
        checkList.each {
            CheckScheduler.schedule(it);
        }

        // Start a timer to flush the queue at the scheduled interval
        long delay = 0   // delay for 0 sec.
        //def random = new Random()

        ScheduledExecutorService timer1 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService timer2 = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService timer3 = Executors.newSingleThreadScheduledExecutor();

        def interval = config.npa.flush_queue_ms
        if ( interval == [:] ) { interval = "30000" }
        
        // Check for a variable setting the period to report back host OK status - default to 60 seconds
        def hostInt = config.npa.submit_host_ok_ms
        if ( hostInt == [:] ) { hostInt = "60000" }


        Log.info("Scheduling results queue to be flushed every $interval")
        CheckScheduler.schedule(new FlushQueue(interval))
        Log.info("Scheduling host OK check to run every $hostInt ms")
        CheckScheduler.schedule(new SubmitHostOK(hostInt))



        // This is a shutdown hook to automatically flush the queue on a JVM shutdown
        def shutdownClosureMap = [run: {
            MaintenanceUtil.sendShutdownHost()
            MaintenanceUtil.stopAllTimers()
            CheckResultsQueue.flush()
            Log.warn "Shutting down...";
            println "Shutting down...";
        }
        ]
        def interfaces = [Runnable]
        def shutdownListener = ProxyGenerator.instantiateAggregate(shutdownClosureMap, interfaces, Thread.class)

        Runtime.getRuntime().addShutdownHook((Thread)shutdownListener);
        
    }

    public static startMaintenance(){
        // Check for a variable setting the period to run maintenance jobs - default to 300 seconds
        def maintInt = config.npa.submit_host_ok_ms
        if ( maintInt == [:] ) { maintInt = 300000 }
        Log.info("Scheduling maintenance to run every $maintInt ms")
        RunMaintenance t = new RunMaintenance(maintInt)
        t.start()
    }


}
