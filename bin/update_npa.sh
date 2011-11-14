# Please make sure the java path is correct
#
# This script will attempt to automatically update NPA to the latest
# stable version from the internet
# It will use the proxy settings in config/defaults.groovy if available
#
JAVA_CMD=$(grep wrapper.java.command ../config/wrapper.conf | egrep -v '^#' | cut -d '=' -f 2)
echo Java is at: $JAVA_CMD
$JAVA_CMD -cp ../lib/groovy-all-1.7.10.jar groovy.lang.GroovyShell download_npa_new.groovy
