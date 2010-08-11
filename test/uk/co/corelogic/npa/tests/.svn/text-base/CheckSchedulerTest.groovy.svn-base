package uk.co.corelogic.npa.tests
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA


/**
 *
 * @author chris
 */
class CheckSchedulerTest extends NPATest {
	
    def chk_name = "chk_disk_free"
    int th_warn = 10
    int th_crit = 5
    def th_type = "LTE"
    def variables = [:]

    void testSchedule() {
        PluginsRegister.registerInternalPlugins()
        Log.info("Registerd checkList:" + CheckRegister.getCheckNames())
        Log.info("Registerd classNames:" + CheckRegister.getClassRegister())
        Log.info("Registerd metricList:" + MetricRegister.getMetricNames())
        Log.info("Registerd metric classNames:" + MetricRegister.getClassRegister())

        def variables = [:]
        variables = [nagiosServiceName:"TEST"]
        variables.volume="ALL"
        variables.unitType="percent"
        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.chk_name = "chk_disk_free"
        c1.chk_th_warn = 1
        c1.chk_th_crit = 2
        c1.chk_th_type = "GTE"
        c1.chk_interval = 10000
        c1.chk_args = variables

        CheckScheduler.schedule(c1);
    }

    void testPrintThreadState() {
        PluginsRegister.registerInternalPlugins()
        Log.info("Registerd checkList:" + CheckRegister.getCheckNames())
        Log.info("Registerd classNames:" + CheckRegister.getClassRegister())
        Log.info("Registerd metricList:" + MetricRegister.getMetricNames())
        Log.info("Registerd metric classNames:" + MetricRegister.getClassRegister())

        def variables = [:]
        variables = [nagiosServiceName:"TEST"]
        variables.volume="ALL"
        variables.unitType="percent"
        def c1 = CheckFactory.getCheck("chk_disk_free")
        c1.chk_name = "chk_disk_free"
        c1.chk_th_warn = 1
        c1.chk_th_crit = 2
        c1.chk_th_type = "GTE"
        c1.chk_interval = 10000
        c1.chk_args = variables
        
        CheckScheduler.schedule(c1);
        CheckScheduler.printThreadState()
    }

    void testRestartThread() {
        this.variables.volume="ALL"
        this.variables.nagiosServiceName="TEST"
        this.variables.unitType="percent"
        def check = new OSCheck(chk_name, th_warn, th_crit, th_type, variables)
        assert check != null
        def result = check.chk_disk_free()
        assert result != null
        println("Results is: " + result.status + " Message is: " + result.message)

        def c1 = CheckFactory.getCheck("chk_disk_free")
        check.schedule(10000)

        CheckScheduler.checkThreadMap.clone().each{
            def id = it.key
            CheckScheduler.restartThread(id)
        }

    }

}

