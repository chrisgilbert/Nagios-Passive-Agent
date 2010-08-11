package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.metrics.RawMetric


/**
 * This class handles parsing of the various configuration files
 * and validation of the npa.xml
 *
 * @author Chris Gilbert
 */
class ConfigParser {

    static def checkList = []
    static config

    /**
     * Register the internal plugins which are currently available
    */
    public registerPlugins() {
        // Register internal plugins
        PluginsRegister.registerInternalPlugins()
        Log.info("Registered checkList:" + CheckRegister.getCheckNames())
        Log.info("Registered classNames:" + CheckRegister.getClassRegister())
        Log.info("Registered metricList:" + MetricRegister.getMetricNames())
        Log.info("Registered metric classNames:" + MetricRegister.getClassRegister())
    }

    /**
     * A complicated XML file parser.
     * This will parse the npa.xml file for known group types and then
     * add either checks or RawMetric objects to the CheckList.
     *
    */
    public parseXMLConfig() {

        def configFile = new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/" + NPA.config.npa.configfile
        def npachecks = new XmlSlurper().parse(new File(configFile))
        def allGroups = npachecks.'check-group'

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
                Log.warn("Group type ${it.@type} not supported - ignoring.")

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

                    checkList.add([check:c, group:g.@name, interval:g.@interval])
                    Log.fine(checkList)
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
                    checkList.add([check:c, group:g.@name, interval:g.@interval.toString().toFloat()])
                    Log.fine(checkList)
                }



        }
    }

    /**
     * Schedule all checks, and setup a queue flushing mechanism.
     * Queue will be flushed acording to the npa.flush_queue_ms setting
     * in the defaults file.  It will also be flushed when a JVM shutdown
     * signal is received.
    */
    public void scheduleChecks() {

        checkList.each {
            it.check.schedule(it.interval)
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

