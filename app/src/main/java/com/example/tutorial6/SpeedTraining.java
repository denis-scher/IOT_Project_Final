package com.example.tutorial6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class SpeedTraining extends AppCompatActivity {

    private TextView trainingTime;
    private TextView overAllPunches;
    private TextView punchRatio;
    private TextView maxSpeed;
    private TextView rankSP;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_speed_traning);
        trainingTime = findViewById(R.id.trainingTimeSp);
        overAllPunches = findViewById(R.id.overAllPunchesSP);
        punchRatio = findViewById(R.id.punchRatio);
        maxSpeed = findViewById(R.id.maxSpeedSP);
        rankSP = findViewById(R.id.rankSP);
        String timeTrained = intent.getStringExtra("TIME_TRAINED");
        int punchNum = intent.getIntExtra("NUM_OF_PUNCHES",0);
        int time = Integer.parseInt(timeTrained);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");


        trainingTime.setText("Session Time: " + timeTrained + " Seconds");
        overAllPunches.setText("Overall Punches Thrown: " + Integer.toString(punchNum));
        double ratio = (double) punchNum/time;
        ratio = Double.parseDouble(decimalFormat.format(ratio));
        punchRatio.setText("Punch Ratio: " + Double.toString(ratio) +" Punches/Sec");

        if(ratio >= 1){
            rankSP.setText("Your Rank: Pro");
        } else if (ratio >= 0.7) {
            rankSP.setText("Your Rank: Intermediate");
        }else {
            rankSP.setText("Your Rank: Beginner");
        }

    }
}