package com.example.JihadIntel;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import static com.example.JihadIntel.MainActivity.prefs_file_login;

public class NewsActivity extends AppCompatActivity {

    String news_id_string;
    String TAG = "NewsActivity";
    SharedPreferences sharedPreferences;
    ImageView news_image;
    TextView news_headline, news_content, news_timeline;
    LinearLayout to_login;
    Boolean isLogged;
    Button login;
    int lines_restrict = 4;
    ImageView profile, log_out, back_btn;
    Button log_in;
    private int ProfileActivity_REQ = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        news_image = findViewById(R.id.news_img);
        news_timeline = findViewById(R.id.news_timeline);
        news_headline = findViewById(R.id.news_headline);
        news_content = findViewById(R.id.news_content);
        to_login = findViewById(R.id.to_login);
        login = findViewById(R.id.login);

        Intent get_id = getIntent();
        news_id_string = get_id.getStringExtra("news_id");

        sharedPreferences = getSharedPreferences(prefs_file_login,MODE_PRIVATE);

        toolBarActions();
        show_content();
    }

    public void show_content() {
        Log.d(TAG, "show_content: " + news_id_string);
        NewsArticle ns = GlobalData.articles.get(Integer.parseInt(news_id_string));

        Log.d(TAG, "show_content: " + isLogged);
        Picasso.get().load(ns.getImage_url()).into(news_image);

        news_headline.setText(ns.getTitle());
        news_timeline.setText(ns.getTimestamp().toDate().toString());
        news_content.setText(ns.getDesc());
        isLogged = sharedPreferences.getBoolean("logged_in",false);

        if ( !isLogged ) {
            news_content.setMaxLines(lines_restrict);
            to_login.setVisibility(View.VISIBLE);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent goto_login = new Intent(NewsActivity.this, LoginActivity.class);
                    goto_login.putExtra("CalledActivity", "NewsActivity");
                    startActivityForResult(goto_login,1);
                }
            });
        } else {
            news_content.setMaxLines(Integer.MAX_VALUE);
            to_login.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //isLogged = sharedPreferences.getBoolean("logged_in",false);
        if (resultCode == GeneralMethods.RESULT_SIGN_IN) {
            news_content.setMaxLines(Integer.MAX_VALUE);
            to_login.setVisibility(View.GONE);
            toolBarActions();
            setResult(GeneralMethods.RESULT_SIGN_IN);

        } else if (resultCode == GeneralMethods.RESULT_SIGN_OUT) {
            profile = findViewById(R.id.profilePic);
            profile.setImageResource(R.mipmap.profile_pic);
            profile.setEnabled(false);
            profile.setVisibility(View.GONE);
            log_in.setVisibility(View.VISIBLE);
            setResult(GeneralMethods.RESULT_SIGN_OUT);
            show_content();
        }
    }

    public void toolBarActions () {
        profile = findViewById(R.id.profilePic);
        back_btn = findViewById(R.id.back_btn);
        log_in = findViewById(R.id.log_in);

        profile.setVisibility(View.GONE);

        profile.setEnabled(false);
        if (GlobalData.userData.getId() != null) {
            if (GlobalData.userData.getPhoto_url() == null || GlobalData.userData.getPhoto_url().toString().equals(""))
                profile.setImageResource(R.mipmap.profile_pic);
            else
                Picasso.get().load(GlobalData.userData.getPhoto_url()).into(profile);
            profile.setEnabled(true);
            profile.setVisibility(View.VISIBLE);
            log_in.setVisibility(View.GONE);
        }

        log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goto_login = new Intent(NewsActivity.this, LoginActivity.class);
                goto_login.putExtra("CalledActivity", "NewsActivity");
                startActivityForResult(goto_login,1);
            }
        });
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile_page = new Intent(NewsActivity.this,ProfileActivity.class);
                startActivityForResult(profile_page,ProfileActivity_REQ);
            }
        });
    }
}