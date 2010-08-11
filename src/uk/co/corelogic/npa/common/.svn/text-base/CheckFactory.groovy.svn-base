package uk.co.corelogic.npa.common
import java.lang.ClassNotFoundException

class CheckFactory {

    private CheckFactory() {

    }

    /**
     * Method to instantiate Check objects based on the supplied check name
     *
     * @throws Exception
    */
    public static getCheck(chk_name) throws Exception {
        Log.debug("Factory producing $chk_name")

        def className = CheckRegister.getClassName(chk_name)
        if (className == null ) {
            Log.error("You are attempting to instantiate check $chk_name which has no registred class type.")
            throw new Exception()
        }

        // Use reflection to create a class using a string of it's name
        try {
            Class aCheck = Class.forName(className)
            return aCheck.newInstance()
        } catch(e) {
            Log.error("An exception occurred when building a new check.", e)
            Log.error("STACK:", e)
        }
    }

}