package uk.co.corelogic.npa.nagios

class MessageHandler {

String gwfServer = "nagios.corelogic.local"
int gwfPort = 4913
String output
String messageContent

MessageHandler(String messageContent) {
	this.messageContent = messageContent
}

public String forwardMessage() throws Exception {
	s = new Socket(this.gwfServer, this.gwfPort);
	s << this.messageContent + "\n"
	s >> this.output
	log.Debug(output)
	s.close()
}



}


