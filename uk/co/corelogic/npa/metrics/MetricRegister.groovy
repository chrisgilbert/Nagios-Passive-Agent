package uk.co.corelogic.npa.metrics

/**
 * A register of all metric names and types.  This is a syncronized list which is accessed from a static context
*/

class MetricRegister {

private static metricList = [:];
private static classRegister = [:]

    private MetricRegister() {
        
    }

    /**
     * Add a new metric to the public list
    */
    synchronized public static void add(metricName, type, classObj) {
        metricList[metricName] = type
        classRegister[type] = classObj
    }

    /**
     * Return a string representing the type of metric
    */
    synchronized public static getType(metricName) {
        return metricList[metricName]
    }

    /**
    * Return a boolean indicating whether a metric has been registered or not
    */
    synchronized public static isRegistered(metricName) {
        if ( metricList[metricName] != null ) {
            return true
        } else {
            return false
        }
    }

    /**
     * Return a class name of the appropriate type for a given metric
    */
    synchronized public static getClassName(metricName) {
        return classRegister[metricList[metricName]]
    }

    /**
    * Return the full registered metricList
    */
    synchronized public static getMetricNames() {
        return metricList;
    }

    /**
    * Return the full classRegister
    */
    synchronized public static getClassRegister() {
        return classRegister;
    }
}