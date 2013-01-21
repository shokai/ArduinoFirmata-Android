package org.shokai.firmata.sysexsample;

import org.shokai.firmata.ArduinoFirmata;
import org.shokai.firmata.ArduinoFirmataEventHandler;

import java.io.*;
import java.lang.*;
import android.hardware.usb.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.widget.*;
import android.view.*;
import android.view.View.OnClickListener;

public class Main extends Activity
{
    private String TAG = "ArduinoFirmataSysexSample";
    private ArduinoFirmata arduino;
    private Button btnSysex1, btnSysex2;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.btnSysex1 = (Button)findViewById(R.id.btn_sysex1);
        this.btnSysex2 = (Button)findViewById(R.id.btn_sysex2);

        Log.v(TAG, "start");
        Log.v(TAG, "Firmata Lib Version : "+ArduinoFirmata.VERSION);
        this.setTitle(this.getTitle()+" v"+ArduinoFirmata.VERSION);

        this.arduino = new ArduinoFirmata(this);
        final Activity self = this;
        arduino.setEventHandler(new ArduinoFirmataEventHandler(){
                public void onError(String errorMessage){
                    Log.e(TAG, errorMessage);
                }
                public void onClose(){
                    Log.v(TAG, "arduino closed");
                    self.finish();
                }
            });

        this.btnSysex1.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    Log.v(TAG, "pin13, blink 5 times, 200 msec interval");
                    byte[] data = {13, 5, 2};
                    arduino.sysex((byte)0x01, data);
                }
            });
        this.btnSysex2.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    Log.v(TAG, "pin11, blink 3 times, 1000 msec interval");
                    byte[] data = {11, 3, 10};
                    arduino.sysex((byte)0x01, data);
                }
            });


        try{
            arduino.connect();
            Log.v(TAG, "Board Version : "+arduino.getBoardVersion());
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
