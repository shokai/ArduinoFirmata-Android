
## build arduino-firmata.jar

get "usb-serial-for-android-v010.jar" from http://code.google.com/p/usb-serial-for-android/

    export LC_ALL=en
    export CLASSPATH=$CLASSPATH:/usr/local/var/lib/android-sdk/platforms/android-13/android.jar:`pwd`/usb-serial-for-android-v010.jar
    make
    make deploy
