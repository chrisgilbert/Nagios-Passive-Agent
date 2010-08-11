package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.gatherers.OracleGatherer
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.NPA

/**
 * This class tests the OracleGatherer
 * @author Chris Gilbert
 */
class OracleGathererTest extends NPATest {

    
    public void testInstatiate() {
        def gatherer = new OracleGatherer("oracle10g.corelogic.local", "1521", "ora10test", "fw34x", "fw34x", "TEST")
    }

    public void testMetrics() {
        def variables = [:]
        variables["tablespace_name"] = "USERS"
        def g = new OracleGatherer("oracle10g.corelogic.local", "1521", "ora10test", "fw34x", "fw34x", "TEST")
        assert g.sample("ORA_TBS_MB_FREE", variables) > 0
        assert g.sample("ORA_TBS_MB_USED", variables) > 0
        assert g.sample("ORA_TBS_MB_TOTAL", variables) > 0
        assert g.sample("ORA_STATS_BUFFER_CACHE_HIT_RATIO", [:]) > 0
        g.disconnect();
    }

    //public void testDisconnect() {
    //    def variables = [:]
    //    variables["tablespace_name"] = "SYSTEM"
    ///
    //    def g = new OracleGatherer("oracle10g.corelogic.local", "1521", "ora10test", "fw34x", "fw34x", "TEST")
    //    assert g.sample("ORA_TBS_MB_FREE", variables) > 0
    //    assert g.sample("ORA_TBS_MB_USED", variables) > 0
    //    assert g.sample("ORA_TBS_MB_TOTAL", variables) > 0
    //    assert g.sample("ORA_STATS_BUFFER_CACHE_HIT_RATIO", [:]) > 0
    //    g.disconnect();
    //}


}

