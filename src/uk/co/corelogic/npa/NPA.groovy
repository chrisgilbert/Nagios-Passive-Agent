package uk.co.corelogic.npa
import java.net.ServerSocket
import uk.co.corelogic.npa.nagios.MessageHandler
import uk.co.corelogic.npa.checks.*
import uk.co.corelogic.npa.reports.*
import uk.co.corelogic.npa.common.StartChecks
import uk.co.corelogic.npa.common.*

class NPA {

def static config

static void main(String[] args) {
        System.setProperty("runPath", new File(NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString())
          config = this.getConfigObject()

        try {
          StartChecks.start()
        }
        catch (e) {
            Log.fatal("Eek.  A horrible exception made it all the way back here!", e)
            e.getStackTrace()
            MaintenanceUtil.sendShutdownHost(e.message)
            throw e
        }
        startListener()
}

static ConfigObject getConfigObject() {

        if ( System.getProperty("npa.testMode") == "true" ) {
                println("Test mode ENABLED.")
                return new ConfigSlurper().parse(
                    new File(new File(
                        NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/../config/defaults.groovy").toURL())
        } else {
            return new ConfigSlurper().parse(
                        new File(new File(
                                NPA.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().toString() + "/config/defaults.groovy").toURL())
        }
}

static void startListener() {

Thread.start {

def listenPort = config.npa.listen_port
def server = new ServerSocket(listenPort, 0, InetAddress.getLocalHost())
def openSocket = true
def messageContent
Log.info("Started NPA listener on port " + listenPort + ".")

while(true) {
    server.accept { socket ->
	Log.info("Accepting new connection from client.")

        socket.withStreams { input, output ->
            def reader = input.newReader()
            output << "--- NPA Interactive Shell Utility ---\n"
            output << "Hello!  Please enter 'shell' or post an XML message.\n"
            while(openSocket) {
		def buffer = reader.readLine()
		if ( buffer == "<npa-message>" ) {
			output << "OK. Continue with message...\n"
			messageContent = ""
			while(buffer != "</npa-message>") {
				buffer = reader.readLine()
				messageContent = messageContent + buffer + "\n"
			}
		        output << "Message Accepted.\nMessage content is: \n" + messageContent + "\n"
			openSocket = false
			try {
				def handler = new MessageHandler(messageContent)
				result = handler.forwardMessage()
			} catch(Exception e) {
				Log.error("STACK:", e);
			}
			output << "Bye!"
        } else if (buffer == "shell") {
            output << "Entering interactive terminal.\n"
            output << "npa> "
            while(buffer != "quit") {
				buffer = reader.readLine()
                output << runNPACommand(buffer)
                output << "npa> "
            }
            output << "\nClosing connection.  Bye!\n"
			openSocket = false

		} else {
			output << "Waiting for <npa-message> header or 'shell' command.  Sorry.\n"
		}


	    }
        }
    }
}
}
}

    static String runNPACommand(String cmd) {

        def obj
        switch (cmd) {
            case ~/check.*/: obj = CheckFactory.getCheck(cmd)

            break
            case "help" : return "Valid commands: check\n"
            break
            default : return "Unrecognised command.\nValid commands: check\n"
        }

        return obj.returnString()
    }

}

