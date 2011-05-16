package uk.co.corelogic.npa.common
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.gatherers.*


/**
 * This class provides methods for registering plugins associated with the
 * NPA.  It has not yet implemented a method for adding external plugins
 *
 * @author Chris Gilbert
 */
class PluginsRegister {
	
    // Make a singleton
    private PluginsRegister() {

    }

    /**
     * A simple method to add all the included plugins to the register.
    */
    synchronized public static registerInternalPlugins() {
        registerPlugin(new ExternalGatherer(), new NagiosCheck())
        registerPlugin(new HTTPGatherer(), new HTTPCheck())
        registerPlugin(new OSGatherer(), new OSCheck())
        registerPlugin(new OracleGatherer(), new OracleCheck())
        registerPlugin(new SSGatherer(), new SSCheck())
        registerPlugin(new DBGatherer(), new DBCheck())
    }

    /**
     * Method to register a plugin.  Requires two objects, of type Gatherer and Check
    */
    synchronized public static registerPlugin(g, c) {
        Log.debug("Registering plugin with following classes: ${g.getClass().getName()} ${c.getClass().getName()}")
        g.registerMetrics()
        c.registerChecks()
        g = null
        c = null
    }
}

