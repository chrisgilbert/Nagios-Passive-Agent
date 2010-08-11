package uk.co.corelogic.npa.common

/**
 * A register of all check names and types.  This is a syncronized list which is accessed from a static context
*/

class CheckRegister {

private static checkList = [:];
private static classRegister = [:]

    private CheckRegister() {

    }

    /**
     * Add a new metric to the public list
    */
    synchronized public static void add(chkName, type, className) {
        checkList[chkName] = type
        classRegister[type] = className
    }

    /**
     * Return a string representing the type of metric
    */
    synchronized public static getType(chkName) {
        return checkList[chkName]
    }

    /**
     * Return a class name of the appropriate type for a given metric
    */
    synchronized public static getClassName(chkName) {
        return classRegister[checkList[chkName]]
    }

    /**
    * Return the full registered metricList
    */
    synchronized public static getCheckNames() {
        return checkList;
    }

    /**
    * Return the full classRegister
    */
    synchronized public static getClassRegister() {
        return classRegister;
    }
}