package com.example.guyto.petmatev2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.guyto.petmatev2.Utility.makeToast;
import static com.example.guyto.petmatev2.Utility.sha256;
import static java.lang.StrictMath.abs;

public class MyPetsActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private String email;
    private User user;
    private Button addPetBtn, rightBtn, leftBtn, editProfBtn;
    private TextView infoText, nameText;
    private List<Pet> petList;
    private ImageView petProfileImg;
    private int petIndex;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pets);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        petProfileImg = (ImageView)findViewById(R.id.petProfileImg);
        rightBtn = (Button)findViewById(R.id.rightBtn);
        leftBtn = (Button)findViewById(R.id.leftBtn);
        addPetBtn = (Button)findViewById(R.id.addPetBtn);
        editProfBtn = (Button)findViewById(R.id.editProfBtn);
        infoText = (TextView)findViewById(R.id.info_text);
        nameText = (TextView)findViewById(R.id.name_text);
        petList = new ArrayList<Pet>();
        petIndex = 0;
        email = getSPEmail();
        if (email.isEmpty()){
            makeToast(getApplicationContext(), "email is not defined at MyPets");
        }else {
            usersRef.child(sha256(email)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    DataSnapshot petRef = dataSnapshot.child("Pets");
                    if (petRef.getChildrenCount() == 0){
                        displayDefaultInfo();
                        return;
                    }else{
                        for(DataSnapshot child: petRef.getChildren()){
                            petList.add(child.getValue(Pet.class));
                        }
                        user.setPets(petList);
                        displayPetInfo(petList.get(0));
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

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleImages(true);
            }
        });
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycleImages(false);
            }
        });

    }

    private void goToPetProfile(boolean isEdit){
        Intent intent = new Intent(MyPetsActivity.this, PetProfileActivity.class);
        if(isEdit){
            intent.putExtra("isEdit",true);
        }else{
            intent.putExtra("isEdit",false);
        }

        startActivity(intent);
        finish();
    }


    private String getSPEmail(){
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "errorGettingEmail");
    }
    private void cycleImages(boolean right) {
        if (!petList.isEmpty()) {
            if (right) {
                petIndex = (petIndex + 1) % petList.size();
            } else {
                petIndex = (petIndex - 1) % petList.size();
            }
            displayPetInfo(petList.get(abs(petIndex)));
        }
    }

    private void displayPetInfo(Pet pet){
        displayImage(pet.getImage());
        nameText.setText(pet.getName());
        displayInfo(pet.getAge(), pet.getAge(), pet.getType(), pet.getArea(), pet.getLookingFor(), pet.getPurpose());
    }
    private void displayImage(String imageStr){
        byte[] imageBytes = Base64.decode(imageStr, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 100;
        options.outWidth = 200;
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
        petProfileImg.setImageBitmap(decodedImage);
    }
    private void displayInfo(String age, String gender, String type, String area, String looking, String purpose){
        String info = "I'm a "+age+" year old "+gender+" "+type+"\nfrom the "+area+" area.\nLooking for a "+looking+" "+type+" for "+purpose+".";
        infoText.setText(info);
    }
    private void displayDefaultInfo(){
        nameText.setText("Your Pet's Name");
        String info = "Click on the Add Pets button to add your pets";
        infoText.setText(info);
    }

}
