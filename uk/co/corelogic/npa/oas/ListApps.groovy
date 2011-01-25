package uk.co.corelogic.npa.oas
import javax.management.remote.*
import oracle.oc4j.admin.jmx.remote.api.JMXConnectorConstant
import javax.management.remote.JMXConnectorFactory as JmxFactory
import javax.management.MBeanServerConnection;

class listApps {

static void main(String[] args) {

// Get required credentials and information
def host = args[0]
def port = args[1]
def user = args[2]
def password = args[3]

def serverUrl = new JMXServiceURL('service:jmx:rmi:///opmn://'+ host +':' + port + '/cluster:' + port)
def serverPath = 'oc4j:j2eeType=J2EEServer,name=standalone'
def jvmPath = 'oc4j:j2eeType=JVM,name=single,J2EEServer=standalone'
def provider = 'oracle.oc4j.admin.jmx.remote'
def credentials = [ (JMXConnectorConstant.CREDENTIALS_LOGIN_KEY): user
, (JMXConnectorConstant.CREDENTIALS_PASSWORD_KEY): password]
def env = [ (JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES): provider
, (JMXConnector.CREDENTIALS): credentials ]
def con = JmxFactory.newJMXConnector(serverUrl, env)

println ("Attempting to connect to OAS cluster..")
try {
	con.connect()
} catch(e) {
	println "Connection failed!"
	System.exit(1)
}

// Here we get the mbean server...
def MBeanServerConnection appserver = con.getMBeanServerConnection()


// We need to connect to the individual OC4J instances
def JVMs = getJVMServers(appserver, env)

// Get a set of MbeanServerConnection objects for the JVMs
def jvmServers = []
JVMs.collect { jvmServers.add(it.getMBeanServerConnection()) }

//
//avail.each { println it.objectName }

// Get a list of application beans and OC4J beans for each JVM
def AllBeans = [:]
def OC4JBeans = []
def appBeans = []
jvmServers.collect {
//        println it.getAttribute( new ObjectName('java.lang:type=Runtime'), 'Uptime' )
        appBeans.add(getApplicationBeans(it))
        OC4JBeans.add(getOC4JBeans(it))
}


// Search for an appropriate webmodule to identify the applications
def apps = [:]
for(applist in appBeans) {
    applist.collect {
        println it.ObjectName
        def appinfo = [:]
        appinfo[getAppType(it)] = it
        apps.put(it.ObjectName, appinfo)
    }
}

// Get attributes required

def details = [:]
appBeans.each {
//    it.getAttribute( new ObjectName(it.ObjectName), 'framework.version' )
    //it.getAttribute( new ObjectName(it.ObjectName), 'framework.database' )
}



// Use the objectName to match the appbeans into a Map of instance names and Mbeans for each app
OC4JBeans.each() {
            println it
            if(it.value.instanceName) { println it.value.instanceName } else { println "null"  }
            if(it.value.instanceName) { println it.value.instanceName } else { println "null"  }
            AllBeans.put(it.value.instanceName, apps[it.value.objectName])
}


// Print all applications
AllBeans.each {
        def appinfo = it.value

        println it.key
        appinfo.each { -> app
            app.each {
                if (it.key == "framework") {
                          println "Framework version: " + it.getProperty("version")

                    }
                //println "FIS Version: ${it[fis]}"
                //println "Quartz: ${it[quartz]}"
            }
        }

}


//getMBeansFromSearch(appserver, "j2eeType=").each { println it.objectName }

def appVersions = [:]

// Cleanup
JVMs.each {
            if ( it!=null ) { it.close() }
}
if(con!=null) {
	con.close();
}

}


static void printApps(AllBeans) {
AllBeans.each {
        def appinfo = it.value

        println it.key
        appinfo.each { -> app
            app.each {
                if (it.key == "framework") {
                        //  println "Framework version: " + server.getAttribute( new ObjectName('java.lang:type=Runtime'), 'Uptime' )/1000/60/60 + " hours"

                    }
                //println "FIS Version: ${it[fis]}"
                //println "Quartz: ${it[quartz]}"
            }
        }
}
}

static void printAppsWiki(AllBeans) {
AllBeans.each {
        def appinfo = it.value

        println it.key
        appinfo.each { -> app
            app.each {
                if (it.key == "framework") {
                          println "Framework version: " + it.getProperty("version")

                    }
                //println "FIS Version: ${it[fis]}"
                //println "Quartz: ${it[quartz]}"
            }
        }
}
}

static List getMBeansFromSearch(server, search) {
	def query = new javax.management.ObjectName('ias:*')
	def String[] allNames = server.queryNames(null, null)

	def mbeans = allNames.findAll{ name ->
	name.contains(search)
	}.collect{ new GroovyMBean(server, it) }
	return mbeans
}

static List getOC4JMBeansFromSearch(server, search) {
	def query = new javax.management.ObjectName('oc4j:*')
	def String[] allNames = server.queryNames(null, null)

	def mbeans = allNames.findAll{ name ->
	name.contains(search)
	}.collect{ new GroovyMBean(server, it) }
	return mbeans
}

static List getJVMServers(server, env) {
	def mbeanservers = []
    print "Attempting to connect to JVMs."
	for(jvmProxy in getMBeansFromSearch(server, "j2eeType=JVMProxy")) {

		def serviceurl = new JMXServiceURL("service:jmx:rmi://$jvmProxy.node:$jvmProxy.rmiPort")

		def jvm = JmxFactory.newJMXConnector(serviceurl, env)

		try {
			jvm.connect()
			print "."
			mbeanservers.add(jvm)

		} catch(e) {
			println " Could not connect :("
		}
	}
	return mbeanservers
}

static List getJVMProxies(server) {
	def proxies = []
	for(jvmProxy in getMBeansFromSearch(server, "j2eeType=JVMProxy")) {
		proxies.add(jvmProxy)
	}
	return proxies
}


static void print_fwi_version(appMBean) {
	println "Trying to get frameworki version.."
	try {
		println appMBean.getProperty("framework.version")
	} catch(e) {
	}
}

static List getApplicationBeans(jvm) {
	def appnames = []

	for(appProxy in getOC4JMBeansFromSearch(jvm, "j2eeType=J2EEApplication")) {
		appnames.put(appProxy)
	}
	return appnames
}

static List getOC4JBeans(jvm) {
	def oc4jnames = []

	for(appProxy in getOC4JMBeansFromSearch(jvm, "j2eeType=J2EEServer")) {
		oc4jnames.put(appProxy)
	}
	return oc4jnames
}

static List getApplicationList(jvm) {
	def apps = []
    def query = new javax.management.ObjectName('oc4j:*')
	def String[] allNames = jvm.queryNames(null, null)

	apps = allNames.findAll{ name ->
        name.contains("j2eeType=J2EEApplication")
	}
	return apps
}


static String getAppType(app) {
	def apptype = "other"
    def FWIDENTIFIER="name=framework"
    def FISIDENTIFIER="name=fis"
    def QZIDENTIFIER="name=quartz"

	for(appProxy in getOC4JMBeansFromSearch(app.server(), FWIDENTIFIER)) {
        apptype="fw"
	}
	for(appProxy in getOC4JMBeansFromSearch(app.server(), QZIDENTIFIER)) {
        apptype="quartz"
	}
    for(appProxy in getOC4JMBeansFromSearch(app.server(), FISIDENTIFIER)) {
        apptype="fis"
	}
	return apptype
}



}