package com.example.guyto.petmatev2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private ProgressBar matchesPB;
    private User srcUser;
    private ArrayList<Pet> srcUserPets, targetPets;
    private ArrayList<User> targetUsers;
    private ImageView noMatches;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_matches);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        utils = new Utility();
        srcUser = utils.getSPUser(getApplicationContext());
        srcUserPets = new ArrayList<>();
        targetPets = new ArrayList<>();
        targetUsers = new ArrayList<>();
        matches = new ArrayList<>();
        listView = (ListView)findViewById(R.id.matches_list_view);
        backBtn = (Button)findViewById(R.id.my_matches_back_btn);
        matchesPB = (ProgressBar)findViewById(R.id.matches_progress_bar);
        noMatches = (ImageView)findViewById(R.id.matches_not_found);
        Drawable progressDrawable = matchesPB.getIndeterminateDrawable().mutate();
        progressDrawable.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        matchesPB.setProgressDrawable(progressDrawable);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyPets();
            }
        });

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
                       Like l = likeSnapShot.getValue(Like.class);
                       if(l.getHasMatch()){
                           srcUser.addToLikes(l);
                       }
                   }
               }catch (ExceptionInInitializerError e){
                   Like l = dataSnapshot.getValue(Like.class);
                   if(l.getHasMatch()){
                       srcUser.addToLikes(l);
                   }
               }
               populateLikeInfo();
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
    }

    private void populateLikeInfo(){
        if(srcUser.getLikes()==null){
            noMatches.setVisibility(View.VISIBLE);
            matchesPB.setVisibility(View.GONE);
            return;
        }
        for(Like like : srcUser.getLikes()){
            String targetEmail = like.getTargetUserEmail();
            String targetPetName = like.getTargetPetName();
            String srcPetName = like.getSrcPetName();
            User targetUser = new User();
            targetUser.setEmail(targetEmail);
            Pet srcPet = new Pet();
            srcPet.setName(srcPetName);
            Pet targetPet = new Pet();
            targetPet.setName(targetPetName);
            Match match = new Match(targetUser, srcPet, targetPet);
            matches.add(match);

        }
        populateSrcUserPet();
    }

    private void populateSrcUserPet(){
        usersRef.child(sha256(srcUser.getEmail())).child(getString(R.string.pets)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    srcUserPets.add(child.getValue(Pet.class));
                }
                populateTargetUserPet();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateTargetUserPet(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for(DataSnapshot userSnapSHot : dataSnapshot.getChildren()){
                        User u = userSnapSHot.getValue(User.class);
                        if(isEmailInLikes(u.getEmail())){
                            ArrayList<Pet> userPetList = new ArrayList();
                            for(DataSnapshot petSnapshot: userSnapSHot.child("Pets").getChildren()){
                                Pet p = petSnapshot.getValue(Pet.class);
                                if(isNameInLikes(u.getEmail(),p.getName())){
                                    userPetList.add(p);
                                }
                            }
                            if(userPetList.size()>0) {
                                u.setPets(userPetList);
                                targetUsers.add(u);
                            }
                        }
                    }
                    matchesPB.setVisibility(View.GONE);
                    if(targetUsers.size()>0){
                        reorganizeInfo();
                    }else{
                        noMatches.setVisibility(View.VISIBLE);                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "petList is null -> Failed to read value." +
                            e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isEmailInLikes(String email){
        for(Like l : srcUser.getLikes()){
            if(l.getTargetUserEmail().equals(email)){
                return true;
            }
        }
        return false;
    }

    private boolean isNameInLikes(String email, String petName){
        for(Like l : srcUser.getLikes()){
            if(l.getTargetUserEmail().equals(email) && l.getTargetPetName().equals(petName)){
                return true;
            }
        }
        return false;
    }


    private void reorganizeInfo(){
        for(Match match: matches){
            match.setUser(getUserByEmail(match.getUser().getEmail()));
            match.setSrcPet(getSrcPetByName(match.getSrcPet().getName()));
            match.setTargetPet(getTargetPetByName(match.getUser(),match.getTargetPet().getName()));
        }
        matchesAdapter = new MatchesAdapter(this, matches);
        listView.setAdapter(matchesAdapter);

    }

    private User getUserByEmail(String userEmail){
        for(User u : targetUsers){
            if(u.getEmail().equals(userEmail)){
                return u;
            }
        }
        return new User();
    }

    private Pet getSrcPetByName(String petName){
        for(Pet p : srcUserPets){
            if(p.getName().equals(petName)){
                return p;
            }
        }
        return new Pet();
    }

    private Pet getTargetPetByName(User targetUser, String petName){
        for(Pet p : targetUser.getPets()){
            if(p.getName().equals(petName)){
                return p;
            }
        }
        return new Pet();
    }

    private void removeLike(String srcUserEmail, final String targetUserEmail, final String srcPetName, final String targetPetName){
        usersRef.child(sha256(srcUserEmail)).child(getString(R.string.likes)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot likeDataSnapShot: dataSnapshot.getChildren()){
                    Like l = likeDataSnapShot.getValue(Like.class);
                    if(l.getTargetUserEmail().equals(targetUserEmail)&& l.getSrcPetName().equals(srcPetName) && l.getTargetPetName().equals(targetPetName)){
                        likeDataSnapShot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    makeToast(getApplicationContext(), "Error at removeLike");
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeHasMatch(String srcUserEmail, final String targetUserEmail, final String srcPetName, final String targetPetName){
        usersRef.child(sha256(srcUserEmail)).child(getString(R.string.likes)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot likeDataSnapShot: dataSnapshot.getChildren()){
                    Like l = likeDataSnapShot.getValue(Like.class);
                    if(l.getTargetUserEmail().equals(targetUserEmail)&& l.getSrcPetName().equals(srcPetName) && l.getTargetPetName().equals(targetPetName)){
                        likeDataSnapShot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    makeToast(getApplicationContext(), "Error at removeHasMatch");
                                }
                            }
                        });
                        l.setHasMatch(false);
                        likeDataSnapShot.getRef().setValue(l).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    makeToast(getApplicationContext(), "Error at removeHasMatch");
                                }
                            }
                        });

                    }
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
            View itemView = inflater.inflate(R.layout.list_view_matches, parent, false);
            TextView textViewName = (TextView)itemView.findViewById(R.id.matched_pet_name);
            TextView textViewInfo = (TextView)itemView.findViewById(R.id.matched_pet_info);
            ImageView profileImageView = (ImageView)itemView.findViewById(R.id.match_pet_image);
            Button removeBtn = (Button)itemView.findViewById(R.id.remove_like);
            Button smsBtn = (Button)itemView.findViewById(R.id.sms_btn);
            final Match match = matches.get(position);
            final User user = match.getUser();
            final Pet srcPet = match.getSrcPet();
            final Pet targetPet = match.getTargetPet();
            String likeStr = srcPet.getName()+" likes "+targetPet.getName()+" !!!";
            textViewName.setText(likeStr);
            String info = "A "+targetPet.getAge()+ " year old "+targetPet.getGender()+" "+targetPet.getType();
            textViewInfo.setText(info);
            byte[] imageBytes = Base64.decode(targetPet.getImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            profileImageView.setImageBitmap(decodedImage);
            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeLike(srcUser.getEmail(), user.getEmail(), srcPet.getName(), targetPet.getName());
                    removeHasMatch(user.getEmail(), srcUser.getEmail(), targetPet.getName(), srcPet.getName());
                    matches.remove(match);
                    matchesAdapter.notifyDataSetChanged();
                    if(matches.isEmpty()){
                        noMatches.setVisibility(View.VISIBLE);
                    }
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
