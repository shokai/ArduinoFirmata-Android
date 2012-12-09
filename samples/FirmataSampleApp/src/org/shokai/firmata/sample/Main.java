package org.shokai.firmata.sample;

import org.shokai.firmata.ArduinoFirmata;

import java.io.*;
import java.lang.*;
import android.hardware.usb.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;

public class Main extends Activity{
    private String TAG = "ArduinoFirmataSample";
    private ArduinoFirmata arduino;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v(TAG, "start activity");
        Log.v(TAG, ArduinoFirmata.VERSION);
        
        this.arduino = new ArduinoFirmata(this);
        try{
            arduino.start();
        }
        catch(IOException e){
            e.printStackTrace();
            finish();
        }
        catch(InterruptedException e){
            e.printStackTrace();
            finish();
        }
    }
}
