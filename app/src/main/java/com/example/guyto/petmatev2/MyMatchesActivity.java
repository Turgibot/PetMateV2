package com.example.guyto.petmatev2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.guyto.petmatev2.Utility.*;

public class MyMatchesActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private Button backBtn;
    private Utility utils;
    private MatchesAdapter matchesAdapter;
    private ArrayList<Match> matches;
    private ListView listView;
    private boolean isDataReady;
    private ProgressBar matchesPB;
    private User srcUser;
    private ArrayList<Pet> srcUserPets, targetPets;
    private ArrayList<User> targetUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        utils = new Utility();
        srcUser = utils.getSPUser(getApplicationContext());
        matchesAdapter = new MatchesAdapter(this, matches);
        listView = (ListView)findViewById(R.id.matches_list_view);
        backBtn = (Button)findViewById(R.id.my_matches_back_btn);
        matchesPB = (ProgressBar)findViewById(R.id.matches_progress_bar);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyPets();
            }
        });
        //listView.setAdapter(matchesAdapter);
        isDataReady = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    synchronized (this){
                       populateMatches();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void goToMyPets(){
        Intent in = new Intent(MyMatchesActivity.this, MyPetsActivity.class);
        startActivity(in);
        finish();
    }

    private void populateMatches(){
       usersRef.child(sha256(srcUser.getEmail())).child(getString(R.string.likes)).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               try {
                   for (DataSnapshot likeSnapShot : dataSnapshot.getChildren()) {
                       srcUser.addToLikes(likeSnapShot.getValue(Like.class));
                   }
               }catch (ExceptionInInitializerError e){
                   srcUser.addToLikes(dataSnapshot.getValue(Like.class));
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
    }

    //--------------------------------------------------------------------------------------------//


    class MatchesAdapter extends ArrayAdapter<Match> {
        Activity context;
        ArrayList<Match> matches;
        private LayoutInflater inflater;


        public MatchesAdapter(Activity context, ArrayList<Match> matches) {

            super(context,R.layout.list_view_matches, matches);
            this.context = context;
            this.matches = matches;
            inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount(){
            return matches.size();
        }

        @Override
        public Match getItem(int position) {
            return matches.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = inflater.inflate(R.layout.item, parent, false);
            TextView textViewName = (TextView)itemView.findViewById(R.id.matched_pet_name);
            TextView textViewInfo = (TextView)itemView.findViewById(R.id.matched_pet_info);
            ImageView profileImageView = (ImageView)itemView.findViewById(R.id.matched_pet_name);
            Button removeBtn = (Button)itemView.findViewById(R.id.remove_like);
            Button smsBtn = (Button)itemView.findViewById(R.id.sms_btn);
            Match match = matches.get(position);
            final User user = match.getUser();
            Pet srcPet = match.getSrcPet();
            final Pet targetPet = match.getTargetPet();
            String likeStr = srcPet.getName()+" likes "+targetPet.getName()+" !!!";
            textViewName.setText(likeStr);
            String info = "A "+targetPet.getAge()+ "year old "+targetPet.getGender()+" "+targetPet.getType();
            textViewInfo.setText(info);
            byte[] imageBytes = Base64.decode(targetPet.getImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profileImageView.setImageBitmap(decodedImage);
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //removeLike(user.getEmail(), targetPet.getName());
                }
            });
            smsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //smsUser(user name , user phone, srcpet name)
                }
            });
            return itemView;
        }

    }



}
