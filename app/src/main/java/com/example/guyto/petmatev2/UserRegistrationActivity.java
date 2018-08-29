package com.example.guyto.petmatev2;

import android.animation.TypeConverter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;

public class UserRegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebase;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPhoneView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordRepeatView;
    private Button mStartUseBtn;
    private String email, passRepeat, password, fname ,lname, phone, emailHash;
    private User u;
    private boolean debug = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        firebase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFirstNameView = (EditText) findViewById(R.id.firstNameView);
        mLastNameView = (EditText) findViewById(R.id.lastNameView);
        mPhoneView = (EditText) findViewById(R.id.phoneView);
        mEmailView = (EditText) findViewById(R.id.emailView);
        mPasswordView = (EditText) findViewById(R.id.passView);
        mPasswordRepeatView = (EditText) findViewById(R.id.passRepeatView);
        mStartUseBtn = (Button) findViewById(R.id.startUseBtn);
        mFirstNameView.requestFocus();





        mStartUseBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                if (isValidRegistration()){
                    try {
                        u = new User(fname, lname, email, password, phone,null,null);
                        emailHash = sha256(email);
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    registerUser();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Authentication incomplete", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(),"Authentication sha: "+sha256(email)+"-----"+ e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean isValidRegistration() {
        resetError();
        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        passRepeat = mPasswordRepeatView.getText().toString();
        password = mPasswordView.getText().toString();
        fname = mFirstNameView.getText().toString();
        lname = mLastNameView.getText().toString();
        phone = mPhoneView.getText().toString();

        boolean abort = false;
        View focusView = null;
        // Check for a valid password.
        if (!isPasswordValid(mPasswordView, password)) {
            focusView = mPasswordView;
            abort = true;
        }
        else
        if(!isMatchingPassword(mPasswordRepeatView, password, passRepeat)){
            focusView = mPasswordRepeatView;
            abort = true;
        }

        if (!isEmailValid(mEmailView, email)) {
            focusView = mEmailView;
            abort = true;
        }
        if (!isPhoneValid(mPhoneView, phone)) {
            focusView = mPhoneView;
            abort = true;
        }
        //Check that the last name is valid
        if(!isTextValid(mLastNameView, lname)){
            abort = true;
            focusView = mLastNameView;
        }
        //Check that the first name is valid
        if(!isTextValid(mFirstNameView, fname)){
            abort = true;
            focusView = mFirstNameView;
        }

        if (abort) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        return !abort;
    }

    private void resetError(){
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mPhoneView.setError(null);
        mPasswordRepeatView.setError(null);
    }
    private boolean isPureString(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPureNum(String name) {
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsDigitAndLetterOnly(String s){
        char[] chars = s.toCharArray();
        boolean isLetter = false, isDigit = false;
        for (char c : chars) {
            if(Character.isLetter(c) && !isLetter) {
                isLetter = true;
            }
            if(Character.isDigit(c) && !isDigit) {
                isDigit = true;
            }
        }
        return isDigit && isLetter;
    }

    private boolean isTextValid(EditText t, String s){
        if (TextUtils.isEmpty(s)){
            t.setError(getString(R.string.error_field_required));
            return false;
        }
        if(!isPureString(s)) {
            t.setError(getString(R.string.error_invalid_format));
            return false;
        }
        return true;
    }

    private boolean isPhoneValid(EditText t, String s){
        if (TextUtils.isEmpty(s)){
            t.setError(getString(R.string.error_field_required));
            return false;
        }
        if(!isPureNum(s)){
            t.setError(getString(R.string.error_invalid_format));
            return false;
        }
        if(s.length()!=10){
            t.setError(getString(R.string.error_invalid_length));
            return false;
        }
        return true;
    }
    private boolean isPasswordValid(EditText p, String s){

        if (TextUtils.isEmpty(s)){
            p.setError(getString(R.string.error_field_required));
            return false;
        }
        if(s.length()<4){
            p.setError(getString(R.string.error_invalid_password));
            return false;
        }
        if(!containsDigitAndLetterOnly(s)) {
            p.setError(getString(R.string.error_no_legal_password));
            return false;
        }

        return true;
    }

    private boolean isEmailValid(EditText e, String email) {
        if (TextUtils.isEmpty(email)) {
            e.setError(getString(R.string.error_field_required));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            e.setError(getString(R.string.error_invalid_email));
            return false;
        }
        return true;
    }


    private boolean isMatchingPassword(EditText p , String src, String target){
        if(!src.equals(target)){
            p.setError(getString(R.string.error_matched_passwords));
            return false;
        }
        return true;
    }

    public  String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    private void registerUser(){
        try {
            DatabaseReference usersDB = firebase.getReference(getString(R.string.users)).child(emailHash);
            usersDB.setValue(u).addOnCompleteListener(new OnCompleteListener<Void>() {

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "registration successful", Toast.LENGTH_LONG).show();
                        SharedPreferences sharedPreferences = getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("UserName", fname + "" + lname);
                        editor.putString("Phone", phone);
                        editor.apply();
                        goToMyPets();

                    } else {
                        Toast.makeText(getApplicationContext(), "registration failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Registration sha: "+sha256(email)+"-----"+ e.toString(), Toast.LENGTH_LONG).show();
        }
    }
    private void goToMyPets(){
        Intent intent = new Intent(UserRegistrationActivity.this, MyPetsActivity.class);
        startActivity(intent);
        finish();
    }

}