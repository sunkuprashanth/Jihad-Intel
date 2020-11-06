package com.example.JihadIntel;

import android.net.Uri;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public class UserDetails {

    private String name;
    private String email_id;
    private String id;
    private Uri photo_url;
    String mobile;
    String dob;
    String gender;
    String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(Uri photo_url) {
        this.photo_url = photo_url;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {this.mobile = mobile;}

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
