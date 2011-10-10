package uk.co.corelogic.npa.common

/**
 * Exception to be thrown when a check contains invalid arguments
 * @author chris
 */

class InvalidCheckArgumentsException extends NPAException {

    InvalidCheckArgumentsException(s) {
        Log.error("You specified incorrect arguments for the check somewhere.  Please check the configuration in npa.xml")
        Log.error(s)
        System.out.println("ERROR: " + s)
    }
}