package com.example.guyto.petmatev2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OpenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (this){
                        wait(5000);
                        Intent intent = new Intent(OpenActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        }
                    }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
