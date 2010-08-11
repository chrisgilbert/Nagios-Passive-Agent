package uk.co.corelogic.npa.reports
import uk.co.corelogic.npa.reports.*

class Report {

def rep
def rep_name
def rep_category
def rep_interval
private reportsList = [:]
def sockOutput = new StringBuffer()
def errorOcc = false

    // Construct a new report object with a string from FMD interface
    Report(cmd) {
        try {
            def args = cmd.split()
            if ( args.length != 5 ) {
                sockOutput << "Invalid number of report parameters.  Should be exactly 5\n"

            }
            
            this.rep_name = args[1]
            reportType repType = Enum.valueOf(reportType, args[2].toUpper());
            
            // This bit contains the arguments - tokenize using comma seperator
            def optionsAll = args[3]
            def options = methodsAll.tokenize(",")

            // This bit contains the methods - tokenize using comma seperator
            def methodsAll = args[4]
            def methods = methodsAll.tokenize(",")

            //def obj = ["options":options, "methods":methods]

            this.createReport(repType, options, methods)
            
        } catch (e) {
            this.sockOutput << "Error occurred whilst creating report!\n"
            this.errorOcc = true
            Log.error("STACK:", e)
        }    
    }

    private createReport(type, options, methods) {
        switch( type ) {
            case ORACLE:
                def rep = new OracleReport(args)
                break
            case SQLSERVER:
                //return new sqlserverReport(args)
                break
            case AS:
                //return new asReport(args)
                break
            case OS:
                //return new osReport(args)
                break
            default:
                this.sockOutput << "Invalid report type specified."
                this.errorOcc = true
            break
        }
        methods.each {
            try {
                // Very groovy way of doing things - dynamically invoke requested methods
                rep.invokeMethod(it)
            } catch (e) {
                sockOutput << "You specified an incorrect method - $it - for report type $type!"
                this.errorOcc = true
            }

        }
        this.rep = rep
    }


    public getHTMLFormattedResults() {


    }

    public getWikiFormattedResults() {

    }

    public getTextFormattedResults() {
        def output = new StringBuffer()
        this.reportsList.each { list ->
            output << "Report for" << it.key
            list.each {
                output << it
            }
        }
        return output
    }

    private returnString() {
        if ( this.errorOcc ) {
            return sockOutput
        } else {
            return getTextFormattedResults()
        }
    }

}