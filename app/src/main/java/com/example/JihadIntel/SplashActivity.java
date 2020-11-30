package com.example.JihadIntel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.JihadIntel.MainActivity.prefs_file_login;


public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        GlobalData.articles = new ArrayList<NewsArticle>();
        GeneralMethods.getAllArticles();

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }, 2000);

    }
}