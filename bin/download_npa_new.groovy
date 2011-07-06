
//
// Simple update class, to update NPA to the most recent version.  This will update the npa.jar file and download any new libraries,
// but not make any changes to configuration
//

def npaURL="http://www.disciple3d.co.uk/npa-stable.jar"
newFile="npa-stable.jar"
def libList="http://www.disciple3d.co.uk/libs.txt"
def suffix=123

def config = new ConfigSlurper().parse(new File("../config/defaults.groovy").toURL())

// This grabs NPA settings for proxy and uses them if necessary
def proxyHost=config.npa.proxy_host
def proxyPort=config.npa.proxy_port
def proxyUserName=config.npa.proxy_username
def proxyPasswd=config.npa.proxy_password

if ( proxyHost != null ) {
	println("Using proxy $proxyHost:$proxyPort")
        if (config.npa.proxy_enabled == "true" ){
            System.properties.putAll( ["http.proxyHost":proxyHost, "http.proxyPort":proxyPort,"http.proxyUserName":proxyUserName, "http.proxyPassword":proxyPasswd] )
        }
}

println("Downloading release from $npaURL")
download(npaURL, '../')
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
    	  download(newLib, "../lib")
        }
}


stopNpa()
renameFile("../npa.jar", "../npa.jar.old${suffix}")
renameFile("../" + newFile, "../npa.jar")
startNpa()


def download(address, toDir)
{
    println("Downloading to: " + toDir + "/" + new URL(address).getFile())
    def file = new FileOutputStream(toDir + "/" + new URL(address).getFile())
    def out = new BufferedOutputStream(file)
    out << new URL(address).openStream()
    out.close()
    return (toDir + "/" + new URL(address).getFile())
}

def stopNpa() {
   "./npa stop".execute()
}


def startNpa() {
   "./npa start".execute()
}
