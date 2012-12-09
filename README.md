
## build arduino-firmata.jar

    export LC_ALL=en
    export CLASSPATH=$CLASSPATH:/usr/local/var/lib/android-sdk/platforms/android-13/android.jar:`pwd`/usb-serial-for-android/UsbSerialLibrary/src/
    make
    make deploy
