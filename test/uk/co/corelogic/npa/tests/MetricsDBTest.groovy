package uk.co.corelogic.npa.tests

import uk.co.corelogic.npa.*
import uk.co.corelogic.npa.common.*
import uk.co.corelogic.npa.metrics.*
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.gatherers.*
import uk.co.corelogic.npa.NPA

import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters


class MetricsDBTest extends NPATest {


  @Before void setUp() {
     // Delete test data
     MetricsDB.connect()
     MetricsDB.deleteTestData()

  }

  @Test void testConnect() {
      MetricsDB.connect()
  }

  @Test void testSaveLogPosition() {
      def variables = [:]
      def pos = 5000
      variables.filename = "/tmp/test";
      MetricsDB.connect()
      MetricsDB.saveLogPosition(variables, pos)
      assertEquals(5000, MetricsDB.retrieveLogPosition(variables))

      pos = 10000
      MetricsDB.saveLogPosition(variables, pos)
      assertEquals(10000, MetricsDB.retrieveLogPosition(variables))

  }

  @Test void testPurgeMetrics() {
    //  MetricsDB.connect()
    //  MetricsDB.purgeMetrics()
  }

  @Test void testSaveMetricAndRetrieveByID() {

     // Create a MetricModel object and set the metric properties
    MetricModel mod = new MetricModel()
    mod.setMetricName("TEST_METRIC")
    mod.setMetricType("TEST")
    mod.setMetricDataType("Double")
    mod.setIdentifier("TOTAL")
    mod.setInstanceName("TESTINST");
    mod.setHostName("HOST")
    mod.setGroupID("TEST")
    mod.setInitiatorID("TEST")

    MetricsDB.connect()
    def datestamp = MetricsDB.getNewDateTime()
    //Creating a new metric should automatically persist it
    Metric m = new Metric(mod, 1.999999999D, datestamp)

    // Try to retrieve it from the database and check all the data is there
    def m2 = MetricsDB.retrieveWithID(m.ID)
    assertEquals("TEST_METRIC", m2.metricName)
    assertEquals("TEST", m2.metricType)
    assertEquals("Double", m2.metricDataType)
    assertEquals("TOTAL", m2.identifier)
    assertEquals("HOST", m2.hostName)
    assertEquals("TEST", m2.groupID)
    assertEquals("TEST", m2.initiatorID)
    assertEquals(1.999999999D, m2.value.toDouble())
    assertEquals(datestamp, m2.datestamp)
  }

    @Test void testFindGroupID() {
    // Create a MetricModel object and set the metric properties
    MetricModel mod = new MetricModel()
    mod.setMetricName("TEST_METRIC")
    mod.setMetricType("TEST")
    mod.setMetricDataType("Double")
    mod.setIdentifier("TOTAL")
    mod.setInstanceName("TESTINST");
    mod.setHostName("HOST")
    mod.setGroupID("TESTGROUP")
    mod.setInitiatorID("TEST")

    MetricsDB.connect()
    def datestamp = MetricsDB.getNewDateTime()
    //Creating a new metric should automatically persist it
    Metric m = new Metric(mod, 1, datestamp)
    // Try to retrieve it from the database and check all the data is there
    Metric m2 = MetricsDB.retrieveWithID(m.ID)
    
    assertEquals("TEST_METRIC", m2.metricName)
    assertEquals("TEST", m2.metricType)
    assertEquals("Double", m2.metricDataType)
    assertEquals("TOTAL", m2.identifier)
    assertEquals("HOST", m2.hostName)
    assertEquals("TESTGROUP", m2.groupID)
    assertEquals("TEST", m2.initiatorID)
    assertEquals(1, m2.value)
    assertEquals(datestamp, m2.datestamp)


   }

   @Test void testAddGroup() {
       MetricsDB.connect()
       MetricsDB.addGroup("TESTGROUP", "123")
       def id = MetricsDB.retrieveGroupID("TESTGROUP", "123")
   }



   @Test void testGetGroupMetric() {
       def initiatorID = "TEST"
       def identifier = "TEST1"
       def instance = "TESTINST"
       MetricsDB.connect()

       // Create a MetricModel object and set the metric properties
       MetricModel mod = new MetricModel()
       mod.setMetricName("TEST_METRIC")
       mod.setMetricType("TEST")
       mod.setMetricDataType("Double")
       mod.setIdentifier(identifier)
       mod.setInstanceName(instance);
       mod.setHostName(identifier)
       mod.setInitiatorID(initiatorID)

       // First add a group and metric
       MetricGroup grp = new MetricGroup("TESTGROUP2", initiatorID, ["MYTESTMET"])
       mod.setGroupID(grp.groupID)

       def value = 12345.01010101D
       def g = new Gatherer(initiatorID)
       def met3 = g.persistMetric(mod, value, MetricsDB.getNewDateTime())

       assert MetricsDB.getGroupMetric(grp.groupID, "TEST_METRIC").toDouble() == value, "Retrieved metric does not match original value!"
    }

    @Test void testMetricModelSave() {
         // Configure standard metric variables
        def identifier = "TEST1"

        // Create a MetricModel object and set the metric properties
        MetricModel mod = new MetricModel()
        mod.setMetricName("TEST_METRIC")
        mod.setMetricType("TEST")
        mod.setMetricDataType("Double")
        mod.setIdentifier(identifier)
        mod.setInstanceName(identifier);
        mod.setHostName(identifier)
        mod.setInitiatorID("12345")

        def value = 12345.01010101D
        def g = new Gatherer("12345")
        def met3 = g.persistMetric(mod, value, MetricsDB.getNewDateTime())

    }

}