
//
// Simple update class, to update NPA to the most recent version.  This will update the npa.jar file and download any new libraries,
// but not make any changes to configuration
//
// Version 2.2 - 25/10/2011 Chris Gilbert
//

def config = new ConfigSlurper().parse(new File("../config/defaults.groovy").toURL())

def npaURL=config.npa.update_url
def newFile=config.npa.update_file
def libList=config.npa.libs_list
def suffix=new Date().format("yyyy-MM-dd-HH24-mm-ss")

// This grabs NPA settings for proxy and uses them if necessary
def proxyHost=config.npa.proxy_host
def proxyPort=config.npa.proxy_port
def proxyUserName=config.npa.proxy_username
def proxyPasswd=config.npa.proxy_password

if ( proxyHost != null ) {
        if (config.npa.proxy_enable == "true" ){
		println("Using proxy $proxyHost:$proxyPort")
            //println ( ["http.proxyHost":proxyHost, "http.proxyPort":proxyPort,"http.proxyUserName":proxyUserName.toString(), "http.proxyPassword":proxyPasswd.toString()] )
            System.properties.putAll( ["http.proxyHost":proxyHost.toString(), "http.proxyPort":proxyPort.toString(), "http.proxyUserName":proxyUserName.toString(), "http.proxyPassword":proxyPasswd.toString()] )
        }
}

println("This script will attempt to download and new version of NPA.  It is an experimental feature, so may have issues..")
println("-----------------------------------------------------------------------------------------------------------------")
println("Downloading release from $npaURL")
download(npaURL, '../')

def oldList = []
def dir = new File("../lib")
dir.eachFile{file->
  if(file.isFile()){
    //println ("Library ${file.getName()} found.")
    oldList.add(file.getName())
  }
}

println("System temp is at: " + System.getProperty('java.io.tmpdir'))
new File(download(libList, System.getProperty('java.io.tmpdir'))).eachLine { newLib->
        //println("Checking Library: " + newLib.tokenize("/")[-1])
	if ( ! oldList.contains(newLib.tokenize("/")[-1]) ) {
	  println ("Fetching library: $newLib")
    	  download(newLib, "../lib")
        }
}


stopNpa()
try {
	if ( ! (new File("../npa.jar").renameTo(new File("../npa.jar.old${suffix}"))) ) { throw new IOException() }
	if ( ! (new File("../" + newFile).renameTo(new File("../npa.jar"))) ) { throw new IOException() }
} catch(e) {
	println("Failed to rename file!" + e)
	throw e
}
startNpa()


def download(address, toDir)
{
    println("Downloading to: " + toDir + "/" + address.tokenize("/")[-1])
    def file = new FileOutputStream(toDir + "/" + address.tokenize("/")[-1])
    def out = new BufferedOutputStream(file)
    out << new URL(address).openStream()
    out.close()
    return (toDir + "/" + new URL(address).getFile())
}

def stopNpa() {
   println("Stopping NPA..")
   try {
	if (System.getProperty("os.name").startsWith("Windows")) {
		runCmd(["cmd", "/c", "net stop npa"])
	} else {
		runCmd(["sh", "-c", ".\npa stop"])
	}
   } catch(e) {
	println("Failed to stop NPA!" + e)
   }
}


def startNpa() {
   println("Starting NPA..")
   try {
	if (System.getProperty("os.name").startsWith("Windows")) {
		runCmd(["cmd", "/c", "net start npa"])
	} else {
		runCmd(["sh", "-c", ".\npa start"])
	}

	println("Update complete! Check for any errors above, and confirm NPA is still working correctly in the npa.log file.")
	System.exit(0)
   } catch(e) {
	println("Failed to start NPA!" + e)
   }
}


def runCmd(command) {
	def initialSize = 10000
	def outStream = new StringBuffer(initialSize)
	def errStream = new StringBuffer(initialSize)
	def proc = command.execute()
	proc.out.flush()
	proc.out.close()
	proc.waitForProcessOutput(outStream, errStream)
}



