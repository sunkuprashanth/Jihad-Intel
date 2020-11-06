package com.example.JihadIntel;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GetUserDetailsActivity extends AppCompatActivity {

    private static final String TAG = "GetUserDetailsActivity";
    EditText name, mobile, email_id, pass, c_pass, dob;
    RadioGroup gender;
    DatePickerDialog.OnDateSetListener date;
    Button submit;
    DatePickerDialog dpg;
    String name_str, mobile_str, email_id_str, gender_str, dob_str;
    int loop_var = 0;
    Drawable edit_back;
    boolean valid_details = true;
    ArrayList<EditText> for_listener = new ArrayList<>();
    Map<String,Object> user;
    String type_str;
    Intent prev_intent;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseDatabase = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_user_details);

        name = findViewById(R.id.name);
        mobile = findViewById(R.id.mobile);
        email_id = findViewById(R.id.email_id);
        dob = findViewById(R.id.dob);
        gender = findViewById(R.id.gender);
        submit = findViewById(R.id.sign_up);

        edit_back = name.getBackground();
        prev_intent = getIntent();
        type_str = prev_intent.getStringExtra("account_type");

        if (type_str.equals("Mobile")) {
            mobile_str = mAuth.getCurrentUser().getPhoneNumber();
            mobile.setText(mobile_str.substring(3));
            mobile.setEnabled(false);
        } else {
            name_str = mAuth.getCurrentUser().getDisplayName();
            email_id_str = mAuth.getCurrentUser().getEmail();
            name.setText(name_str);
            email_id.setText(email_id_str);
            name.setEnabled(false);
            email_id.setEnabled(false);
        }



        // For Reducing redundant text Listeners for all EditText's
        for_listener.add(name);
        for_listener.add(email_id);
        for_listener.add(mobile);
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
        dpg = new DatePickerDialog(GetUserDetailsActivity.this, date, myCalendar.get(Calendar.YEAR),
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

        submit.setOnClickListener(new View.OnClickListener() {
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
                email_id_str = email_id.getText().toString();
                mobile_str = "+91"+mobile.getText().toString();
                dob_str = dob.getText().toString();


                Log.d(TAG, "onSuccess: User added" + valid_details);

                if (valid_details) {

                    user = new HashMap<String,Object>();
                    user.put("account_type",type_str);
                    user.put("name",name_str);
                    user.put("email_id",email_id_str);
                    user.put("mobile",mobile_str);
                    user.put("gender",gender_str);
                    user.put("dob",dob_str);
                    if (type_str.equals("Google"))
                        user.put("photo_url", mAuth.getCurrentUser().getPhotoUrl().toString());
                    else
                        user.put("photo_url","");

                    firebaseDatabase.collection("users").document(mAuth.getCurrentUser().getUid()).
                            set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(GetUserDetailsActivity.this, "Details uploaded Successfully" ,Toast.LENGTH_SHORT).show();
                            GeneralMethods.postLoginCalls(GetUserDetailsActivity.this);
                            if (getIntent().getStringExtra("CalledActivity").equals("NewsActivity")) {
                                setResult(GeneralMethods.RESULT_SIGN_IN);
                                onBackPressed();
                            } else {
                                Intent showing_news = new Intent(getApplicationContext(),MainActivity.class);
                                showing_news.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(showing_news);
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