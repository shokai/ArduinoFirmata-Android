ArduinoFirmata Sysex Sample App
===============================

## Setup

    % make setup

## Build & Install

    % make
    % make install

## WiFi Debug

    % adb tcpip 5555
    % adb connect 192.168.1.101:5555
    % adb logcat | grep "^./Arduino"
