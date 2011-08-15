package uk.co.corelogic.npa.common

/**
 *  Generic NPA Exception type
 * @author Chris gilbert
 */
class NPAException extends Exception {


    NPAException(s, e) {
        Log.error(s, e)
        System.out.println("ERROR: " + s)
        this.getStackTrace()
    }
    NPAException(s) {
        Log.error(s)
        System.out.println("ERROR: " + s)
        this.getStackTrace()
    }
    NPAException() {
        this.getStackTrace()
    }

}

