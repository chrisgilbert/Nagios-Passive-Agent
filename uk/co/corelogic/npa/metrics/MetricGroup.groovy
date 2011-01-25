package uk.co.corelogic.npa.metrics
import uk.co.corelogic.npa.common.*

/**
 * This class is reponsible for providing methods to define and retrieve grouped metrics
*/
class MetricGroup {

    def instance
    def identifier
    def groupID
    def initiatorID
    def metricList

    public MetricGroup(groupName, initiatorID, metricList) {
        findGroup(groupName, initiatorID)
        this.metricList = metricList
    }


    /**
     * Return a groupID for a given name, or create a new one if required.
    */
    private findGroup(groupName, initiatorID) {
        if ( MetricsDB.retrieveGroupID(groupName, initiatorID) != null ) {
            this.groupID = MetricsDB.retrieveGroupID(groupName, initiatorID)
        } else {
            this.groupID = MetricsDB.addGroup(groupName, initiatorID)
        }
    }

    /**
     * Get value if present, or return null
    */
    private getValue(metricName) {
        def value = MetricsDB.getGroupMetric(this.groupID, metricName)
        Log.debug("Return value from database: $value")

        if (value != null) {
            return value
        } else {
            Log.debug("Value returned from database was null.")
            return null
        }
    }

    public getMetric(metricName) {
        if ( metricList.find { it == metricName } ) {
            getValue(metricName)

        } else {
            Log.error("Metric $metricName is not part of group $groupID")
            throw new Exception()
        }
    }

}