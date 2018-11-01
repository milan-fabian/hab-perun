# Android application for high altitude balloon project "Perun"

Application running on Samsung Galaxy Note 4 as a part of the balloon payload.

## Goals

* Send photos (low-res), location and sensor readings from Galaxy Note as well as Raspberry Pi to the server via GSM/3G (may fail in high altitudes)
* Save photos (high-res), location and sensor reading from Galaxy Note to internal flash memory (for use after payload recovery)

## Power conservation

* The display is off all the time, but there have to be Wake Lock in place, so the CPU won't stop
* When battery level is lower than 25%, there is gradually more power conservation effects in place (lowering sensor reading and picture taking rate)

It is necessary to forbid idle mode on the device via ADB:
```
adb shell dumpsys deviceidle disable
adb shell dumpsys deviceidle whitelist
```
Otherwise the application will stop running after 1-2 hours.
