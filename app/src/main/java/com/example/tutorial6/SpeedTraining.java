package com.example.tutorial6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

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
        ArrayList<ArrayList<Float>> accData = (ArrayList<ArrayList<Float>>)intent.getSerializableExtra("ACC_DATA");
        int time = Integer.parseInt(timeTrained);
        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        float maxVelocity = maxVelocityCalculator(accData);
        maxSpeed.setText("Maximum Velocity: " + (decimalFormat.format(maxVelocity)) + " m/s");

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

    private float maxVelocityCalculator(ArrayList<ArrayList<Float>> data){
        ArrayList<Float> velX = new ArrayList<Float>();
        ArrayList<Float> velY = new ArrayList<Float>();
        ArrayList<Float> velZ = new ArrayList<Float>();
        ArrayList<Float> velNorm = new ArrayList<Float>();
        float currentVelX = 0;
        float currentVelY = 0;
        float currentVelZ = 0;
        for(int i = 0; i<data.get(0).size();i++){
            currentVelX += data.get(0).get(i) * 1/10;
            velX.add(currentVelX);
            currentVelY += data.get(1).get(i) * 1/10;
            velY.add(currentVelY);
            currentVelZ += data.get(2).get(i) * 1/10;
            velZ.add(currentVelZ);
            float currentNorm = (float)Math.sqrt((currentVelX * currentVelX + currentVelY * currentVelY + currentVelZ * currentVelZ));
            velNorm.add(currentNorm);
        }
        float max = Collections.max(velNorm)/15;
        return max;
    }
}