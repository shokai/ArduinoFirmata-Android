package org.shokai.firmata.sample;

import java.io.*;
import java.lang.*;
import android.hardware.usb.*;
import com.hoho.android.usbserial.driver.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

public class Main extends Activity
{
    private String TAG = "ArduinoFirmataSample";

    @Override
        public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v(TAG, "start activity");

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbSerialDriver usb = UsbSerialProber.acquire(manager);
        if(usb != null){
            try{
                usb.open();
                usb.setBaudRate(9600);
                start_arduino(usb);
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public void start_arduino(UsbSerialDriver __usb){
        final UsbSerialDriver usb = __usb;
        new Thread(new Runnable(){
                public void run(){
                    Log.v(TAG, "start read thread");
                    while(true){
                        try{
                            byte buf[] = new byte[256];
                            int num = usb.read(buf, buf.length);
                            if(num > 0) Log.v(TAG, new String(buf, 0, num)); // Arduinoから受信した値をlogcat出力
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
                            Thread.sleep(1000);
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
