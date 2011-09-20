#!/bin/bash

# Update an existing NPA installation

INSTALL_DIR=/usr/local/npa
NPAUSER=npa
NPAGROUP=npa

cd ..
echo Updating NPA installtion to new version ..
$INSTALL_DIR/bin/npa stop
cp bin/* $INSTALL_DIR/bin/
cp wrapper-lib/* $INSTALL_DIR/wrapper-lib
cp lib/* $INSTALL_DIR/lib/
cp npa.jar $INSTALL_DIR
echo Changing $INSTALL_DIR/bin/npa file..
sed "s/RUN_AS_USER=npa/RUN_AS_USER=$NPAUSER/" $INSTALL_DIR/bin/npa > $INSTALL_DIR/bin/npa.new
mv $INSTALL_DIR/bin/npa.new $INSTALL_DIR/bin/npa
chmod 755 $INSTALL_DIR/bin/*
chmod 600 $INSTALL_DIR/config/npa.xml
chmod 600 $INSTALL_DIR/config/defaults.groovy
chown -R $NPAUSER:$NPAGROUP $INSTALL_DIR

echo Renaming new config files to .newrelease
mkdir $INSTALL_DIR/config/new-config 2>/dev/null
cp config/npa.xml $INSTALL_DIR/config/npa.xml.newrelease
cp config/defaults.groovy $INSTALL_DIR/config/defaults.groovy.newrelease
cp config/wrapper.conf $INSTALL_DIR/config/wrapper.conf.newrelease 
cp -r config/samples $INSTALL_DIR/config/

$INSTALL_DIR/bin/npa start

echo Done.
