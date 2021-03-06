package com.example.guyto.petmatev2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class OpenActivity extends AppCompatActivity {
    private final int timeMS = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (this){
                        wait(timeMS);
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
