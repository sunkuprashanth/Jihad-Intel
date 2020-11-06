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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocalLoginActivity extends AppCompatActivity {

    private static final String TAG = "LocalLoginActivity";
    EditText userId, password;
    String userId_str, password_str;
    Button login;
    TextView goto_register;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    int LOCAL_SIGN_IN_REQ = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_login);

        userId = findViewById(R.id.user_id);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        goto_register = findViewById(R.id.goto_register_link);

        sharedPreferences = getSharedPreferences(MainActivity.prefs_file_login,MODE_PRIVATE);
        editor = sharedPreferences.edit();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userId_str = userId.getText().toString();
                password_str = GeneralMethods.hash(password.getText().toString());
                mAuth.signInWithEmailAndPassword(userId_str, password_str)
                        .addOnCompleteListener(LocalLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser account = mAuth.getCurrentUser();
                                    editor.putBoolean("logged_in",true);
                                    editor.putBoolean("FirstRun",false);
                                    editor.commit();
                                    Log.d(TAG, "onComplete: "  );
                                    Toast.makeText(LocalLoginActivity.this, mAuth.getCurrentUser().getUid(), Toast.LENGTH_LONG).show();

                                    GeneralMethods.postLoginCalls(LocalLoginActivity.this);
                                    if (getIntent().getStringExtra("CalledActivity").equals("NewsActivity")) {
                                        setResult(GeneralMethods.RESULT_SIGN_IN);
                                        onBackPressed();
                                    } else {
                                        Intent showing_news = new Intent(getApplicationContext(),MainActivity.class);
                                        showing_news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(showing_news);
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.d(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LocalLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });

        goto_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(LoginActivity.this,"Moving to Registration Form",Toast.LENGTH_SHORT).show();
                Intent goto_register_form = new Intent(LocalLoginActivity.this, SignupActivity.class);
                goto_register_form.putExtra("CalledActivity",getIntent().getStringExtra("CalledActivity"));
                startActivityForResult(goto_register_form,LOCAL_SIGN_IN_REQ);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == GeneralMethods.RESULT_SIGN_IN) {
            setResult(GeneralMethods.RESULT_SIGN_IN);
            onBackPressed();
        }
    }
}