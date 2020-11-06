package com.example.JihadIntel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class NewsBriefTemplate extends AppCompatActivity {

    CardView news_template;
    TextView news_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_brief_template);

        /*
        news_template = findViewById(R.id.news_template);
        news_id = findViewById(R.id.news_id);

        news_template.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String news_id_string = news_id.getText().toString();
                Intent show_full_news = new Intent(NewsBriefTemplate.this,NewsActivity.class);
                show_full_news.putExtra("news_id",news_id_string);
                startActivity(show_full_news);
            }
        });
         */
    }
}