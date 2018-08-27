package com.example.guyto.petmatev2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference database;
    private EditText emailText;
    private EditText passText;
    private Button login_btn;
    private Button acnt_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        database = FirebaseDatabase.getInstance().getReference();
        emailText = (EditText) findViewById(R.id.login_email);
        passText = (EditText) findViewById(R.id.login_pass);
        login_btn = (Button) findViewById(R.id.login_btn);
        acnt_btn = (Button) findViewById(R.id.new_acnt_btn);

        
    }
}
//fbTest = (Button)findViewById(R.id.testBtn);
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//
//        fbTest.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View view) {
//        //1 create a child
//        //2 assign value to the child
//
//        mDatabase.child("name").setValue("Guy");
//
//        }
//        });