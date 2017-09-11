/*
 * Copyright (C) 2017 MaBaoGW
 *
 * Source Link: https://github.com/MaBaoGW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.mabaogw.ledbarsample;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.onegravity.colorpicker.ColorPickerDialog;
import com.onegravity.colorpicker.ColorPickerListener;
import com.onegravity.colorpicker.SetColorPickerListenerEvent;

import java.util.ArrayList;
import java.util.List;

public class UploadStringActivity extends AbstractBleControlActivity implements ColorPickerListener {
    private final static String TAG = UploadStringActivity.class.getSimpleName();
    private Context context;

    private List<String> ledColors = new ArrayList<String>();
    private String ledColorsStr;

    private Button buttonClear,buttonSend,buttonColor;

    private int mDialogId = -1;
    private EditText all_led_num;
    private EditText my_led_position;
    private int led_num1,led_num2,led_num3,led_num4;
    private CheckBox check_single,check_star,check_rainbow;
    private Button button_single_color,button_meteor_color;
    private String defaultColor = "#ff000000";
    private String myColor = "#ffff0000";
    private Button buttonPreview,buttonSaveToLed;

    private int prev_check_index=1;
    private SeekBar time_seekBar;
    private int time_value = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload_string);
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        activity = this;
        context = this;

        led_num1 = 0;
        led_num2 = 0;
        led_num3 = 0;
        led_num4 = 0;

        final Intent intent = getIntent();
        currDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        currDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(currDeviceName);

        all_led_num = (EditText) findViewById(R.id.all_led_num);
        my_led_position = (EditText) findViewById(R.id.my_led_position);

        buttonClear = (Button) findViewById(R.id.button_clear);
        buttonClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "buttonClear onClick");

                if(mConnected) {
                    UploadAsyncTask2 asyncTask = new UploadAsyncTask2();
                    if(!all_led_num.getText().toString().equalsIgnoreCase("")) {
                        int led_num = Integer.valueOf(all_led_num.getText().toString());
                        Log.d(TAG, "led_num = " + led_num);
                        if (led_num > 0) {
                            if (led_num > 255 && led_num < 255 * 2) {
                                led_num1 = 255;
                                led_num2 = led_num - 255;
                                led_num3 = 0;
                                led_num4 = 0;
                            } else if (led_num > 255 * 2 && led_num < 255 * 3) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = led_num - 255;
                                led_num4 = 0;
                            } else if (led_num > 255 * 3 && led_num < 255 * 4) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = 255;
                                led_num4 = led_num - 255;
                            } else if (led_num > 255 * 4) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = 255;
                                led_num4 = 255;
                            } else {
                                led_num1 = led_num;
                                led_num2 = 0;
                                led_num3 = 0;
                                led_num4 = 0;
                            }
                            asyncTask.execute(0xa1, 0x1a, led_num1, led_num2, led_num3, led_num4);
                        }
                    }
                }
                else{
                    Toast.makeText(context, getResources().getString(R.string.Not_Connected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonColor = (Button) findViewById(R.id.buttonColor);
        buttonColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "buttonColor onClick");
                if (mDialogId == -1) {
                    mDialogId = new ColorPickerDialog(context, Color.parseColor(myColor), false).show();
                    SetColorPickerListenerEvent.setListener(mDialogId, UploadStringActivity.this);
                }

            }
        });

        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "buttonSend onClick");

                if(mConnected) {

                    String temp_color="";
                    int effect_type=1;
                    temp_color = myColor;

                    String send_color = temp_color.replace("#ff","") + "00";
                    msgBuffer = new StringBuilder("");
                    msgBuffer.append(send_color);

                    Upload1AsyncTask1 asyncTask = new Upload1AsyncTask1();

                    if(!all_led_num.getText().toString().equalsIgnoreCase("")) {
                        int led_num = Integer.valueOf(all_led_num.getText().toString());
                        //Log.d(TAG, "led_num = " + led_num + " time_value = " + time_value);
                        if (Integer.valueOf(all_led_num.getText().toString()) > 0) {
                            if (led_num > 255 && led_num < 255 * 2) {
                                led_num1 = 255;
                                led_num2 = led_num - 255;
                                led_num3 = 0;
                                led_num4 = 0;
                            } else if (led_num > 255 * 2 && led_num < 255 * 3) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = led_num - 255;
                                led_num4 = 0;
                            } else if (led_num > 255 * 3 && led_num < 255 * 4) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = 255;
                                led_num4 = led_num - 255;
                            } else if (led_num > 255 * 4) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = 255;
                                led_num4 = 255;
                            } else {
                                led_num1 = led_num;
                                led_num2 = 0;
                                led_num3 = 0;
                                led_num4 = 0;
                            }
                            if(!my_led_position.getText().toString().equalsIgnoreCase("")) {
                                int position = Integer.valueOf(my_led_position.getText().toString());
                                if(position > 0) {
                                    asyncTask.execute(led_num1, led_num2, led_num3, led_num4, effect_type, time_value, position-1);//led start from 0
                                }
                                else{
                                    Log.d(TAG, "led position<=0");
                                }
                            }
                            else{
                                Log.d(TAG, "no led position");
                            }
                        }
                        else{
                            Log.d(TAG, "led num<=0");
                        }
                    }
                    else{
                        Log.d(TAG, "no led num");
                    }
                }
                else{
                    Toast.makeText(context, getResources().getString(R.string.Not_Connected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        check_single = (CheckBox) findViewById(R.id.checkBox_single);
        check_star = (CheckBox) findViewById(R.id.checkBox_star);
        check_rainbow = (CheckBox) findViewById(R.id.checkBox_rainbow);
        check_single.setChecked(true);//default

        check_single.setOnCheckedChangeListener(checklistener1);
        check_star.setOnCheckedChangeListener(checklistener4);
        check_rainbow.setOnCheckedChangeListener(checklistener5);

        button_single_color = (Button) findViewById(R.id.button_single_color);
        button_meteor_color = (Button) findViewById(R.id.button_meteor_color);

        button_single_color.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "button_snake_color onClick");
                if (mDialogId == -1) {
                    mDialogId = new ColorPickerDialog(context, Color.parseColor(myColor), false).show();
                    SetColorPickerListenerEvent.setListener(mDialogId, UploadStringActivity.this);
                }

            }
        });

        button_meteor_color.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "button_meteor_color onClick");
                if (mDialogId == -1) {
                    mDialogId = new ColorPickerDialog(context, Color.parseColor(myColor), false).show();
                    SetColorPickerListenerEvent.setListener(mDialogId, UploadStringActivity.this);
                }

            }
        });

        buttonPreview = (Button) findViewById(R.id.previewButton);
        buttonSaveToLed = (Button) findViewById(R.id.saveLEDButton);

        buttonPreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "button_send onClick");

                if(mConnected) {

                    String temp_color="";
                    int effect_type=0;

                    if(check_single.isChecked()) {
                        temp_color = myColor;
                        effect_type = 2;
                    }
                    else if(check_star.isChecked()){
                        temp_color=myColor;
                        effect_type=3;
                    }
                    else if(check_rainbow.isChecked()){
                        temp_color=defaultColor;
                        effect_type=4;
                    }

                    String send_color = temp_color.replace("#ff","") + "00";
                    msgBuffer = new StringBuilder("");
                    msgBuffer.append(send_color);

                    Upload1AsyncTask1 asyncTask = new Upload1AsyncTask1();

                    if(!all_led_num.getText().toString().equalsIgnoreCase("")) {
                        int led_num = Integer.valueOf(all_led_num.getText().toString());
                        //Log.d(TAG, "led_num = " + led_num + " time_value = " + time_value);
                        if (led_num > 0) {
                            if (led_num > 255 && led_num < 255 * 2) {
                                led_num1 = 255;
                                led_num2 = led_num - 255;
                                led_num3 = 0;
                                led_num4 = 0;
                            } else if (led_num > 255 * 2 && led_num < 255 * 3) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = led_num - 255;
                                led_num4 = 0;
                            } else if (led_num > 255 * 3 && led_num < 255 * 4) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = 255;
                                led_num4 = led_num - 255;
                            } else if (led_num > 255 * 4) {
                                led_num1 = 255;
                                led_num2 = 255;
                                led_num3 = 255;
                                led_num4 = 255;
                            } else {
                                led_num1 = led_num;
                                led_num2 = 0;
                                led_num3 = 0;
                                led_num4 = 0;
                            }
                            asyncTask.execute(led_num1, led_num2, led_num3, led_num4, effect_type, time_value, 0);
                        }
                    }

                }
                else{
                    Toast.makeText(context, getResources().getString(R.string.Not_Connected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSaveToLed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "button_save onClick");

                if(mConnected) {
                    Save2LEDAsyncTask asyncTask = new Save2LEDAsyncTask();
                    asyncTask.execute();
                }
                else{
                    Toast.makeText(context, getResources().getString(R.string.Not_Connected), Toast.LENGTH_SHORT).show();
                }
            }
        });

        time_seekBar = (SeekBar) findViewById(R.id.time_seekBar);
        time_seekBar.setEnabled(false);
        time_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                time_value = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nothing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private CheckBox.OnCheckedChangeListener checklistener1 = new CheckBox.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            Log.d(TAG, "buttonView.getId() " + buttonView.getId());
            switch(buttonView.getId()){
                case R.id.checkBox_single:
                    Log.d(TAG, "checkBox_single checked " + check_single.isChecked() + " " + prev_check_index);

                    if(check_single.isChecked()){
                        if(prev_check_index==1){
                            check_single.setChecked(false);
                        }
                        else{
                            prev_check_index = 1;
                            check_single.setChecked(true);
                            check_star.setChecked(false);
                            check_rainbow.setChecked(false);
                            time_seekBar.setEnabled(false);
                        }
                    }
                    else{//重複按
                        if(prev_check_index==1) {
                            prev_check_index = 1;
                            check_single.setChecked(true);
                        }
                        else{
                            check_single.setChecked(false);
                        }
                    }
                    break;
            }
        }
    };

    private CheckBox.OnCheckedChangeListener checklistener4 = new CheckBox.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            Log.d(TAG, "buttonView.getId() " + buttonView.getId());
            switch(buttonView.getId()){
                case R.id.checkBox_star:
                    Log.d(TAG, "checkBox_star checked");

                    if(check_star.isChecked()){
                        if(prev_check_index==2){
                            check_star.setChecked(false);
                        }
                        else{
                            prev_check_index = 2;
                            check_single.setChecked(false);
                            check_star.setChecked(true);
                            check_rainbow.setChecked(false);
                            time_seekBar.setEnabled(true);
                        }
                    }
                    else{//重複按
                        if(prev_check_index==2) {
                            prev_check_index = 2;
                            check_star.setChecked(true);
                        }
                        else{
                            check_star.setChecked(false);
                        }
                    }
                    break;
            }
        }
    };

    private CheckBox.OnCheckedChangeListener checklistener5 = new CheckBox.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
            Log.d(TAG, "buttonView.getId() " + buttonView.getId());
            switch(buttonView.getId()){
                case R.id.checkBox_rainbow:
                    Log.d(TAG, "checkBox_rainbow checked");

                    if(check_rainbow.isChecked()){
                        if(prev_check_index==3){
                            check_rainbow.setChecked(false);
                        }
                        else{
                            prev_check_index = 3;
                            check_single.setChecked(false);
                            check_star.setChecked(false);
                            check_rainbow.setChecked(true);
                            time_seekBar.setEnabled(false);
                        }
                    }
                    else{//重複按
                        if(prev_check_index==3) {
                            prev_check_index = 3;
                            check_rainbow.setChecked(true);
                        }
                        else{
                            check_rainbow.setChecked(false);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onColorChanged(int color) {
        myColor = "#" + Integer.toHexString(color);
        Log.d(TAG, "myColor " + myColor);
    }

    @Override
    public void onDialogClosing(boolean color_changed) {
        Log.d(TAG, "onDialogClosing color_changed = " + color_changed);
        mDialogId = -1;
    }

}