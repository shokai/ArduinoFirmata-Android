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
    private UsbSerialDriver usb;

    private Context context;
    public ArduinoFirmata(android.app.Activity context){
        this.context = context;
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        this.usb = UsbSerialProber.acquire(manager);
    }

    public void start() throws IOException, InterruptedException{

        if(this.usb == null) throw new IOException("device not found");

        try{
            this.usb.open();
            this.usb.setBaudRate(9600);
        }
        catch(IOException e){
            throw e;
        }

        final UsbSerialDriver usb = this.usb;
        new Thread(new Runnable(){
                public void run(){
                    Log.v(TAG, "start read thread");
                    while(true){
                        try{
                            byte buf[] = new byte[256];
                            int num = usb.read(buf, buf.length);
                            if(num > 0) Log.v(TAG, new String(buf, 0, num));
                            Thread.sleep(10);
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        new Thread(new Runnable(){
                public void run(){
                    boolean stat = false;
                    while(true){
                        try{
                            Thread.sleep(300);
                            String data = stat ? "o" : "x";
                            Log.v(TAG, data);
                            usb.write(data.getBytes(), 10);
                            stat = !stat;
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
    }
}