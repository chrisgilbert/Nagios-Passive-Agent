package uk.co.corelogic.npa.gatherers
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.common.*


class LogGatherer extends Gatherer {//implements Gatherer {


    public readLog(variables, fromLine) {

        def processLine = []
        def pos

        lnr= new LineNumberReader(new FileReader(variables.filename), variables.buffer)
        if ( lnr.exists() ) {
            Log.debug("Commence processing lines from $fromLine onward in $variables.filename")
            lnr.lineNumber = fromLine

            while (ln = lnr.readLine() ) {
               if  ( ln =~ variables.search ) {
                    processLine(variables, ln)
               }
               ln = null
               pos = lnr.lineNumber
            }
            if ( variables.savePosition == "true" ) {
                MetricsDB.saveLogPosition(variables, pos)
            }
            Log.debug("Finished processing lines from $fromLine onward in $variables.filename")

        } else {
            Log.error("File $variables.filename does not exist." )
        }
    }

    public processLine(variables, ln) {
        invokeMethod(variables.processMethod, [variables, ln])
        variables = null
        ln = null
    }

    public processFWiLine(l) {
        def variables = l[0]
        def line = l[1]
        l = null
        //
        //2008-05-07 13:41:16,690 [AJPRequestHandler-ApplicationServerThread-7] INFO  uk.co.corelogic.framework.common.log.PerformanceLogger - PERFORMANCE METRICS::REFERENCE::retrieveSystemProperties::3487ms
        def lineArr = ln.split()
        def time=lineArr[1]
        def perfStr = lineArr[7]
        lineArr = null

        //METRICS::REFERENCE::retrieveSystemProperties::3487ms
        def perfArr = perfStr.split('::')
        def value = perfArr[3]
        def type = perfArr[1]
        def meth = perfArr[2]

        // Populate hashmap with each value in sample set
        // We need a sampling method here...
        // Calculate percentage of sample frame?
        // e.g. 10% of all returned values
        // Do we sample all types, or just particular actions?
        this.samples[type][meth].add(value)

        Metric m1 = new Metric(this.initiatorID, groupID, this.host, null, "OS_DISK_MB_TOTAL", metricType, metricDataType, all[1].toFloat()/1024, identifier, datestamp)
        new Metric()

    }

    public addFWiMetrics() {

    }
}