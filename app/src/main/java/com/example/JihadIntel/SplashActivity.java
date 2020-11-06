package com.example.JihadIntel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;


public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 2000);

        sharedPreferences = getSharedPreferences(MainActivity.prefs_file_login, MODE_PRIVATE);
        if(sharedPreferences.getBoolean("logged_in",false)) {
            GeneralMethods.postLoginCalls(this);
        }
    }
}