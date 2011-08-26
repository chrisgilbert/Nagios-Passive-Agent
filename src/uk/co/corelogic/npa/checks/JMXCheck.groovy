/*
 * A generic JMX Check class.  Other classes subclass this class to add specific application server functionality.
 */
package uk.co.corelogic.npa.checks

import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.database.*
import uk.co.corelogic.npa.common.Log
import uk.co.corelogic.npa.gatherers.*

/**
 *
 * @author Chris Gilbert
 */
class JMXCheck extends Check implements CheckInterface {

    /**
    * Register all the checks which this class implements
    */
    public registerChecks() {
        CheckRegister.add("chk_jmx_attr", "JMX", this.getClass().getName())
        CheckRegister.add("chk_jmx_eval", "JMX", this.getClass().getName())
    }

    public init(variables) {

    }

    JMXCheck(args) {
        init(args)
    }
    JMXCheck() {
        super()
    }

    JMXCheck(chk_name, th_warn, th_crit, th_type, variables) {
        super(chk_name, th_warn, th_crit, th_type, variables)
        init(variables)
    }


  }