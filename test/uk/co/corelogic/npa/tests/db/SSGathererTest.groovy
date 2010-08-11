package uk.co.corelogic.npa.tests.db
import uk.co.corelogic.npa.tests.NPATest
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.gatherers.*



/**
 *
 * @author chris
 */
class SSGathererTest extends NPATest {

    public getVars(){
       def variables = [:]
       variables.host = "ss2005-test1.corelogic.local"
       variables.port = "1433"
       variables.database = "fw35x"
       variables.user = "fw"
       variables.password = "fw"
       variables.initiatorID = "TEST"
       return variables
    }

    public void testInstantiate(){

        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())

    }

    public void testSS_NUM_BLOCKING_PROCS() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_NUM_BLOCKING_PROCS(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_STATS_BC_PAGE_LIFE() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_STATS_BC_PAGE_LIFE(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_DATA_FILES_TOTAL() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_DATA_FILES_TOTAL(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_DATA_FILES_USED() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_DATA_FILES_USED(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_DATA_FILES_FREE() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_DATA_FILES_FREE(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }
    public void testSS_LOG_FILES_TOTAL() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_LOG_FILES_TOTAL(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_LOG_FILES_USED() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_LOG_FILES_USED(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_LOG_FILES_FREE() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_LOG_FILES_FREE(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }
    
    public void testSS_LOG_FILES_PCT_USED() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_LOG_FILES_PCT_USED(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_DATA_FILES_PCT_USED() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_DATA_FILES_PCT_USED(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    public void testSS_STATS_BC_HIT_RATIO() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_STATS_BC_HIT_RATIO(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }
    
    public void testSS_STATS_NUM_ACT_TRANS() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_STATS_NUM_ACT_TRANS(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }
    
    public void tesSS_STATS_TRANS_PER_SEC() {
        def gatherer = new SSGatherer(getVars().host, getVars().port, getVars().database, getVars().user, getVars().password, getVars().initiatorID)
        gatherer = new SSGatherer(getVars())
        def procs = gatherer.SS_STATS_TRANS_PER_SEC(getVars())
        assert procs >= 0, "Did not return a valid value (>= 0)"
    }

    
}

