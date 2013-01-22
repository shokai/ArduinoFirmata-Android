package org.shokai.firmata;

public interface ArduinoFirmataDataHandler{
    public void onSysex(byte command, byte[] data);
}