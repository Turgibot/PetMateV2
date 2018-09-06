package com.example.guyto.petmatev2;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.guyto.petmatev2.Utility.*;

public class MyMatchesActivity extends AppCompatActivity {
    private Button backBtn;
    private Utility utils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);
        backBtn = (Button)findViewById(R.id.my_matches_back_btn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //utils.goToMyPets(getApplicationContext(), MyMatchesActivity.this);
                makeToast(getApplicationContext(), "MyMatchesActivity.this:"MyMatchesActivity.this);
            }
        });
    }

}
