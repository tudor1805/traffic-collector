#!/bin/bash

if [ "$EUID" != "0" ] ; then
    echo "Script must be run as root"
    exit 1
fi

APACHE_HTML_DIR="/var/www/html"
TRAFFIC_DIR="traffic"

mkdir -p  "$APACHE_HTML_DIR"
chmod 777 "$APACHE_HTML_DIR"

echo "Deploying [$TRAFFIC_DIR] to [$APACHE_HTML_DIR]"

rm -rf "$APACHE_HTML_DIR/$TRAFFIC_DIR"
cp -r  "$TRAFFIC_DIR" "$APACHE_HTML_DIR"

echo "Done"
