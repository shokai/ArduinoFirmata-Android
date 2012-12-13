package org.shokai.firmata;

import com.hoho.android.usbserial.driver.*;

import java.io.*;
import java.lang.*;
import android.hardware.usb.*;
import android.app.*;
import android.os.*;
import android.content.*;
import android.util.Log;

public class ArduinoFirmata{
    public final static String VERSION = "0.0.1.beta";
    public final static String TAG = "ArduinoFirmata";

    public static final int INPUT  = 0;
    public static final int OUTPUT = 1;
    public static final int ANALOG = 2;
    public static final int PWM    = 3;
    public static final int SERVO  = 4;
    public static final int SHIFT  = 5;
    public static final int I2C    = 6;
    public static final boolean LOW   = false;
    public static final boolean HIGH  = true;
    private final int MAX_DATA_BYTES  = 32;
    private final int DIGITAL_MESSAGE = 0x90;
    private final int ANALOG_MESSAGE  = 0xE0;
    private final int REPORT_ANALOG   = 0xC0;
    private final int REPORT_DIGITAL  = 0xD0;
    private final int SET_PIN_MODE    = 0xF4;
    private final int REPORT_VERSION  = 0xF9;
    private final int SYSTEM_RESET    = 0xFF;
    private final int START_SYSEX     = 0xF0;
    private final int END_SYSEX       = 0xF7;

    private UsbSerialDriver usb;
    private Context context;
    private Thread th_receive = null;
    private ArduinoFirmataEventHandler handler;
    public void setEventHandler(ArduinoFirmataEventHandler handler){
        this.handler = handler;
    }

    private int waitForData = 0;
    private int executeMultiByteCommand = 0;
    private int multiByteChannel = 0;
    private int[] storedInputData = new int[MAX_DATA_BYTES];
    private boolean parsingSysex;
    private int sysexBytesRead;
    private int[] digitalOutputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int[] digitalInputData  = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int[] analogInputData   = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int majorVersion = 0;
    private int minorVersion = 0;
    public String getBoardVersion(){
        return String.valueOf(majorVersion)+"."+String.valueOf(minorVersion);
    }

    public ArduinoFirmata(android.app.Activity context){
        this.context = context;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.usb = UsbSerialProber.acquire(manager);
    }

    public void connect() throws IOException, InterruptedException{
        if(!this.isOpen()) throw new IOException("device not found");
        try{
            this.usb.open();
            this.usb.setBaudRate(57600);
        }
        catch(IOException e){
            throw e;
        }
        if(this.th_receive == null){
            this.th_receive = new Thread(new Runnable(){
                    public void run(){
                        while(isOpen()){
                            try{
                                byte buf[] = new byte[256];
                                int size = usb.read(buf, buf.length);
                                for(int i = 0; i < size; i++){
                                    processInput(buf[i]);
                                }
                                Thread.sleep(10);
                            }
                            catch(IOException e){
                                close();
                                if(handler!=null) handler.onClose();
                            }
                            catch(InterruptedException e){
                                if(handler!=null) handler.onError(e.toString());
                            }
                        }
                    }
                });
            this.th_receive.start();
        }

        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        for (int i = 0; i < 6; i++) {
            write(REPORT_ANALOG | i);
            write(1);
        }
        for (int i = 0; i < 2; i++) {
            write(REPORT_DIGITAL | i);
            write(1);
        }
    }

    public boolean isOpen(){
        return this.usb != null;
    }

    public boolean close(){
        try{
            this.usb.close();
            this.usb = null;
            return true;
        }
        catch(IOException e){
            if(handler!=null) handler.onError(e.toString());
            return false;
        }
    }

    public void write(int data){
        byte[] writeData = {(byte)data};
        try{
            if(this.isOpen()) this.usb.write(writeData, 100);
        }
        catch(IOException e){
            this.close();
            if(handler!=null) handler.onClose();
        }
    }

    public boolean digitalRead(int pin) {
        return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
    }

    public int analogRead(int pin) {
        return analogInputData[pin];
    }

    public void pinMode(int pin, int mode) {
        write(SET_PIN_MODE);
        write(pin);
        write(mode);
    }

    public void digitalWrite(int pin, boolean value) {
        int portNumber = (pin >> 3) & 0x0F;
        if (!value) digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
        else digitalOutputData[portNumber] |= (1 << (pin & 0x07));
        write(DIGITAL_MESSAGE | portNumber);
        write(digitalOutputData[portNumber] & 0x7F);
        write(digitalOutputData[portNumber] >> 7);
    }

    public void analogWrite(int pin, int value) {
        pinMode(pin, PWM);
        write(ANALOG_MESSAGE | (pin & 0x0F));
        write(value & 0x7F);
        write(value >> 7);
    }

    private void setDigitalInputs(int portNumber, int portData) {
        digitalInputData[portNumber] = portData;
    }

    private void setAnalogInput(int pin, int value) {
        analogInputData[pin] = value;
    }

    private void setVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    private void processInput(byte inputData){
        int command;
        if (parsingSysex) {
            if (inputData == END_SYSEX) {
                parsingSysex = false;
            } else {
                storedInputData[sysexBytesRead] = inputData;
                sysexBytesRead++;
            }
        } else if (waitForData > 0 && inputData < 128) {
            waitForData--;
            storedInputData[waitForData] = inputData;
            if (executeMultiByteCommand != 0 && waitForData == 0) {
                switch(executeMultiByteCommand) {
                case DIGITAL_MESSAGE:
                    setDigitalInputs(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
                    break;
                case ANALOG_MESSAGE:
                    setAnalogInput(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
                    break;
                case REPORT_VERSION:
                    setVersion(storedInputData[1], storedInputData[0]);
                    break;
                }
            }
        }
        else {
            if(inputData < 0xF0) {
                command = inputData & 0xF0;
                multiByteChannel = inputData & 0x0F;
            } else {
                command = inputData;
            }
            switch (command) {
            case DIGITAL_MESSAGE:
            case ANALOG_MESSAGE:
            case REPORT_VERSION:
                waitForData = 2;
                executeMultiByteCommand = command;
                break;
            }
        }
    }

}