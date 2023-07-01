package com.example.tutorial6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StrengthTraining extends AppCompatActivity {

    private TextView trainingTime;
    private TextView overAllPunches;
    private TextView weakPunches;
    private TextView mediumPunches;
    private TextView strongPunches;
    private TextView rank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int[] message = intent.getIntArrayExtra("KEY_MESSAGE");
        String timeTrained = intent.getStringExtra("TIME_TRAINED");
        setContentView(R.layout.activity_strength_training);
        trainingTime = findViewById(R.id.trainingTime);
        overAllPunches = findViewById(R.id.overAllPunches);
        weakPunches = findViewById(R.id.weakPunches);
        mediumPunches = findViewById(R.id.mediumPunches);
        strongPunches = findViewById(R.id.strongPunches);
        rank = findViewById(R.id.rank);

        trainingTime.setText("Session Time: " + timeTrained + " Seconds");

        int overall = 0;
        for (int num : message) {
            overall = overall + num;
        }
        overAllPunches.setText("Overall Punches Thrown: " + Integer.toString(overall));
        weakPunches.setText("Weak Punches: " + Integer.toString(message[0]));
        mediumPunches.setText("Medium Punches: " + Integer.toString(message[1]));
        strongPunches.setText("Strong Punches: " + Integer.toString(message[2]));

        double rankScore = (double)message[2]/overall;
        if(rankScore >= 0.7){
            rank.setText("Your Rank: Pro");
        } else if (rankScore >= 0.5) {
            rank.setText("Your Rank: Intermediate");
        }else {
            rank.setText("Your Rank: Beginner");
        }


    }

}