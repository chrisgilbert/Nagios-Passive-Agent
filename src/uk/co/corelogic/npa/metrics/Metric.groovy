package uk.co.corelogic.npa.metrics
import groovy.sql.GroovyRowResult
import uk.co.corelogic.npa.common.*

public class Metric {

def initiatorID
def groupID
def hostName
def instanceName
def metricName
def metricType
def metricDataType
def value
def identifier
def datestamp
def ID
def description


    public Metric(initiatorID, groupID, hostName, instanceName, metricName, metricType, metricDataType, value, identifier, datestamp) {
        // Check for null values
        assert initiatorID != null, 'InitiatorID cannot be null!'
        assert hostName != null, 'Host name cannot be null!'
        assert metricName != null, 'Metric name cannot be null!'
        assert value != null, 'Metric value cannot be null!'
        assert datestamp != null, 'Datestamp cannot be null!'

        this.initiatorID = initiatorID
        this.groupID = groupID
        this.hostName = hostName
        this.instanceName = instanceName
        this.metricName = metricName
        this.metricType = metricType
        this.metricDataType = metricDataType
        this.value = value
        this.identifier = identifier
        this.datestamp = datestamp
        Log.debug("Creating a metric with value ${this.value}")
        // Persist the metric, so we can get an ID
        MetricsDB.persistMetric(this)
    }

    /**
     * Constructor for using MetricModels
    */
    public Metric(MetricModel mod, value, datestamp) {
        // Check for null values
        assert mod.initiatorID != null, 'InitiatorID cannot be null!'
        assert mod.hostName != null, 'Host name cannot be null!'
        assert mod.metricName != null, 'Metric name cannot be null!'
        assert value != null, 'Metric value cannot be null!'
        assert datestamp != null, 'Datestamp cannot be null!'

        this.initiatorID = mod.initiatorID
        this.groupID = mod.groupID
        this.hostName = mod.hostName
        this.instanceName = mod.instanceName
        this.metricName = mod.metricName
        this.metricType = mod.metricType
        this.metricDataType = mod.metricDataType
        this.value = value
        this.identifier = mod.identifier
        this.description = mod.description
        this.datestamp = datestamp
        Log.debug("Creating a metric with value ${this.value}")
        // Persist the metric, so we can get an ID
        this.ID = MetricsDB.persistMetric(this)
    }

    public Metric(GroovyRowResult row) {
        this.initiatorID = row.initiatorID
        this.groupID = row.groupID
        this.hostName = row.hostName
        this.instanceName = row.instanceName
        this.metricName = row.metricName
        this.metricType = row.metricType
        this.metricDataType = row.metricDataType
        this.value = row.value
        this.identifier = row.identifier
        this.description = row.description
        this.datestamp = row.datestamp
    }

}