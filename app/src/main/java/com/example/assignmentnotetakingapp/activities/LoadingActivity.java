package com.example.assignmentnotetakingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignmentnotetakingapp.R;

public class LoadingActivity extends AppCompatActivity {
    private static final int SPLASH_TIME_OUT = 10000;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        progressBar = findViewById(R.id.progressBar);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int progress = progressBar.getProgress() + 10;
                progressBar.setProgress(progress);
                if (progress>=100){
                    Intent intent;
                    intent = new Intent(LoadingActivity.this,
                            LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    handler.postDelayed(this,200);
                }
            }
        }, SPLASH_TIME_OUT);
    }
}
