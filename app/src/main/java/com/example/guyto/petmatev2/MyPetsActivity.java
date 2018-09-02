package com.example.guyto.petmatev2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.guyto.petmatev2.Utility.makeToast;
import static com.example.guyto.petmatev2.Utility.sha256;

public class MyPetsActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private String email;
    private User user;
    private Button addPetBtn;
    private List<Pet> petList;
    private ImageView petProfileImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pets);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        petProfileImg = (ImageView)findViewById(R.id.petProfileImg);
        petList = new ArrayList<Pet>();
        addPetBtn = (Button)findViewById(R.id.addPetBtn);
        email = getSPEmail();
        if (email.isEmpty()){
            makeToast(getApplicationContext(), "email is not defined at MyPets");
        }else {
            usersRef.child(sha256(email)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    DataSnapshot petRef = dataSnapshot.child("Pets");
                    if (petRef.getChildrenCount() == 0){
                        return;
                    }else{
                        for(DataSnapshot child: petRef.getChildren()){
                            petList.add(child.getValue(Pet.class));
                        }
                        user.setPets(petList);
                        Uri imageUri = Uri.parse(petList.get(0).getImageUri());
                        petProfileImg.setImageURI(imageUri);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        addPetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPetProfile(false);
            }
        });
    }

    private void goToPetProfile(boolean isEdit){
        if(isEdit){

        }else{

        }
        Intent intent = new Intent(MyPetsActivity.this, PetProfileActivity.class);
        startActivity(intent);
        finish();
    }


    private String getSPEmail(){
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "errorGettingEmail");
    }
}
