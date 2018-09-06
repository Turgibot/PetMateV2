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


    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private ArrayList<Pet> petList;
    private PetAdapter petAdapter;
    private String instructionStr, lastStr;
    private Pet srcPet, lastPet, selectedPet, firstPet, viewedPet;
    private User srcUser, firstUser, lastUser;
    private ArrayList<User> userList;
    private Button backBtn;
    private boolean isDataReady;
    private SwipeFlingAdapterView flingContainer;
    private Utility utils;
    private boolean listIsReady, isRight;
    private int numOfMatches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_finder);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        utils = new Utility();
        isRight = false;
        instructionStr = "Swipe right to Like\nLeft tp pass";
        lastStr = "That's all folks \nCome again soon to find new mates!!";
        backBtn = (Button)findViewById(R.id.back_btn);
        isDataReady = false;
        numOfMatches = 0;
        srcUser = utils.getSPUser(getApplicationContext());
        srcPet = utils.getSPPet(getApplicationContext());
        petList = new ArrayList<>();
        userList = new ArrayList<>();
        firstPet = new Pet("Example", "",instructionStr,"","","","",getSilhouete(),null);
        lastPet = new Pet("Yeah!!!", "",lastStr,"","","","",firstPet.getImage(),null);
        firstUser = new User();
        lastUser = new User();
        firstUser.addPet(firstPet);
        lastUser.addPet(lastPet);
        userList.add(firstUser);
        petList.add(firstPet);
        populatePetListByPreference();
        petAdapter = new PetAdapter(this, userList);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame) ;
        flingContainer.setAdapter(petAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                viewedPet = userList.get(0).getPets().get(0);
                if(isRight){
                    boolean isMatch = registerAndMatch();
                    makeToast(getApplicationContext(), "righth");
                }

                userList.get(0).getPets().remove(0);
                //petList.remove(0);
                if(userList.get(0).getPets().size()==0){
                    userList.remove(0);
                }
                if(userList.isEmpty()){
                    goToMyMatches();
                    return;
                }
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
                    userList.add(lastUser);
                    listIsReady = false;
                }

                //petList.add(userList.get(0).getPets().get(0));
                //makeToast(getApplicationContext(), "itemsInAdapter: "+itemsInAdapter);
                petAdapter.notifyDataSetChanged();
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
                            ArrayList<Pet> userPetList = new ArrayList();
                            for(DataSnapshot petSnapshot: userSnapSHot.child("Pets").getChildren()){
                                Pet p = petSnapshot.getValue(Pet.class);
                                if(!p.getType().equals(srcPet.getType()))
                                    continue;
                                if(p.getGender().equals(srcPet.getLookingFor())||srcPet.getLookingFor().equals("Any")){
                                    userPetList.add(p);
                                    numOfMatches++;
                                }
                            }
                            if(userPetList.size()>0){
                                u.setPets(userPetList);
                                userList.add(u);
                            }
                        }
                    }
                    listIsReady = true;
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
        ArrayList<User> users;
        private LayoutInflater inflater;


        public PetAdapter(Activity context, ArrayList<User> users) {

            super(context,R.layout.item, users.get(0).getPets());
            this.context = context;
            this.users = users;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount(){
            return users.get(0).getPets().size();
        }

        @Override
        public Pet getItem(int position) {
            return users.get(0).getPets().get(position);
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
            selectedPet = users.get(0).getPets().get(position);
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

}
