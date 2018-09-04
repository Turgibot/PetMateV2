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
import java.util.concurrent.Semaphore;

import static com.example.guyto.petmatev2.Utility.sha256;


public class MatchFinderActivity extends Activity{
    private ArrayList<String> al;
    private ArrayAdapter<String> arrayAdapter;
    private int i;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private ArrayList<Pet> petList;
    private PetAdapter petAdapter;
    private String srcPetName, hashedEmail;
    private Pet srcPet, targetPet;
    private User srcUser, targetUser;
    static Semaphore mutex;
    private Button backBtn;
    private boolean isDataReady;
    private SwipeFlingAdapterView flingContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_finder);
        mutex = new Semaphore(1);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        hashedEmail = sha256(getSPEmail());
        getSrcUser(hashedEmail);
        backBtn = (Button)findViewById(R.id.back_btn);
        srcPetName = getSrcPetName();
        isDataReady = false;
        petList = new ArrayList<>();
        Pet firstPet = new Pet("Example", "","Swipe right to Like\nLeft tp pass","","","","",getSilhouete(),null);
        Pet lastPet = new Pet("That's it", "","You've gone \nLeft tp pass","","","","",getSilhouete(),null);
        petList.add(demo);
        petAdapter = new PetAdapter(this, petList );

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame) ;

        flingContainer.setAdapter(petAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                petList.remove(0);
                makeToast(getApplicationContext(), "count: "+petList.size());
                petAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You petList so have access to the original object.
                //If you want to use it just cast it (String) dataObject
                makeToast(MatchFinderActivity.this, "Left!");
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                makeToast(MatchFinderActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                //petList.add("XML ".concat(String.valueOf(i)));
                petAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                makeToast(MatchFinderActivity.this, "Clicked!");
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
    private void setAdapter(){


    }

    private String getSPEmail(){
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString("email", "errorGettingEmail");
    }
    private String getSrcPetName(){
        Intent intent = getIntent();
        return intent.getStringExtra("petName");

    }
    private void getSrcUser(String hashedEmail){

        DatabaseReference userQuery = usersRef.child(hashedEmail).getRef();
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                try {
                    srcUser = dataSnapshot.getValue(User.class);
                    getSrcPetAndPetList(dataSnapshot, srcPetName);

                } catch (Exception e) {
                    srcUser = null;
                    Toast.makeText(getApplicationContext(), "getSrcUser is null -> Failed to read value." +
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

    private void getSrcPetAndPetList(DataSnapshot snapshot, String petName){
        for(DataSnapshot dataSnapshot: snapshot.child("Pets").getChildren()){
            Pet p = dataSnapshot.getValue(Pet.class);
            if(p.getName().equals(petName)){
                srcPet = p;
                break;
            }
        }
        if(srcPet!= null)
            populatePetListByPreference();
    }
    private void populatePetListByPreference(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                try {
                    for(DataSnapshot userSnapSHot : dataSnapshot.getChildren()){
                        User u = userSnapSHot.getValue(User.class);
                        if(!sha256(u.getEmail()).equals(hashedEmail)){
                            for(DataSnapshot petSnapshot: userSnapSHot.child("Pets").getChildren()){
                                Pet p = petSnapshot.getValue(Pet.class);
                                if(p.getGender().equals(srcPet.getLookingFor()) && p.getType().equals(srcPet.getType()))
                                    petList.add(p);
                            }
                        }
                    }
                    setAdapter();
                    mutex.release();
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
            Pet selectedPet = pets.get(position);
            textViewName.setText(selectedPet.getName());
            textViewInfo.setText("A "+selectedPet.getAge()+" year old "+selectedPet.getGender() +" "+selectedPet.getType()+".\nI'm from the "+selectedPet.getArea()+" area.\nI'm here for the purpose of "+selectedPet.getPurpose());
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
}
