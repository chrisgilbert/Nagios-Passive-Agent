#!/bin/bash

# Defaults
export NPAUSER=npa
export NPAGROUP=npa
export INSTALLDIR=/usr/local/npa
export RUNCORRECT=y


#
# Don't edit past here
#

if [ $(id -u) -eq 0 ]; then
  echo Root access is available: good.
else
  echo Please install as root.
  exit 1
fi


STATUS=1

set_status() {
  if [ $1 -eq 1 ]; then
    STATUS=1
  fi
}

if [ $# -ne 3 ]; then
  echo "There weren't any install arguments, so these defaults will be used:"
  echo INSTALL DIRECTORY: $INSTALLDIR
  echo NPA GROUP NAME: $NPAGROUP
  echo NPA USER NAME: $NPAUSER
  echo "If you wish to specify them yourself, then please enter $0 [INSTALL DIR] [NPA USER] [NPA GROUP].  These can be existing group/users if you wish."
else
  NPAUSER=$2
  NPAGROUP=$3
  INSTALLDIR=$1
  echo Using these parameters:  
  echo INSTALL DIRECTORY: $INSTALLDIR
  echo NPA GROUP NAME: $NPAGROUP
  echo NPA USER NAME: $NPA USER
fi


# Run an update if NPA is already installed
if [ -f $INSTALLDIR/npa.jar ]; then
  echo NPA is already installed, running update..
  bash update_npa.sh


else

/usr/sbin/groupadd $NPAUSER
/usr/sbin/useradd $NPAUSER -g $NPAGROUP

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

chown -R ${NPAUSER}:${NPAGROUP} ${INSTALLDIR}
if [ $? -eq 0 ]; then
	echo Installtion completed.  NPA installed in $INSTALLDIR
	set_status 0
else
	Change of ownership failed!
	set_status 1
	echo Critical error occurred. Exiting.
	exit 1
fi


ln -s $INSTALLDIR/bin/npa /etc/init.d/npa 
/sbin/chkconfig --add npa
/sbin/chkconfig --level 345 npa on

if [ $? -eq 0 ]; then
	echo Successfully installed npa service.
	set_status 0
else
	echo Chkconfig command to install npa service failed!
	set_status 1
fi


#
# Add a cron job to restart the NPA agent every hour, and update it once a week
#
echo "Attempting to add cron jobs.. (these go into /etc/cron.hourly and /etc/cron.daily)"
ln -s /etc/cron.hourly/npa-restart-cron $INSTALLDIR/bin/restart-npa.sh 
ln -s /etc/cron.weekly/npa-update $INSTALLDIR/bin/update_npa.sh
if [ $? -eq 0 ]; then
	echo Successfully installed cron jobs.
	set_status 0
else
	echo Failed to install cron jobs!
	set_status 1
fi




if [${STATUS} -eq 1 ]; then
  echo Errors occurred during install!
  exit 1
else
  exit 0
fi
fi
