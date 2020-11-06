package com.example.JihadIntel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.JihadIntel.MainActivity.prefs_file_login;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profile_img;
    TextView name, email, mobile, gender, dob;
    ImageView back_btn;
    Button sign_out;
    SharedPreferences sharedPreferences;
    LinearLayout optional_details;
    final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: " + GlobalData.userData.getName() +" " + GlobalData.userData.getEmail_id() + " " + GlobalData.userData.getId());

        //GeneralMethods.setUserData(getSharedPreferences(prefs_file_login, MODE_PRIVATE));
        Log.d(TAG, "onCreate: " + GlobalData.userData.getName() +" " + GlobalData.userData.getEmail_id() + " " + GlobalData.userData.getId());

        profile_img = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email_id);
        mobile = findViewById(R.id.mobile);
        gender = findViewById(R.id.gender);
        dob = findViewById(R.id.dob);
        optional_details = findViewById(R.id.optional_details);
        back_btn = findViewById(R.id.back_btn);
        sign_out = findViewById(R.id.sign_out);

        sharedPreferences = getSharedPreferences(prefs_file_login, MODE_PRIVATE);

        if (GlobalData.userData.getPhoto_url() == null || GlobalData.userData.getPhoto_url().toString().equals("")) {
            Log.d(TAG, "onCreate : " + "none");
            profile_img.setImageResource(R.mipmap.profile_pic);
        }
        else {
            Log.d(TAG, "onCreate : " + GlobalData.userData.getPhoto_url().toString());
            Picasso.get().load(GlobalData.userData.getPhoto_url()).into(profile_img);
        }

        name.setText(GlobalData.userData.getName());
        email.setText(GlobalData.userData.getEmail_id());
        if (GlobalData.userData.getDob() != null) {
            dob.setText(GlobalData.userData.getDob());
            gender.setText(GlobalData.userData.getGender());
            mobile.setText(GlobalData.userData.getMobile());
        } else
            optional_details.setVisibility(View.GONE);

        Toast.makeText(this,""+GlobalData.userData.getMobile(),Toast.LENGTH_SHORT).show();

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                Log.d(TAG, "onClick: " + mAuth.getCurrentUser());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putBoolean("is_logged", false);
                editor.putBoolean("FirstRun", false);
                editor.commit();
                GlobalData.userData = new UserDetails();
                GeneralMethods.workInBackground.cancel(true);
                Log.d(TAG, "onClick: ");
                setResult(GeneralMethods.RESULT_SIGN_OUT);
                finish();
            }
        });
    }
}