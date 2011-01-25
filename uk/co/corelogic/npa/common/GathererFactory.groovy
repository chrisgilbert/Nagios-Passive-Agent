package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.metrics.MetricRegister

class GathererFactory {

    private GathererFactory() {

    }

    /**
     * Method to instantiate Gatherer objects based on the supplied metric name
     *
     * @throws ClassNotFoundException
    */
    public static Gatherer getGatherer(met_name, id) throws ClassNotFoundException {
        Log.debug("Instantiating new $met_name gatherer with ID $id")

        // Use reflection to create a class using a string of it's name
        Class aGatherer = Class.forName(MetricRegister.getClassName(met_name))
        return aGatherer.newInstance(id)
    }

}