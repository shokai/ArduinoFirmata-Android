# Sysex Command Samples

Sysex
-----
- http://firmata.org/wiki/V2.1ProtocolDetails#Sysex_Message_Format

Format
------
```
Generic Sysex Message
0     START_SYSEX (0xF0)
1     sysex command (0x00-0x7F)
x     between 0 and MAX_DATA_BYTES 7-bit bytes of arbitrary data
last  END_SYSEX (0xF7)
```

Samples
-------
- SysexSampleApp
  - Android App
  - send Sysex command "0x01" to Arduino
- SysexLedBlinkFirmata
  - receive "0x01" comamnd and run
- StandardFirmataWithLedBlink
  - embed SysexLedBlinkFirmata's code into StandardFirmata.ino


Command definition in StandardFirmata
-------------------------------------
0x00-0x0F reserved for user-defined command.
```c
// extended command set using sysex (0-127/0x00-0x7F)
/* 0x00-0x0F reserved for user-defined commands */
#define SERVO_CONFIG            0x70 // set max angle, minPulse, maxPulse, freq
#define STRING_DATA             0x71 // a string message with 14-bits per char
#define SHIFT_DATA              0x75 // a bitstream to/from a shift register
#define I2C_REQUEST             0x76 // send an I2C read/write request
#define I2C_REPLY               0x77 // a reply to an I2C read request
#define I2C_CONFIG              0x78 // config I2C settings such as delay times and power pins
#define EXTENDED_ANALOG         0x6F // analog write (PWM, Servo, etc) to any pin
#define PIN_STATE_QUERY         0x6D // ask for a pin's current mode and value
#define PIN_STATE_RESPONSE      0x6E // reply with pin's current mode and value
#define CAPABILITY_QUERY        0x6B // ask for supported modes and resolution of all pins
#define CAPABILITY_RESPONSE     0x6C // reply with supported modes and resolution
#define ANALOG_MAPPING_QUERY    0x69 // ask for mapping of analog to pin numbers
#define ANALOG_MAPPING_RESPONSE 0x6A // reply with mapping info
#define REPORT_FIRMWARE         0x79 // report name and version of the firmware
#define SAMPLING_INTERVAL       0x7A // set the poll rate of the main loop
#define SYSEX_NON_REALTIME      0x7E // MIDI Reserved for non-realtime messages
#define SYSEX_REALTIME          0x7F // MIDI Reserved for realtime messages
// these are DEPRECATED to make the naming more consistent
#define FIRMATA_STRING          0x71 // same as STRING_DATA
#define SYSEX_I2C_REQUEST       0x76 // same as I2C_REQUEST
#define SYSEX_I2C_REPLY         0x77 // same as I2C_REPLY
#define SYSEX_SAMPLING_INTERVAL 0x7A // same as SAMPLING_INTERVAL
```
