#!/bin/bash

echo "========================================================================="
echo "Update & Upgrade Server"
echo "========================================================================="
sudo apt-get update && sudo apt-get -y upgrade

echo "========================================================================="
echo "Initializing Server"
echo "========================================================================="
sudo free -m
echo "========================================================================="
echo "Would you like to setup 4GB SWAP Memory?"
echo "*** DO NOT USE TWICE! ***"
echo "========================================================================="
read -p "Yes or no? If you're unsure, please type 'no': " swap
if [[ $swap == [Yy][Ee][Ss] ]] ; then
    sudo echo "Installing 4GB Swap Memory..."
    sudo echo "*** Do NOT cancel, this may take some time ***"
    sudo dd if=/dev/zero of=/swap bs=1024 count=4096k
    sudo mkswap /swap
    sudo swapon /swap
    echo " /swap       none    swap    sw      0       0 " >> /etc/fstab
    sudo chown root:root /swap
    sudo chmod 0600 /swap
    echo "vm.swappiness=90" >> /etc/sysct1.conf
    echo "4GB SWAP Memory install COMPLETED!"
fi

echo "========================================================================="
echo " Installing APPS... "
echo "========================================================================="
sudo mkdir /apps

echo "========================================================================="
echo " Installing Java... "
echo "========================================================================="
sudo apt-get install -y ant openjdk-7-jre openjdk-6-jre default-jdk

echo "========================================================================="
echo " Installing LAMP & PHPMyAdmin... "
echo "========================================================================="
apt-get install -y apache2 apache2-mpm-prefork apache2-utils 
apt-get install -y apache2.2-common libapache2-mod-php5
apt-get install -y libapr1 libaprutil1 libdbd-mysql-perl libdbi-perl 
apt-get install -y libnet-daemon-perl libplrpc-perl libpq5 
apt-get install -y mysql-server mysql-server-5.5
apt-get install -y php5 php5-gd php5-curl php5-imagick php5-ffmpeg php5-cli 
apt-get install -y php5-mcrypt php-mdb2-driver-mysql
apt-get install -y lamp-server^
echo "ServerName localhost" >> /etc/apache2/httpd.conf
