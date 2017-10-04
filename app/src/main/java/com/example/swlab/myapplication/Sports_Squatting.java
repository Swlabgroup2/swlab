package com.example.swlab.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Sports_Squatting extends AppCompatActivity implements SensorEventListener{
    private EditText edt_cal;
    private EditText edt_count;
    private EditText edt_time;
    private SimpleDateFormat dtFormat;
    private String nowTime;
    private Date date;
    private TextView timer;
    private Button finish;
    private int sec =0;
    private int min=0;
    private CountDownTimer countdownTimer;
    private Boolean isTimer=false;
    private String cal;
    private String count;
    private String sportTime;
    private FirebaseAuth auth;
    private Dialog customDialog;
    private Button confirm;
    private TextView title;
    private TextView message;
    private EditText input;


    private SensorManager sensorManager;
    private Boolean running=false;
    private Boolean isSensor =false;
    private String sensorCount;

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(Sports_Squatting.this, Sports_Content.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sports_squatting);
        processView();
        processControl();
    }


    private void processView() {
        edt_cal = (EditText) findViewById(R.id.txtCal);
        edt_count = (EditText) findViewById(R.id.txtCount);
        edt_time = (EditText) findViewById(R.id.txtTime);
        timer=(TextView)findViewById(R.id.txt_timer);
        finish = (Button) findViewById(R.id.btn_start);
        auth = FirebaseAuth.getInstance();
        timer = (TextView)findViewById(R.id.txt_timer);
        dtFormat = new SimpleDateFormat("yyyy/MM/dd");
        date = new Date();
        nowTime = dtFormat.format(date);
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    private void processControl() {
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTimer=false;
                timerStop();
            }
        });

        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimer) {
                    countdownTimer.cancel();
                    isTimer=false;
                }
                else if (min==0&&sec==0){
                    timerStart();
                    isTimer=true;
                }
                else{
                    countdownTimer.start();
                    isTimer=true;
                }
            }
        });
    }


    private void timerStart() {
        finish.setVisibility(View.VISIBLE);
        countdownTimer=new CountDownTimer(1000000000000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sec++;
                if(sec==60) {
                    min++;
                    sec=0;
                }
                if(min<10&&sec>10)
                    timer.setText("0"+min+":"+sec);
                else if(min>10&&sec<10)
                    timer.setText(min+":0"+sec);
                else if(min<10&sec<10)
                    timer.setText("0"+min+":0"+sec);
                else
                    timer.setText(min+":"+sec);
                finish.setText("結束運動");
            }

            @Override
            public void onFinish() {

            }
        };
        countdownTimer.start();
    }

    private void timerStop() {
        edt_time.setText(timer.getText().toString().trim());
        edt_cal.setText(((min*60+sec)*0.019)+"");
        timer.setText("00:00");
        min=0;
        sec=0;
        countdownTimer.cancel();
        finishDialog();
    }

    private void finishDialog() {
        if(!isSensor){
            customDialog=new Dialog(Sports_Squatting.this,R.style.DialogCustom);
            customDialog.setContentView(R.layout.custom_dialog_text);
            customDialog.setCancelable(false);
            confirm=(Button)customDialog.findViewById(R.id.confirm);
            confirm.setText("確認");
            title=(TextView)customDialog.findViewById(R.id.title);
            title.setText("結束運動");
            message=(TextView)customDialog.findViewById(R.id.message);
            message.setText("請輸入今天做了幾下運動吧~");
            input=(EditText)customDialog.findViewById(R.id.editText);

            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(input.getText().toString().trim().equals("")) {
                        Toast.makeText(Sports_Squatting.this, "請輸入數字~", Toast.LENGTH_LONG).show();
                    }
                    else{
                        count = input.getText().toString().trim();
                        edt_count.setText("");
                        cal=edt_cal.getText().toString().trim();
                        edt_cal.setText("");
                        sportTime=edt_time.getText().toString().trim();
                        edt_time.setText("");
                        insertData(nowTime, cal, count, sportTime);
                        finish.setVisibility(View.INVISIBLE);
                        customDialog.dismiss();
                        Toast.makeText(Sports_Squatting.this, "紀錄已儲存", Toast.LENGTH_LONG).show();
                    }

                }
            });
            customDialog.show();
        }
        else{
            customDialog=new Dialog(Sports_Squatting.this,R.style.DialogCustom);
            customDialog.setContentView(R.layout.custom_dialog_one);
            customDialog.setCancelable(false);
            confirm=(Button)customDialog.findViewById(R.id.confirm);
            confirm.setText("確認");
            title=(TextView)customDialog.findViewById(R.id.title);
            title.setText("結束運動");
            message=(TextView)customDialog.findViewById(R.id.message);
            message.setText("按下確認以儲存紀錄");
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count=edt_count.getText().toString().trim();
                    edt_count.setText("");
                    cal=edt_cal.getText().toString().trim();
                    edt_cal.setText("");
                    sportTime=edt_time.getText().toString().trim();
                    edt_time.setText("");
                    insertData(nowTime,cal, count,sportTime);
                    finish.setVisibility(View.INVISIBLE);
                    customDialog.dismiss();
                    Toast.makeText(Sports_Squatting.this, "紀錄已儲存",Toast.LENGTH_LONG).show();
                }
            });
            customDialog.show();
        }
    }

    private void insertData(String sportDate, String Cal, String Distance, String sportTime){
        Firebase myFirebaseRef = new Firebase("https://swlabapp.firebaseio.com/user");
        Firebase userRef = myFirebaseRef.child("sport").child("squatting").child(auth.getCurrentUser().getUid().trim());
        DB_Sports_Others data = new DB_Sports_Others(sportDate,Cal,Distance,sportTime);
        userRef.push().setValue(data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        running=true;
        Sensor countSensor=sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(countSensor!=null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
            isSensor =true;
            Toast.makeText(this,"Sensor Founded!",Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this,"Sensor Not Found!",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        running=false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(running)
        {
            sensorCount=String.valueOf(event.values[0]);
            edt_count.setText(sensorCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}