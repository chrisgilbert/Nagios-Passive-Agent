@echo off
rem Please make sure the java path is correct
rem
rem This script will attempt to automatically update NPA to the latest
rem stable version from the internet
rem It will use the proxy settings in config/defaults.groovy if available
rem
java -cp ..\lib\groovy-all-1.7.10.jar groovy.lang.GroovyShell download_npa_new.groovy
pause

