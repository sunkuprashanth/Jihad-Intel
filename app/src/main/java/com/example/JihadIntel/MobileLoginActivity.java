package com.example.JihadIntel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class MobileLoginActivity extends AppCompatActivity {

    private static final String TAG = "MobileLoginActivity";
    EditText mobile_num, otp;
    Button login, get_otp;
    String mobile_str, otp_str, verificationId;
    PhoneAuthProvider.ForceResendingToken resend_token;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    TextView resend_code;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_login);

        mobile_num = findViewById(R.id.mobile_num);
        otp = findViewById(R.id.otp);
        login = findViewById(R.id.login);
        get_otp = findViewById(R.id.get_otp);
        resend_code = findViewById(R.id.resend_code);
        sharedPreferences = getSharedPreferences(MainActivity.prefs_file_login, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    otp_str = otp.getText().toString();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp_str);
                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(MobileLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = task.getResult().getUser();
                                    Log.d(TAG, "onComplete: " + user.getUid());
                                    editor.putBoolean("logged_in",true);
                                    editor.putBoolean("FirstRun",false);
                                    editor.commit();

                                    DocumentReference doc_reference = firebaseFirestore.collection("users").document(mAuth.getCurrentUser().getUid());
                                    doc_reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot snapshot = task.getResult();
                                                if (snapshot.exists()) {
                                                    Log.d(TAG, "onSuccess: "+ snapshot.get("name"));
                                                    GeneralMethods.postLoginCalls(MobileLoginActivity.this);
                                                    if (getIntent().getStringExtra("CalledActivity").equals("NewsActivity")) {
                                                        setResult(GeneralMethods.RESULT_SIGN_IN);
                                                        onBackPressed();
                                                    } else {
                                                        Intent showing_news = new Intent(getApplicationContext(),MainActivity.class);
                                                        showing_news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(showing_news);
                                                    }
                                                } else {
                                                    Intent get_user_details = new Intent(MobileLoginActivity.this, GetUserDetailsActivity.class);
                                                    get_user_details.putExtra("CalledActivity", getIntent().getStringExtra("CalledActivity"));
                                                    get_user_details.putExtra("account_type", "Mobile");
                                                    startActivityForResult(get_user_details, 1);
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    // Sign in failed, display a message and update the UI
                                    Log.d(TAG, "signInWithCredential:failure", task.getException());
                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        // The verification code entered was invalid
                                    }
                                }
                            }
                        });
            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                String code = credential.getSmsCode();


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.d(TAG, "onVerificationFailed: " + e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                resend_token = forceResendingToken;
                Toast.makeText(MobileLoginActivity.this,"OTP sent", Toast.LENGTH_LONG).show();

            }
        };

        resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(mobile_str, 60, TimeUnit.SECONDS, MobileLoginActivity.this, mCallbacks, resend_token);
            }
        });

        get_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobile_str = "+91" + mobile_num.getText().toString();
                if (mobile_str.length()==13) {
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(mobile_str, 60, TimeUnit.SECONDS, MobileLoginActivity.this, mCallbacks);
                    mobile_num.setEnabled(false);
                    otp.setVisibility(View.VISIBLE);
                    login.setVisibility(View.VISIBLE);
                    resend_code.setVisibility(View.VISIBLE);
                    get_otp.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (getIntent().getStringExtra("CalledActivity").equals("NewsActivity")) {
            setResult(GeneralMethods.RESULT_SIGN_IN);
            onBackPressed();
        } else {
            Intent showing_news = new Intent(getApplicationContext(),MainActivity.class);
            showing_news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(showing_news);
        }
    }
}