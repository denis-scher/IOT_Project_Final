package com.example.tutorial6;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SpeedTraining extends AppCompatActivity {

    private TextView trainingTime;
    private TextView overAllPunches;
    private TextView punchRatio;
    private TextView maxSpeed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_traning);
        trainingTime = findViewById(R.id.trainingTimeSp);
        overAllPunches = findViewById(R.id.overAllPunchesSP);
        punchRatio = findViewById(R.id.punchRatio);
        maxSpeed = findViewById(R.id.maxSpeedSP);
    }
}