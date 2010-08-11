package uk.co.corelogic.npa.metrics;

/**
 * This class is for creating new MetricModels as part of a Gatherer.  It provides some basic
 * functionality to assist in gathering operations, to save having to write tedious Metric constructors
*/
class MetricModel implements Cloneable {

    def metricName;  // The registered name of the metric
    def metricType;  // The overall metric type, for example OS, ORACLE, SS
    def metricDataType; // The datatype stored in the database.  This is a string description
    def identifier;  // The unique indentifier in the database
    def instanceName;    // The instance of the data value being persisted (for example database name, filesystem identifier)

    def initiatorID;  // The initiator ID for the Gatherer
    def groupID;      // The group ID for the metric (if required)
    def hostName;     // Host name which the metric was gathered from

    public setInstance(instance) {
        this.instanceName = instance
    }

}