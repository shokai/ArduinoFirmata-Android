ArduinoFirmata-Android
======================
Arduino Firmata protocol implementation on Android(Java)


## Dependencies

* Android SDK 13+ (Android OS 3.2+)
* [usb-serial-for-android](http://code.google.com/p/usb-serial-for-android)


## Build arduino-firmata.jar

set Android SDK Path

    % export CLASSPATH=$CLASSPATH:/usr/local/var/lib/android-sdk/platforms/android-13/android.jar
    % export LC_ALL=en

build jar

    % make

=> arduino-firmata.jar