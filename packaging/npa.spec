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
This means it can be deployed in envrionments behind a firewall, and run independant checks which submit back to a central server.
It contains some inbuilt operating system and database checks, and can run standard nagios plugins.  For more information on nagios,
visit http://www.nagios.org/

%prep
%setup

%build
ant jar

%install
export INSTALLDIR=/usr/local/npa
/usr/sbin/groupadd npa >2/dev/null
/usr/sbin/useradd npa -g npa  >2/dev/null

mkdir -p $INSTALLDIR/db

cp -r npa.jar db config lib wrapper-lib bin logs doc $INSTALLDIR
if [ $? -eq 0 ]; then
	echo Copied files ok. 
else
	echo Copying files to $INSTALLDIR produced errors!
fi

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


%files
%doc/usr/local/npa/doc/README
/usr/local/npa/*
/usr/local/npa/bin/*
/usr/local/npa/db
/usr/local/npa/logs
/usr/local/npa/config/*
/usr/local/npa/lib/*
/usr/local/npa/wrapper-lib/*

