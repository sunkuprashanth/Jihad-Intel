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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.JihadIntel.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.JihadIntel.GeneralMethods.postLoginCalls;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int RETURN_SPLASH_SCREEN = 1011;
    LinearLayout newsLayout, profile_pic_layout;
    static final String prefs_file_login = "login_details";
    ProgressDialog dialog;
    CircleImageView profile;
    static SharedPreferences sharedPreferences;
    ImageView log_out, back_btn;
    Button log_in;
    private int NewsActivity_REQ = 1,ProfileActivity_REQ = 2,LoginActivity_REQ = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(prefs_file_login, MODE_PRIVATE);

        if (GeneralMethods.start == 0) {
            Intent intent = new Intent(MainActivity.this, SplashActivity.class);
            startActivityForResult(intent,1011);
            GeneralMethods.start = 1;
        }
        setContentView(R.layout.activity_main);

    }

    public void setLayout() {
        if (sharedPreferences.getBoolean("FirstRun",true)) {
            GeneralMethods.start=1;
            Intent login_act = new Intent(MainActivity.this, LoginActivity.class);
            login_act.putExtra("CalledActivity", "StartActivity");
            startActivityForResult(login_act, LoginActivity_REQ);
        }

        newsLayout = findViewById(R.id.news_layout);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait while loading");
        dialog.show();

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RETURN_SPLASH_SCREEN) {
            setLayout();
        }

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
        for (int i=0 ; i < GlobalData.articles.size(); i++) {

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
        }
        dialog.dismiss();
    }

    public void demo() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> dp = firebaseFirestore.collection("users").document(FirebaseAuth.getInstance().getUid()).get();
        Picasso.get().load(dp.getResult().get("photo_url").toString()).into(profile);

    }


    public int getAllArticles() {
        //Log.d(TAG, "getAllArticles: ");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("article").limit(5)
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

    @Override
    protected void onStart() {
        super.onStart();
        toolBarActions();
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
        //demo();

        //log_out = findViewById(R.id.log_out);
        /*
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Log.d(TAG, "onClick: " + mAuth.getCurrentUser());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putBoolean("is_logged", false);
                editor.commit();
                GlobalData.userData = new UserLocalAccount();
                profile.setImageResource(R.mipmap.profile_pic);
            }
        });
         */
    }

    @Override
    public void onBackPressed() {
        GlobalData.articles = new ArrayList<NewsArticle>();
        super.onBackPressed();
    }
}