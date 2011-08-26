package uk.co.corelogic.npa.checks

/**
 * Check interface to ensure checks correctly implement certain methods
 *
 * @author Chris Gilbert
 */
interface CheckInterface {
	def init(something)
        def clone()
        def registerChecks()
        
}

