package com.example.guyto.petmatev2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.concurrent.Semaphore;

import static com.example.guyto.petmatev2.Utility.sha256;


public class MatchFinderActivity extends Activity{


    private int index;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private ArrayList<Pet> petList;
    private PetAdapter petAdapter;
    private String instructionStr, lastStr;
    private Pet srcPet, lastPet, selectedPet, firstPet;
    private User srcUser;
    private Button backBtn;
    private boolean isDataReady;
    private SwipeFlingAdapterView flingContainer;
    private Utility utils;
    private boolean listIsReady, isRight;
    private int numOfPets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_finder);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        utils = new Utility();
        isRight = false;
        srcUser = utils.getSPUser(getApplicationContext());
        srcPet = utils.getSPPet(getApplicationContext());
        instructionStr = "Swipe right to Like\nLeft tp pass";
        lastStr = "That's all folks \nCome again soon to find new mates!!";
        backBtn = (Button)findViewById(R.id.back_btn);
        isDataReady = false;
        petList = new ArrayList<>();
        firstPet = new Pet("Example", "",instructionStr,"","","","",getSilhouete(),null);
        lastPet = new Pet("Yeah!!!", "",lastStr,"","","","",firstPet.getImage(),null);
        petList.add(firstPet);
        populatePetListByPreference();
        petAdapter = new PetAdapter(this, petList );
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame) ;
        flingContainer.setAdapter(petAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                makeToast(getApplicationContext(), petList.get(0).getName());
                if(isRight){
                    boolean isMatch = registerAndMatch();
                }
                petList.remove(0);
                petAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                isRight = false;
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                isRight = true;
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                if(listIsReady)
                {
                    petList.add(lastPet);
                    listIsReady = false;
                }
                if(petList.isEmpty()){
                    goToMyMatches();
                }
                //makeToast(getApplicationContext(), "itemsInAdapter: "+itemsInAdapter);
                petAdapter.notifyDataSetChanged();
                index++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyPets();
            }
        });

    }

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }

    private void populatePetListByPreference(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                try {
                    for(DataSnapshot userSnapSHot : dataSnapshot.getChildren()){
                        User u = userSnapSHot.getValue(User.class);
                        if(!sha256(u.getEmail()).equals(sha256(srcUser.getEmail()))){
                            for(DataSnapshot petSnapshot: userSnapSHot.child("Pets").getChildren()){
                                Pet p = petSnapshot.getValue(Pet.class);
                                if(!p.getType().equals(srcPet.getType()))
                                    continue;
                                if(p.getGender().equals(srcPet.getLookingFor())||srcPet.getLookingFor().equals("Any")){
                                    petList.add(p);
                                }
                            }
                        }
                    }
                    listIsReady = true;
                    numOfPets = petList.size();
                } catch (Exception e) {
                    petList = null;
                    Toast.makeText(getApplicationContext(), "petList is null -> Failed to read value." +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "getSrcUser: Failed to read value." +
                        databaseError.toException(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    class PetAdapter extends ArrayAdapter<Pet> {
        Activity context;
        ArrayList<Pet> pets;
        private LayoutInflater inflater;


        public PetAdapter(Activity context, ArrayList<Pet> pets) {
            super(context,R.layout.item, pets);
            this.context = context;
            this.pets = pets;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount(){
            return pets.size();
        }

        @Override
        public Pet getItem(int position) {
            return pets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = inflater.inflate(R.layout.item, parent, false);
            TextView textViewName = (TextView)itemView.findViewById(R.id.flingName);
            TextView textViewInfo = (TextView)itemView.findViewById(R.id.flingInfo);
            ImageView profileImageView = (ImageView)itemView.findViewById(R.id.flingImage);
            selectedPet = pets.get(position);
            textViewName.setText(selectedPet.getName());
            String info;
            if(selectedPet == lastPet)
                info = lastStr;
            else if(selectedPet == firstPet)
                info = instructionStr;
            else
                info ="A "+selectedPet.getAge()+" year old "+selectedPet.getGender() +" "+selectedPet.getType()+".\nI'm from the "+selectedPet.getArea()+" area.\nI'm here for the purpose of "+selectedPet.getPurpose()+".";
            textViewInfo.setText(info);
            byte[] imageBytes = Base64.decode(selectedPet.getImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profileImageView.setImageBitmap(decodedImage);
            return itemView;
        }

    }
    private String getSilhouete(){
        ImageView img = (ImageView)findViewById(R.id.demoView);
        Bitmap bitmap= ((BitmapDrawable)img.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void goToMyPets(){
        Intent intent = new Intent(MatchFinderActivity.this, MyPetsActivity.class);
        startActivity(intent);
        finish();
    }
    private void goToMyMatches(){
        Intent intent = new Intent(MatchFinderActivity.this, MyMatchesActivity.class);
        startActivity(intent);
        finish();
    }
    private boolean registerAndMatch(){
        return true;
    }
    class MatchingPet extends Pet{
        private String userEmail;

        public MatchingPet(){
            super();
            userEmail = "";
        }
        public MatchingPet(String userEmail, String name, String age, String type, String gender, String lookingFor, String purpose, String area, String image, Dictionary<String, ArrayList<String>> likesDict){
            super(name, age, type, gender, lookingFor, purpose, area, image, likesDict);
            this.userEmail = userEmail;
        }
    }
}
