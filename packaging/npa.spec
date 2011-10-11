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

%preun

echo Removing cron jobs..
rm /etc/cron.hourly/npa-restart-cron 
rm /etc/cron.weekly/npa-update 

echo Stopping and removing NPA service...
/etc/init.d/npa stop
/sbin/chkconfig --remove npa
rm /etc/init.d/npa

if [ $? -eq 0 ]; then
    echo Successfully installed npa service.
    set_status 0
else
    echo Chkconfig command to install npa service failed!
    set_status 1
fi



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

new_install() {

if [ $# -ne 3 ]; then
  echo "There weren't any install arguments, so these defaults will be used:"
  echo INSTALL DIRECTORY: $INSTALLDIR
  echo NPA GROUP NAME: $NPAGROUP
  echo NPA USER NAME: $NPA USER
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

}

update_npa() {

echo Updating NPA installtion to new version ..
$INSTALLDIR/bin/npa stop
cp bin/* $INSTALLDIR/bin/
cp wrapper-lib/* $INSTALLDIR/wrapper-lib
cp lib/* $INSTALLDIR/lib/
cp npa.jar $INSTALLDIR
echo Changing $INSTALLDIR/bin/npa file..
sed "s/RUN_AS_USER=npa/RUN_AS_USER=$NPAUSER/" $INSTALLDIR/bin/npa > $INSTALLDIR/bin/npa.new
mv $INSTALLDIR/bin/npa.new $INSTALLDIR/bin/npa

chmod 755 $INSTALLDIR/bin/*
chmod 600 $INSTALLDIR/config/npa.xml
chmod 600 $INSTALLDIR/config/defaults.groovy
chown -R $NPAUSER:$NPAGROUP $INSTALLDIR

echo Renaming new config files to .newrelease
mkdir $INSTALLDIR/config/new-config 2>/dev/null
cp config/npa.xml $INSTALLDIR/config/npa.xml.newrelease
cp config/defaults.groovy $INSTALLDIR/config/defaults.groovy.newrelease
cp config/wrapper.conf $INSTALLDIR/config/wrapper.conf.newrelease 
cp -r config/samples $INSTALLDIR/config/

$INSTALLDIR/bin/npa start

echo Done.

}

if [ -f $INSTALLDIR/config/npa.xml ]; then
  update_npa
else
  new_install
fi

if [${STATUS} -eq 1 ]; then
  echo Errors occurred during install!
  exit 1
else
  exit 0
fi



%files
%docdir /usr/local/npa/doc
%config /usr/local/npa/config/defaults.groovy
%config /usr/local/npa/config/npa.xml
/usr/local/npa/

