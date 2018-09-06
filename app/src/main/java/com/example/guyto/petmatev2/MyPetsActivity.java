package com.example.guyto.petmatev2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.example.guyto.petmatev2.Utility.makeToast;
import static com.example.guyto.petmatev2.Utility.sha256;
import static java.lang.StrictMath.abs;
//TODO cancel click on photo and on edit profile if no pets
public class MyPetsActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String email;
    private User user;
    private Button addPetBtn, rightBtn, leftBtn, editProfBtn;
    private Spinner menuSpinner;
    private TextView infoText, nameText;
    private List<Pet> petList;
    private ImageView petProfileImg;
    private int petIndex;
    private boolean isSpinnerTouched, isBack;
    private static final String[] menuOptions = {"","Profile", "Matches", "Logout"};
    private Utility utils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pets);
        final ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(MyPetsActivity.this,
                android.R.layout.simple_spinner_item, menuOptions);
        menuSpinner = (Spinner)findViewById(R.id.menuSpinner);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        petProfileImg = (ImageView)findViewById(R.id.petProfileImg);
        rightBtn = (Button)findViewById(R.id.rightBtn);
        leftBtn = (Button)findViewById(R.id.leftBtn);
        addPetBtn = (Button)findViewById(R.id.addPetBtn);
        editProfBtn = (Button)findViewById(R.id.editProfBtn);
        infoText = (TextView)findViewById(R.id.info_text);
        nameText = (TextView)findViewById(R.id.name_text);
        petList = new ArrayList<Pet>();
        isSpinnerTouched = false;
        isBack = wasHere();
        petIndex = 0;
        utils = new Utility();
        user = utils.getSPUser(getApplicationContext());
        email = user.getEmail();


        if(isBack){
            displayPetInfo(utils.getSPPet(getApplicationContext()));
        }
        setAdapterAndListener(menuAdapter, menuSpinner);



        if (email.isEmpty()){
            makeToast(getApplicationContext(), "email is not defined at MyPets");
        }else {
            usersRef.child(sha256(email)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot petRef = dataSnapshot.child("Pets");
                    if (petRef.getChildrenCount() == 0){
                        editProfBtn.setVisibility(View.INVISIBLE);
                        petProfileImg.setClickable(false);
                        displayDefaultInfo();
                        return;
                    }else{
                        for(DataSnapshot child: petRef.getChildren()){
                            petList.add(child.getValue(Pet.class));
                        }
                        user.setPets(petList);
                        if(!isBack){
                            displayPetInfo(petList.get(0));
                            utils.setSPPet(getApplicationContext(), petList.get(0));
                        }
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
        editProfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPetProfile(true);

            }
        });
        petProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMatchFinder();
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

    private void cycleImages(boolean right) {
        if (!petList.isEmpty()) {
            if (right) {
                petIndex = (petIndex + 1) % petList.size();
            } else {
                petIndex = (petIndex - 1) % petList.size();
            }
            displayPetInfo(petList.get(abs(petIndex)));
            utils.setSPPet(getApplicationContext(), petList.get(abs(petIndex)));
        }
    }

    private void displayPetInfo(Pet pet){
        if(pet == null) {
            if (petList != null) {
                pet = petList.get(0);
            } else {
                displayDefaultInfo();
                return;
            }
        }
        displayImage(pet.getImage());
        nameText.setText(pet.getName());
        displayInfo(pet.getAge(), pet.getGender(), pet.getType(), pet.getArea(), pet.getLookingFor(), pet.getPurpose());

    }

    private void displayImage(String imageStr){
        byte[] imageBytes = Base64.decode(imageStr, Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        petProfileImg.setImageBitmap(decodedImage);
    }
    private void displayInfo(String age, String gender, String type, String area, String looking, String purpose){
        String info = "I'm a "+age+" year old "+gender+" "+type+"\nfrom the "+area+" area.\nLooking for a "+looking+" "+type+" for "+purpose+".";
        infoText.setText(info);
    }
    private void displayDefaultInfo(){
        petProfileImg.setImageResource(R.drawable.silhouette);
        nameText.setText("Your Pet's Name");
        String info = "Click on the \"Add a pet\" button to add your pets";
        infoText.setText(info);
    }

    private void goToMatchFinder(){
        if(petList == null)
            return;
        Intent intent = new Intent(MyPetsActivity.this, MatchFinderActivity.class);
        startActivity(intent);
        finish();
    }

    private void setAdapterAndListener(final ArrayAdapter<String> adapter, final Spinner spinner){
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isSpinnerTouched = true;
                return false;
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(PetProfileActivity.this, "selected: "+adapter.getItem(position), Toast.LENGTH_LONG).show();
                if(!isSpinnerTouched)
                    return;
                switch (position){
                    case 1:
                        goToUserProfile();
                        break;
                    case 2:
                        goToMatches();
                        break;
                    case 3:
                        logOut();
                        break;
                }
                isSpinnerTouched = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void goToUserProfile(){
        Intent intent = new Intent(MyPetsActivity.this, UserRegistrationActivity.class);
        intent.putExtra("isEdit", true);
        intent.putExtra(getString(R.string.prev_activity), "MyPetsActivity");
        startActivity(intent);
        finish();
    }
    private void goToMatches(){
        Intent intent = new Intent(MyPetsActivity.this, MyMatchesActivity.class);
        startActivity(intent);
        finish();
    }
    private void logOut(){
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            mAuth.signOut();
        }
        Intent intent = new Intent(MyPetsActivity.this, OpenActivity.class);
        startActivity(intent);
        finish();
    }
    private boolean wasHere(){
        Intent intent = getIntent();
        String prevActivity = intent.getStringExtra("prevActivity");
        if(prevActivity==null){
            return false;
        }
        return prevActivity.equals(getString(R.string.pet_profile_activity));
    }
}
