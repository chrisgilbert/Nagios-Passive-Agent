#!/bin/bash

NPAUSER=npa
NPAGROUP=npa
INSTALLDIR=/usr/local/npa
/usr/sbin/groupadd $NPAGROUP
/usr/sbin/useradd $NPAUSER -g $NPAGROUP

mkdir -p $INSTALLDIR/db

cd ..
cp -r npa.jar db config lib wrapper-lib bin logs $INSTALLDIR
if [ $? -eq 0 ]; then
        echo Copied files ok.
else
        echo Copying files to $INSTALLDIR produced errors!
fi

echo Changing $INSTALL_DIR/bin/npa file..
sed "s/RUN_AS_USER=npa/RUN_AS_USER=$NPAUSER/" $INSTALL_DIR/bin/npa > $INSTALL_DIR/bin/npa.new
mv $INSTALL_DIR/bin/npa.new $INSTALL_DIR/bin/npa
chmod 755 $INSTALLDIR/bin/*

chown -R $NPAUSER:$NPAGROUP $INSTALLDIR
if [ $? -eq 0 ]; then
        echo Installtion completed.  NPA installed in $INSTALLDIR
else
        Change of ownership failed!
        exit 1
fi


ln -s $INSTALLDIR/bin/npa /etc/init.d/npa
ln -s /etc/init.d/npa /etc/rc3.d/S99npa
ln -s /etc/init.d/npa /etc/rc1.d/K01npa
ln -s /etc/init.d/npa /etc/rc2.d/K01npa

if [ $? -eq 0 ]; then
        echo Successfully installed npa service.
        exit 0
else
        echo Chkconfig command to install npa service failed!
        exit 1
fi
