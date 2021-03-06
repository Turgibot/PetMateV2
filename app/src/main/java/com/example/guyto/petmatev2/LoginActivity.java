//--------------------------------------------------------------------------------------------------
//Handles Login operations
//--------------------------------------------------------------------------------------------------

package com.example.guyto.petmatev2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.guyto.petmatev2.Utility.containsDigitAndLetterOnly;
import static com.example.guyto.petmatev2.Utility.makeToast;
import static com.example.guyto.petmatev2.Utility.sha256;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button login_btn;
    private Button acnt_btn;
    private String email, password;
    private User appUser;
    private List<Pet> petList;
    private Utility utils;
    private ProgressBar loginPB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        mEmailView = (EditText) findViewById(R.id.login_email);
        mPasswordView = (EditText) findViewById(R.id.login_pass);
        login_btn = (Button) findViewById(R.id.login_btn);
        acnt_btn = (Button) findViewById(R.id.new_acnt_btn);
        loginPB = (ProgressBar)findViewById(R.id.login_pb);
        appUser = new User();
        utils = new Utility();
        petList = new ArrayList<Pet>();

        acnt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegistration();
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInfo()){
                    //if sdk >= 27 use fire base authentication
                    loginPB.setVisibility(View.VISIBLE);
                    if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        mAuthLogin();
                    }else {
                        regLogin();
                    }
                }
            }
        });
        setPBColor(Color.BLUE);
    }

    private boolean validateInfo() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(mPasswordView, password)) {
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (!isEmailValid(mEmailView, email)) {
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }
        return true;
    }


    private void mAuthLogin(){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    try {
                        if (task.isSuccessful()) {
                            regLogin();
                        } else {
                            makeToast(LoginActivity.this, "Authentication failed. Try Again");
                        }
                    }catch (Exception e){
                        makeToast(getApplicationContext(),"At signInWithEmailAndPassword: "+e.toString());
                    }
                }
            });
    }

    private boolean isPasswordValid(EditText p, String s){

        if (TextUtils.isEmpty(s)){
            p.setError(getString(R.string.error_field_required));
            return false;
        }
        if(s.length()<6){
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

    private void setPBColor(int color){
        Drawable progressDrawable = loginPB.getIndeterminateDrawable().mutate();
        progressDrawable.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        loginPB.setProgressDrawable(progressDrawable);
    }
    private void goToMyPets(){
        Intent intent = new Intent(LoginActivity.this, MyPetsActivity.class);
        startActivity(intent);
        finish();
    }
    private void goToRegistration(){
        Intent intent = new Intent(LoginActivity.this, UserRegistrationActivity.class);
        startActivity(intent);
        finish();
    }
    private void regLogin(){
        String hashedEmail = sha256(email);
        DatabaseReference userQuery = usersRef.child(hashedEmail).getRef();
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                try {
                    appUser = dataSnapshot.getValue(User.class);
                    if(appUser.getEmail().equals(email) && appUser.getPassword().equals(password)) {
                        Toast.makeText(getApplicationContext(), "Hi " + appUser.getFirstName() + ". You are logged in.", Toast.LENGTH_SHORT).show();
                        utils.setSPUser(getApplicationContext(), appUser);
                        goToMyPets();
                    }else {
                        Toast.makeText(LoginActivity.this, "Authentication failed. Try Again",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    makeToast(getApplicationContext(), "Please make sure you register first");
                }
                loginPB.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to read value." +
                        databaseError.toException(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}