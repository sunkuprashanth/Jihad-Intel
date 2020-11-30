package com.example.JihadIntel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import io.supercharge.shimmerlayout.ShimmerLayout;

import static com.example.JihadIntel.GeneralMethods.limit_articles;
import static com.example.JihadIntel.GeneralMethods.postLoginCalls;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RETURN_SPLASH_SCREEN = 1011;
    int initial = 0, template_height = 0;
    Timestamp last_time = null;
    LinearLayout newsLayout, profile_pic_layout;
    static final String prefs_file_login = "login_details";
    ProgressDialog dialog;
    CircleImageView profile;
    static SharedPreferences sharedPreferences;
    ImageView log_out, back_btn;
    Button log_in;
    ScrollView sv;
    ShimmerLayout ghost_news_layout;
    private int NewsActivity_REQ = 1,ProfileActivity_REQ = 2,LoginActivity_REQ = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(prefs_file_login, MODE_PRIVATE);
        if(sharedPreferences.getBoolean("logged_in",false)) {
            GeneralMethods.postLoginCalls(this);
            toolBarActions();
        }
        if (sharedPreferences.getBoolean("FirstRun",true)) {
            GeneralMethods.start=1;
            Intent login_act = new Intent(MainActivity.this, LoginActivity.class);
            login_act.putExtra("CalledActivity", "StartActivity");
            startActivityForResult(login_act, LoginActivity_REQ);
        }

        newsLayout = findViewById(R.id.news_layout);
        ghost_news_layout = findViewById(R.id.ghost_news_layout);
        ghost_news_layout.startShimmerAnimation();

        sv = findViewById(R.id.sv);
        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                if ((sv.getChildAt(0).getBottom() - template_height*2) <= (sv.getHeight() + sv.getScrollY())) {
                    Log.d(TAG, "onScrollChanged: Reached bottom" + sv.getChildAt(0).getBottom() +" " + sv.getScrollY());
                    if (initial != GlobalData.articles.size()-1) {
                        initial = GlobalData.articles.size() - 1;
                        getAllArticles();
                    }
                }
                else {
                    Log.d(TAG, "onScrollChanged: Not bottom" + template_height);
                }
            }
        });

        //Log.d(TAG, "onCreate: " + GlobalData.articles.size());
        if(GlobalData.articles.size()==0)
            getAllArticles();
        else
            show_temps();

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 200);

        if(sharedPreferences.getBoolean("logged_in",false)) {
            postLoginCalls(this);
        }
        toolBarActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolBarActions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("FirstRun",false);
        editor.commit();

        if(resultCode == GeneralMethods.RESULT_SIGN_IN) {
            toolBarActions();
        } else if (resultCode == GeneralMethods.RESULT_SIGN_OUT) {
            profile = findViewById(R.id.profilePic);
            profile.setImageResource(R.mipmap.profile_pic);
            profile.setEnabled(false);
            profile.setVisibility(View.GONE);
            log_in.setVisibility(View.VISIBLE);
        }
    }

    public void show_temps() {
        if (initial == 0)
            initial = -1;
        for (int i=initial + 1 ; i < GlobalData.articles.size(); i++) {

            NewsArticle ns = GlobalData.articles.get(i);
            View nb = getLayoutInflater().inflate(R.layout.activity_news_brief_template,null,false);

            CardView news_template = nb.findViewById(R.id.news_template);
            final TextView news_id = nb.findViewById(R.id.news_id);
            TextView news_headline = nb.findViewById(R.id.news_headline);
            TextView news_brief = nb.findViewById(R.id.news_brief);
            ImageView news_image = nb.findViewById(R.id.news_image);

            Picasso.get().load(ns.getImage_url()).into(news_image);
            news_id.setText(""+i);
            news_headline.setText(ns.getTitle());
            news_brief.setText(ns.getDesc());

            news_template.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String news_id_string = news_id.getText().toString();
                    Intent show_full_news = new Intent(MainActivity.this,NewsActivity.class);
                    show_full_news.putExtra("news_id",news_id_string);
                    startActivityForResult(show_full_news,NewsActivity_REQ);
                }
            });
            newsLayout.addView(nb);
            if (template_height==0) {
                template_height = newsLayout.getChildAt(0).getHeight();
            }

            ghost_news_layout.stopShimmerAnimation();
            ghost_news_layout.setVisibility(View.GONE);

        }
    }

    public int getAllArticles() {
        //Log.d(TAG, "getAllArticles: ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query qs = db.collection("article").orderBy("date_time",Query.Direction.DESCENDING);
        if (last_time==null)
            last_time = GeneralMethods.last_time;
        if (last_time!=null)
            qs = qs.startAfter(last_time);
        qs.limit(limit_articles)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //Log.d(TAG, "onComplete: " + task.getResult().size());
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Map<String,Object> article = document.getData();
                                //Log.d(TAG, "onComplete: " + document.getId());

                                NewsArticle ns = new NewsArticle();
                                ns.setId(document.getId());
                                ns.setTitle((String) article.get("title"));
                                ns.setDesc((String) article.get("art_desc"));
                                ns.setImage_url((String) article.get("img_url"));
                                ns.setTimestamp((Timestamp) article.get("date_time"));

                                last_time = ns.getTimestamp();
                                GlobalData.articles.add(ns);
                                //Log.d(TAG, "onComplete: " + ns.toString());
                            }

                            show_temps();
                        } else {
                            Log.d(TAG, "GeneralMethods onComplete Error getting documents.", task.getException());
                        }
                    }
                });
        return 1;
    }

    public void toolBarActions () {
        profile = findViewById(R.id.profilePic);
        back_btn = findViewById(R.id.back_btn);
        log_in = findViewById(R.id.log_in);
        profile_pic_layout = findViewById(R.id.profile_pic_layout);

        back_btn.setVisibility(View.GONE);
        profile.setVisibility(View.GONE);

        profile_pic_layout.setPadding((int) getResources().getDimension(R.dimen.toolbar_padding),0,0,0);

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
                Intent goto_login = new Intent(MainActivity.this, LoginActivity.class);
                goto_login.putExtra("CalledActivity", "MainActivity");
                startActivityForResult(goto_login,1);
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile_page = new Intent(MainActivity.this,ProfileActivity.class);
                startActivityForResult(profile_page,ProfileActivity_REQ);
            }
        });
    }

}