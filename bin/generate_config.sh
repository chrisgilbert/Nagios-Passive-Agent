# Please make sure the java path is correct
#
# This script will attempt to automatically update NPA to the latest
# stable version from the internet
# It will use the proxy settings in config/defaults.groovy if available
#
java -cp ../lib/groovy-all-1.7.10.jar groovy.lang.GroovyShell generate_config.groovy
