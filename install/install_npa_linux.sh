#!/bin/bash


INSTALLDIR=/usr/local/npa
/usr/sbin/groupadd npa
/usr/sbin/useradd npa -g npa

mkdir -p $INSTALLDIR/db

cd ..
cp -r npa.jar db config lib wrapper-lib bin logs $INSTALLDIR
if [ $? -eq 0 ]; then
	echo Copied files ok. 
else
	echo Copying files to $INSTALLDIR produced errors!
fi

echo Changing $INSTALLDIR/bin/npa file..
sed "s/RUN_AS_USER=npa/RUN_AS_USER=$NPAUSER/" $INSTALLDIR/bin/npa > $INSTALLDIR/bin/npa.new
mv $INSTALLDIR/bin/npa.new $INSTALLDIR/bin/npa
chmod 755 $INSTALLDIR/bin/*

chown -R npa:npa $INSTALLDIR
if [ $? -eq 0 ]; then
	echo Installtion completed.  NPA installed in $INSTALLDIR
else
	Change of ownership failed!
	exit 1
fi


ln -s $INSTALLDIR/bin/npa /etc/init.d/npa 
/sbin/chkconfig --add npa
/sbin/chkconfig --level 345 npa on

if [ $? -eq 0 ]; then
	echo Successfully installed npa service.
	exit 0
else
	echo Chkconfig command to install npa service failed!
	exit 1
fi



