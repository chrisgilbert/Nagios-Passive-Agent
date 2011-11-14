#!/bin/bash


# Defaults
export NPAUSER=oracle
export NPAGROUP=dba
export INSTALLDIR=/usr/local/corelogic/npa
export RUNCORRECT=y


#
# Don't edit past here
#

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



# Otherwise, install

else

if [ $(who am i | awk '{print $1}') == 'root' ]; then
  echo Root access is available, creating user and group.
  groupadd $NPAGROUP 
  useradd -G $NPAGROUP $NPAUSER
fi

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
chmod 600 $INSTALLDIR/config/*

chown -R $NPAUSER:$NPAGROUP $INSTALLDIR
if [ $? -eq 0 ]; then
        echo Installtion completed.  NPA installed in $INSTALLDIR
else
        Change of ownership failed!
        exit 1
fi


if [ $(who am i | awk '{print $1}') == 'root' ]; then
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

else
  echo No root access, service was not installed.
fi

fi


