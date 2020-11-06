package com.example.JihadIntel;

import androidx.annotation.NonNull;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.JihadIntel.GeneralMethods.GET_DETAILS_REQ;
import static com.example.JihadIntel.GeneralMethods.GET_DETAILS_RES;
import static com.example.JihadIntel.GeneralMethods.LOCAL_SIGN_IN_REQ;
import static com.example.JihadIntel.GeneralMethods.RESULT_SIGN_IN;
import static com.example.JihadIntel.GeneralMethods.req_code;
import static com.example.JihadIntel.MainActivity.prefs_file_login;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    EditText userId, password;
    String userId_str, password_str;
    Button login;
    TextView goto_register;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Button google_sign_in, mail_sign_in, mobile_sign_in;
    GoogleSignInOptions gso;
    GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    TextView skip_login;
    String calledActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        google_sign_in = findViewById(R.id.google_sign_in);
        skip_login = findViewById(R.id.skip_login);
        goto_register = findViewById(R.id.goto_register_link);
        mobile_sign_in = findViewById(R.id.mobile_sign_in);
        mail_sign_in = findViewById(R.id.mail_sign_in);



        final Intent i = getIntent();
        calledActivity = i.getStringExtra("CalledActivity");
        if (!calledActivity.equals("StartActivity")) {
            skip_login.setVisibility(View.GONE);
        }
        sharedPreferences = getSharedPreferences(prefs_file_login,MODE_PRIVATE);
        editor = sharedPreferences.edit();


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this,gso);

        google_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sign_in = googleSignInClient.getSignInIntent();
                startActivityForResult(sign_in,req_code);
            }
        });

        skip_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        goto_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(LoginActivity.this,"Moving to Registration Form",Toast.LENGTH_SHORT).show();
                Intent goto_register_form = new Intent(LoginActivity.this, SignupActivity.class);
                goto_register_form.putExtra("CalledActivity", calledActivity);
                startActivityForResult(goto_register_form,LOCAL_SIGN_IN_REQ);
            }
        });

        mobile_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(LoginActivity.this,"Moving to Registration Form",Toast.LENGTH_SHORT).show();
                Intent login = new Intent(LoginActivity.this, MobileLoginActivity.class);
                login.putExtra("CalledActivity", calledActivity);
                startActivityForResult(login,LOCAL_SIGN_IN_REQ);
            }
        });

        mail_sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(LoginActivity.this,"Moving to Registration Form",Toast.LENGTH_SHORT).show();
                Intent login = new Intent(LoginActivity.this, LocalLoginActivity.class);
                login.putExtra("CalledActivity", calledActivity);
                startActivityForResult(login, LOCAL_SIGN_IN_REQ);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == req_code) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                Log.d(TAG, "Google sign in failed", e);
            }
        }
        if (requestCode == LOCAL_SIGN_IN_REQ && resultCode == RESULT_SIGN_IN) {
            setResult(RESULT_SIGN_IN);
            onBackPressed();
        }
        if (requestCode == GET_DETAILS_REQ && resultCode == GET_DETAILS_RES) {
            GeneralMethods.postLoginCalls(LoginActivity.this);
            setResult(RESULT_SIGN_IN);
            onBackPressed();
        }
    }

    //Adding Users Google Account to Firebase Auth
    private void firebaseAuthWithGoogle(final String idToken) {

        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser account = mAuth.getCurrentUser();

                            Toast.makeText(LoginActivity.this, "" + account.getProviderId(),Toast.LENGTH_LONG).show();
                            editor.putBoolean("logged_in",true);
                            editor.commit();

                            DocumentReference doc_reference = firebaseFirestore.collection("users").document(mAuth.getCurrentUser().getUid());
                            doc_reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot snapshot = task.getResult();
                                        if (snapshot.exists()) {
                                            Log.d(TAG, "onSuccess: "+ snapshot.get("name"));
                                            GeneralMethods.postLoginCalls(LoginActivity.this);
                                            if (getIntent().getStringExtra("CalledActivity").equals("NewsActivity")) {
                                                setResult(GeneralMethods.RESULT_SIGN_IN);
                                                finish();
                                            } else {
                                                Intent showing_news = new Intent(getApplicationContext(),MainActivity.class);
                                                showing_news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(showing_news);
                                            }
                                        } else {
                                            Intent get_user_details = new Intent(LoginActivity.this, GetUserDetailsActivity.class);
                                            get_user_details.putExtra("CalledActivity", getIntent().getStringExtra("CalledActivity"));
                                            get_user_details.putExtra("account_type", "Google");
                                            startActivityForResult(get_user_details, 1);
                                        }
                                    }
                                }
                            });
                            //onBackPressed();
                        } else {
                            Log.d(TAG, "signInWithCredential:Failed");
                        }

                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        Intent i = getIntent();
        String calledActivity = i.getStringExtra("CalledActivity");
        if (calledActivity.equals("StartActivity")) {
            moveTaskToBack(true);
        } else if (calledActivity.equals("NewsActivity")) {
            setResult(RESULT_SIGN_IN);
            finish();
        } else
            finish();
    }
}