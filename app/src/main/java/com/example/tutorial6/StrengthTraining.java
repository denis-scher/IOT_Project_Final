package com.example.tutorial6;

import androidx.appcompat.app.AppCompatActivity;
import android.os.CountDownTimer;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StrengthTraining extends AppCompatActivity {

    private TextView timeTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String message = intent.getStringExtra("KEY_MESSAGE");
        setContentView(R.layout.activity_strength_training);
        timeTextView = findViewById(R.id.power_training_text);
        int time = Integer.parseInt(message);

        // 30000 is the starting time (in milliseconds), 1000 is the interval (also in milliseconds)

        new CountDownTimer(3000, 1000) { // 3000 milli seconds is 3 seconds.

            public void onTick(long millisUntilFinished) {
                // You can display the time left here every second.
                timeTextView.setText("Get Ready: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                // Start your main timer after 3 seconds have passed.
                new CountDownTimer(time *1000, 1000) { // This timer is just an example, adjust as needed.

                    public void onTick(long millisUntilFinished) {
                        timeTextView.setText("Time remaining: " + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        timeTextView.setText("Done!");
                    }
                }.start();
            }
        }.start();
//        CountDownTimer countDownTimer = new CountDownTimer(time *1000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                // this method is called every interval you've set (in this case every second)
//                // you can update a TextView or a ProgressBar here, for example
//                String timeRemaining = "Time remaining: " + millisUntilFinished / 1000;
//                timeTextView.setText("Time remaining: " + millisUntilFinished / 1000);  // replace with code to update UI
//            }
//
//            public void onFinish() {
//                // this method is called when the timer has finished
//                timeTextView.setText("Done!");  // replace with your code to handle timer finished
//            }
//        }.start();

    }
}