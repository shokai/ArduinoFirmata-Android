package org.shokai.firmata.sample;

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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Main extends Activity{
    private String TAG = "ArduinoFirmataSample";
    private Handler handler;
    private ArduinoFirmata arduino;
    private ToggleButton btnDigitalWrite;
    private SeekBar seekAnalogWrite;
    private TextView textAnalogRead;
    private TextView textDigitalRead;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.handler = new Handler();
        this.btnDigitalWrite = (ToggleButton)findViewById(R.id.btn_digital_write);
        this.seekAnalogWrite = (SeekBar)findViewById(R.id.seek_analog_write);
        this.seekAnalogWrite.setMax(255);
        this.seekAnalogWrite.setProgress(10);
        this.textAnalogRead = (TextView)findViewById(R.id.text_analog_read);
        this.textDigitalRead = (TextView)findViewById(R.id.text_digital_read);

        Log.v(TAG, "start");
        Log.v(TAG, "Firmata Lib Version : "+ArduinoFirmata.VERSION);

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

        btnDigitalWrite.setOnCheckedChangeListener(new OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton btn, boolean isChecked){
                    Log.v(TAG, isChecked ? "LED on" : "LED off");
                    arduino.digitalWrite(13, isChecked);
                }
            });

        seekAnalogWrite.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
                public void onProgressChanged(SeekBar seekBar, int value, boolean fromTouch){
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int value = seekBar.getProgress();
                    Log.v(TAG, "analogWrite(11, "+String.valueOf(value)+")");
                    arduino.analogWrite(11, value);
                }
            });

        Thread thread = new Thread(new Runnable(){
                public void run(){
                    while(arduino.isOpen()){
                        try{
                            Thread.sleep(100);
                            handler.post(new Runnable(){
                                    public void run(){
                                        int analog_value = arduino.analogRead(0);
                                        boolean digital_value = arduino.digitalRead(7);
                                        textAnalogRead.setText("analogRead(0) = "+String.valueOf(analog_value));
                                        textDigitalRead.setText("digitalRead(7) = "+String.valueOf(digital_value));
                                    }
                                });
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            });

        try{
            arduino.connect();
            arduino.pinMode(7, ArduinoFirmata.INPUT);
            thread.start();
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
