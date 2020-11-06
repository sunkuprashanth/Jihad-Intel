package com.example.JihadIntel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    EditText name, mobile, email_id, pass, c_pass, dob;
    RadioGroup gender;
    DatePickerDialog.OnDateSetListener date;
    Button sign_up;
    DatePickerDialog dpg;
    String name_str, mobile_str, email_id_str, pass_str, c_pass_str, gender_str, dob_str;
    int loop_var = 0;
    Drawable edit_back;
    boolean valid_details = true;
    ArrayList<EditText> for_listener = new ArrayList<>();
    Map<String,Object> user;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.name);
        mobile = findViewById(R.id.mobile);
        email_id = findViewById(R.id.email_id);
        pass = findViewById(R.id.pass);
        c_pass = findViewById(R.id.c_pass);
        dob = findViewById(R.id.dob);
        gender = findViewById(R.id.gender);
        sign_up = findViewById(R.id.sign_up);

        edit_back = name.getBackground();

        // For Reducing redundant text Listeners for all EditText's
        for_listener.add(name);
        for_listener.add(mobile);
        for_listener.add(email_id);
        for_listener.add(pass);
        for_listener.add(c_pass);
        for_listener.add(dob);

        for(loop_var = 0; loop_var < for_listener.size(); loop_var++) {
            final EditText temp = for_listener.get(loop_var);
            for_listener.get(loop_var).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    //Toast.makeText(SignupActivity.this, "on "+for_listener.get(loop_var).getText().toString(),Toast.LENGTH_SHORT).show();
                    temp.setBackground(edit_back);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
        final Calendar myCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int y, int m, int d) {
                dob_str = checkFormat(d) + "/" + checkFormat(m + 1) + "/" + y;
                dob.setText(dob_str);
            }
        };
        dpg = new DatePickerDialog(SignupActivity.this, date, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        dob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dpg.show();
                return false;
            }
        });

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                gender.setBackground(null);
            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                valid_details = true;

                for(EditText temp : for_listener) {
                    if(temp.getText().toString().equals("")) {
                        temp.setBackground(getResources().getDrawable(R.drawable.red_border));
                        valid_details = false;
                    }
                }
                int select_id = gender.getCheckedRadioButtonId();
                if (select_id == -1) {
                    gender.setBackground(getResources().getDrawable(R.drawable.red_border));
                    valid_details = false;
                } else {
                    RadioButton selected_rb = findViewById(select_id);
                    gender_str = selected_rb.getText().toString();
                }

                name_str = name.getText().toString();
                mobile_str = "+91"+mobile.getText().toString();
                email_id_str = email_id.getText().toString();
                dob_str = dob.getText().toString();
                pass_str = GeneralMethods.hash(pass.getText().toString());
                c_pass_str = GeneralMethods.hash(c_pass.getText().toString());

                if (!pass_str.equals(c_pass_str) || pass_str.equals("")) {
                    valid_details = false;
                    Toast.makeText(SignupActivity.this, "Passwords do not match",Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "onSuccess: User added" + valid_details);

                if (valid_details) {

                    user = new HashMap<String,Object>();
                    user.put("account_type","Email");
                    user.put("name",name_str);
                    user.put("email_id",email_id_str);
                    user.put("mobile",mobile_str);
                    user.put("gender",gender_str);
                    user.put("dob",dob_str);
                    user.put("password",pass_str);
                    user.put("photo_url", "");

                    mAuth.createUserWithEmailAndPassword(email_id_str, pass_str)
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser account = mAuth.getCurrentUser();

                                        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.prefs_file_login, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean("logged_in",true);
                                        editor.commit();

                                        GeneralMethods.postLoginCalls(SignupActivity.this);

                                        firebaseDatabase.collection("users").document(account.getUid()).
                                                set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(SignupActivity.this, "Details stored Successfully" ,Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        if (getIntent().getStringExtra("CalledActivity").equals("NewsActivity")) {
                                            setResult(GeneralMethods.RESULT_SIGN_IN);
                                            onBackPressed();
                                        } else {
                                            Intent showing_news = new Intent(getApplicationContext(),MainActivity.class);
                                            showing_news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(showing_news);
                                        }

                                    } else {
                                        String execption = "" + task.getException();
                                        if (execption.contains("FirebaseAuthUserCollisionException"))
                                            Toast.makeText(SignupActivity.this, "Th given Mail id is already in use",Toast.LENGTH_SHORT).show();
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    }
                                }
                            });

                }
            }
        });
    }

    public String checkFormat(int number)
    {
        return number<=9?"0"+number:""+number;
    }
}