package org.shokai.firmata.sample;

import org.shokai.firmata.ArduinoFirmata;
import org.shokai.firmata.ArduinoFirmataEventHandler;

import java.io.*;
import java.lang.*;
import java.util.*;
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
    private SeekBar seekServoWrite;
    private TextView textAnalogRead;
    private TextView textDigitalRead;
    private long lastServoMovedAt = 0;

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
        this.seekServoWrite = (SeekBar)findViewById(R.id.seek_servo_write);
        this.seekServoWrite.setMax(180);
        this.seekServoWrite.setProgress(10);
        this.textAnalogRead = (TextView)findViewById(R.id.text_analog_read);
        this.textDigitalRead = (TextView)findViewById(R.id.text_digital_read);

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

        seekServoWrite.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
                public void onProgressChanged(SeekBar seekBar, int angle, boolean fromTouch){
                    Log.v(TAG, "servoWrite(9, "+String.valueOf(angle)+")");
                    arduino.servoWrite(9, angle);
                    lastServoMovedAt = System.currentTimeMillis() / 1000L;
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                }
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

        Thread thread = new Thread(new Runnable(){
                public void run(){
                    int servo_count = 0;
                    Random rand = new Random();
                    while(arduino.isOpen()){
                        try{
                            Thread.sleep(100);
                            handler.post(new Runnable(){
                                    public void run(){
                                        int analog_value = arduino.analogRead(0);
                                        boolean digital_value = arduino.digitalRead(7);
                                        textAnalogRead.setText("analogRead(0) = "+String.valueOf(analog_value));
                                        textAnalogRead.setTextSize(10+(float)analog_value/10);
                                        textDigitalRead.setText("digitalRead(7) = "+String.valueOf(digital_value));
                                    }
                                });
                            if(++servo_count > 20){
                                servo_count = 0;
                                Log.v(TAG, "lastServoMovedAt "+String.valueOf(lastServoMovedAt));
                                if(3 < (System.currentTimeMillis()/1000L) - lastServoMovedAt){
                                    arduino.servoWrite(9, rand.nextInt(180));
                                }
                            }
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            });

        try{
            arduino.connect();
            Log.v(TAG, "Board Version : "+arduino.getBoardVersion());
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
