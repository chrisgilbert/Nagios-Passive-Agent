#
# NPA spec file
#
Summary: A nagios passive monitoring agent.
Name: npa
Version: [VERSION]
Release: [BUILD]
Copyright: GPL
Group: System
Source: http://www.github.com/chrisgilbert/npa/
URL: http://npa.sourceforge.net/
Vendor: Corelogic Ltd
Packager: Chris Gilbert <disciple3d@gmail.com>

%description
A Nagios passive monitoring agent.  Unlike other available agents, NPA only needs communication back to a Nagios server via HTTP/S, directly or via a web proxy.
This means it can be deployed in envrionments behind a firewall, and run independent checks which submit back to a central server.
It contains some inbuilt operating system and database checks, and can run standard nagios plugins.  For more information on nagios,
visit http://www.nagios.org/

%prep
%setup

%build
ant jar

%install

# Defaults
NPAUSER=npa
NPAGROUP=npa
INSTALLDIR=/usr/local/npa

#
# Don't edit past here
#

STATUS=1

set_status() {
  if [ $1 -eq 1 ];
    STATUS=1
  fi
}

if [ $# -ne 3 ]; then
  echo "There weren't any install arguments, so these defaults will be used:"
  echo INSTALL DIRECTORY: $INSTALLDIR
  echo NPA GROUP NAME: $NPAGROUP
  echo NPA USER NAME: $NPA USER
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


%files
%doc/usr/local/npa/doc/README
/usr/local/npa/*
/usr/local/npa/bin/*
/usr/local/npa/db
/usr/local/npa/logs
/usr/local/npa/config/*
/usr/local/npa/lib/*
/usr/local/npa/wrapper-lib/*

