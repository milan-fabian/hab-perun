#!/bin/bash

ANDROID_TELEMETRY=android.txt

checkAdb() {
	while true; do
		adb devices | grep -w device
		if [[ $? == 0 ]]; then
			break;
		fi
		sleep 2s
	done
}

setupDevice() {
	echo ">> Disable sleep on Android"
	adb shell dumpsys deviceidle disable
	adb shell dumpsys deviceidle whitelist
	
	echo ">> Starting service on Android"
	adb shell am startservice sk.mimac.perun.android/.MainService
	
	echo ">> Forwarding ports on Android"
	adb forward tcp:9900 tcp:9900
	adb forward tcp:9901 tcp:9901
	
	sleep 10s
}

mainLoop() {
	echo ">> Starting communication with via ADB"
	checkAdb
	echo ">> ADB connected"
	setupDevice
	
	lastLine=""
	counter=1
	
	while true; do
		echo ">> Exchanging data between RPI and Android"
		
		line=$(tail -n 1 telemetry.txt)
		if [ "$line" != "$lastLine" ]; then
			echo "$line" | nc localhost 9900 >> $ANDROID_TELEMETRY || break
		else 
			echo "" | nc localhost 9900 >> $ANDROID_TELEMETRY || break		
		fi
		echo >> $ANDROID_TELEMETRY

		# Send image every 15s * 8 = 2 minutes 
		if (( $number % 8 == 0 )); then
			# TODO
		fi
		
		((counter++))
		sleep 15s
	done
	echo ">> Error while exchanging data, restarting"
}

echo ">> Starting pits tracker"
cd /home/pi/pits/tracker
rm $ANDROID_TELEMETRY
touch $ANDROID_TELEMETRY
./startup 2>&1 | multilog t s100000 n3 ./log &

while true; do
	mainLoop
done
