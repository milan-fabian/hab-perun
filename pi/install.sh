#!/bin/sh

# Installation script for hab-perun Raspberry Pi microcontroller
# Partially taken from http://www.pi-in-the-sky.com/index.php?id=sd-card-image-from-scratch

echo "Starting installation hab-perun..."

sudo apt-get install -y android-tools-adb git wiringpi || exit 10
sudo systemctl disable hciuart || exit 11


echo "Successfully finished"
