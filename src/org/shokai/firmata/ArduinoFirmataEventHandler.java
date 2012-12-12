package org.shokai.firmata;

public interface ArduinoFirmataEventHandler{
    public void onClose();
    public void onError(String errorMessage);
}