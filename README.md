ArduinoFirmata-Android
======================
Arduino Firmata protocol (http://firmata.org) implementation on Android Java.

* http://shokai.github.com/ArduinoFirmata-Android


What is Firmata?
----------------
Firmata is a protocol to controll Arduino from Application on Android Phone.
You can write Arduino code in Java.


REQUIREMENTS:
-------------
* Arduino Standard Firmata v2.2
  * Arduino IDE -> [File] -> [Examples] -> [Firmata] -> [StandardFirmata]
* Android SDK 13+ (Android OS 3.2+)
* [usb-serial-for-android](http://code.google.com/p/usb-serial-for-android)
* [USB Host Cable](https://www.google.com/search?q=USB+host+cable)


SETUP:
------

1. Copy [arduino-firmata.jar](https://github.com/shokai/ArduinoFirmata-Android/raw/master/arduino-firmata.jar) and [usb-serial-for-android.jar](http://code.google.com/p/usb-serial-for-android) to "libs" directory
2. Copy [device_filter.xml](http://usb-serial-for-android.googlecode.com/git/UsbSerialExamples/res/xml/device_filter.xml) to "res/xml" directory
3. edit AndroidManifest.xml

```xml
<activity android:name="MainActivity" />
  <intent-filter>
    <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
  </intent-filter>
  <meta-data
      android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
      android:resource="@xml/device_filter" />
</activity>
```

<img src="http://shokai.org/archive/file/9cc50dffd79a489b23fcf75e4250d4fa.png">



SYNOPSIS:
---------

### Connect

```java
import org.shokai.firmata.ArduinoFirmata;

ArduinoFirmata arduino = new ArduinoFirmata();
try{
  arduino.connect();
}
catch(IOException e){
  e.printStackTrace();
}
```


### Digital Write
```java
arduino.digitalWrite(13, true); // on board LED
arduino.digitalWrite(13, false);
```

### Digital Read
```java
arduino.pinMode(7, ArduinoFirmata.INPUT);
arduino.digitalRead(7);  // => true/false
```

### Analog Write (PWM)
```java
arduino.analogWrite(11, 230); // pinNumber, value(0~255)
```

### Analog Read
```java
arduino.analogRead(0);  // => 0 ~ 1023
```

### Close
```java
arduino.close();
```

### Set Event Handler
```java
arduino.setEventHandler(
  new ArduinoFirmataEventHandler(){
    public void onError(String errorMessage){
      Log.e("ArduinoFirmata App", errorMessage);
    }
    public void onClose(){
      Log.v("ArduinoFirmata App", "arduino closed");
    }
  }
);
```

### Samples
https://github.com/shokai/ArduinoFirmata-Android/blob/master/samples


Contributing
------------
1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

### Build arduino-firmata.jar

set Android SDK Path

    % export CLASSPATH=$CLASSPATH:/usr/local/var/lib/android-sdk/platforms/android-13/android.jar
    % export LC_ALL=en

build jar

    % make init
    % make

=> arduino-firmata.jar