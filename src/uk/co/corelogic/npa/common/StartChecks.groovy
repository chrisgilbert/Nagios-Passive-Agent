package uk.co.corelogic.npa.common
import java.io.InputStream
import uk.co.corelogic.npa.NPA
import java.util.timer.*
import uk.co.corelogic.npa.metrics.MetricsDB
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import uk.co.corelogic.npa.metrics.*


public class StartChecks {


static def checkList = []
static config

    private static parseConfig() {

        /*def factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        def schema = factory.newSchema(new StreamSource(new StringReader(xsd)))
        def validator = schema.newValidator()
        validator.validate(new StreamSource(new StringReader(xml)))
        */

        config = NPA.getConfigObject()

        def npa_version = "1.0.7-build2"


        println "Nagios Passive Agent - version $npa_version started."
        Log.debug("Nagios Passive Agent - version $npa_version started.")
        def configFile = new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/" + NPA.config.npa.configfile
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
            def metrics
            if (it.@type == "nagios" ) {
                checks = it.check
            } else if (it.@type == "metrics") {
                metrics = it.metric
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
                        c.chk_args = argsmap
                    } else {
                        c = CheckFactory.getCheck(it.@name.toString())
                        c.chk_name = it.@name.toString()
                        c.chk_th_warn = it.@warn.toDouble()
                        c.chk_th_crit = it.@crit.toDouble()
                        c.chk_th_type = it.@type.toString()
                        c.chk_args = argsmap
                    }
                    c.chk_interval = g.@interval.toString().toInteger();
                    checkList.add(c);
                    Log.debug(checkList)
                }

                metrics.each {
                    Log.info("Adding metric ${it.@name} to list.")

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

                    def c = new RawMetric(it.@name.toString(), argsmap)
                    c.chk_interval = g.@interval.toString().toInteger();
                    checkList.add(c);
                    Log.debug(checkList)
                }



        }
    }

    static start() {

        parseConfig()

        // Inititialise MetricDB
        MetricsDB.connect()
        MetricsDB.purgeMetrics()

        checkList.each {
            CheckScheduler.schedule(it);
        }

        // Start a timer to flush the queue at the scheduled interval
        long delay = 0   // delay for 0 sec.
        //def random = new Random()
        Timer timer = new Timer("ResultsQueue")
        def interval = NPA.config.npa.flush_queue_ms
        Log.info("Scheduling results queue to be flushed every $interval")
        timer.scheduleAtFixedRate(new FlushQueue(), delay, interval.toLong())

        // This is a shutdown hook to automatically flush the queue on a JVM shutdown
        def shutdownClosureMap = [run: {
            CheckResultsQueue.flush()
            println "Shutting down";
        }
        ]
        def interfaces = [Runnable]
        def shutdownListener = ProxyGenerator.instantiateAggregate(shutdownClosureMap, interfaces, Thread.class)

        Runtime.getRuntime().addShutdownHook((Thread)shutdownListener);

    }


}