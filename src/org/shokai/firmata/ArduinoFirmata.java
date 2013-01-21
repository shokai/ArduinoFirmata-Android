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
    public final static String VERSION = "0.1.3";
    public final static String TAG = "ArduinoFirmata";

    public static final byte INPUT  = 0;
    public static final byte OUTPUT = 1;
    public static final byte ANALOG = 2;
    public static final byte PWM    = 3;
    public static final byte SERVO  = 4;
    public static final byte SHIFT  = 5;
    public static final byte I2C    = 6;
    public static final boolean LOW   = false;
    public static final boolean HIGH  = true;
    private final byte MAX_DATA_BYTES  = 32;
    private final byte DIGITAL_MESSAGE = (byte)0x90;
    private final byte ANALOG_MESSAGE  = (byte)0xE0;
    private final byte REPORT_ANALOG   = (byte)0xC0;
    private final byte REPORT_DIGITAL  = (byte)0xD0;
    private final byte SET_PIN_MODE    = (byte)0xF4;
    private final byte REPORT_VERSION  = (byte)0xF9;
    private final byte SYSTEM_RESET    = (byte)0xFF;
    private final byte START_SYSEX     = (byte)0xF0;
    private final byte END_SYSEX       = (byte)0xF7;

    private UsbSerialDriver usb;
    private Context context;
    private Thread th_receive = null;
    private ArduinoFirmataEventHandler handler;
    public void setEventHandler(ArduinoFirmataEventHandler handler){
        this.handler = handler;
    }

    private int waitForData = 0;
    private byte executeMultiByteCommand = 0;
    private byte multiByteChannel = 0;
    private byte[] storedInputData = new byte[MAX_DATA_BYTES];
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
        if(this.usb == null) throw new IOException("device not found");
        try{
            this.usb.open();
            this.usb.setBaudRate(57600);
            Thread.sleep(3000);
        }
        catch(InterruptedException e){
            throw e;
        }
        catch(IOException e){
            throw e;
        }
        if(this.th_receive == null){
            this.th_receive = new Thread(new Runnable(){
                    public void run(){
                        while(isOpen()){
                            try{
                                byte buf[] = new byte[4096];
                                int size = usb.read(buf, 100);
                                if(size > 0){
                                    for(int i = 0; i < size; i++){
                                        processInput(buf[i]);
                                    }
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

        byte[] writeData = {0, 1};
        for (byte i = 0; i < 6; i++) {
            write((byte)(REPORT_ANALOG | i));
            write((byte)1);
        }
        for (byte i = 0; i < 2; i++) {
            write((byte)(REPORT_DIGITAL | i));
            write((byte)1);
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

    public void write(byte[] writeData){
        try{
            if(this.isOpen()) this.usb.write(writeData, 100);
        }
        catch(IOException e){
            this.close();
            if(handler!=null) handler.onClose();
        }
    }

    public void write(byte writeData){
        byte[] _writeData = {(byte)writeData};
        write(_writeData);
    }

    public void reset(){
        write(SYSTEM_RESET);
    }

    public void sysex(byte command, byte[] data){
        // http://firmata.org/wiki/V2.1ProtocolDetails#Sysex_Message_Format
        if(data.length > 32) return;
        byte[] writeData = new byte[data.length+3];
        writeData[0] = START_SYSEX;
        writeData[1] = command;
        for(int i = 0; i < data.length; i++){
            writeData[i+2] = (byte)(data[i] & 127); // 7bit
        }
        writeData[writeData.length-1] = END_SYSEX;
        write(writeData);
    }

    public boolean digitalRead(int pin) {
        return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
    }

    public int analogRead(int pin) {
        return analogInputData[pin];
    }

    public void pinMode(int pin, byte mode) {
        byte[] writeData = {SET_PIN_MODE, (byte)pin, mode};
        write(writeData);
    }

    public void digitalWrite(int pin, boolean value) {
        byte portNumber = (byte)((pin >> 3) & 0x0F);
        if (!value) digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
        else digitalOutputData[portNumber] |= (1 << (pin & 0x07));
        byte[] writeData = {
            SET_PIN_MODE, (byte)pin, OUTPUT,
            (byte)(DIGITAL_MESSAGE | portNumber),
            (byte)(digitalOutputData[portNumber] & 0x7F),
            (byte)(digitalOutputData[portNumber] >> 7)
        };
        write(writeData);
    }

    public void analogWrite(int pin, int value) {
        byte[] writeData = {
            SET_PIN_MODE, (byte)pin, PWM,
            (byte)(ANALOG_MESSAGE | (pin & 0x0F)),
            (byte)(value & 0x7F),
            (byte)(value >> 7)
        };
        write(writeData);
    }

    public void servoWrite(int pin, int angle){
        byte[] writeData = {
            SET_PIN_MODE, (byte)pin, SERVO,
            (byte)(ANALOG_MESSAGE | (pin & 0x0F)),
            (byte)(angle & 0x7F),
            (byte)(angle >> 7)
        };
        write(writeData);
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
        byte command;
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
                command = (byte)(inputData & 0xF0);
                multiByteChannel = (byte)(inputData & 0x0F);
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