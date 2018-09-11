package com.example.guyto.petmatev2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.io.ByteArrayOutputStream;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.concurrent.Semaphore;

import static com.example.guyto.petmatev2.Utility.makeToast;
import static com.example.guyto.petmatev2.Utility.sha256;


public class MatchFinderActivity extends Activity{


    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private ArrayList<Pet> petList;
    private PetAdapter petAdapter;
    private String instructionStr, lastStr;
    private Pet srcPet, lastPet, selectedPet, firstPet, viewedPet;
    private User srcUser, firstUser, lastUser, viewedUser;
    private ArrayList<User> userList;
    private Button backBtn;
    private SwipeFlingAdapterView flingContainer;
    private Utility utils;
    private Like targetLike;
    private ProgressBar pb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_finder);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        utils = new Utility();
        targetLike = null;
        instructionStr = "Swipe right to Like\nLeft tp pass";
        lastStr = "That's all folks \nCome again soon to find new mates!!";
        backBtn = (Button)findViewById(R.id.back_btn);
        pb = (ProgressBar)findViewById(R.id.match_finder_pb);
        Drawable progressDrawable = pb.getIndeterminateDrawable().mutate();
        progressDrawable.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        pb.setProgressDrawable(progressDrawable);
        srcUser = utils.getSPUser(getApplicationContext());
        srcPet = utils.getSPPet(getApplicationContext());
        petList = new ArrayList<>();
        userList = new ArrayList<>();
        firstPet = new Pet("Example", "",instructionStr,"","","","",getSilhouete());
        lastPet = new Pet("Yeah!!!", "",lastStr,"","","","",firstPet.getImage());
        firstUser = new User();
        lastUser = new User();
        firstUser.addPet(firstPet);
        lastUser.addPet(lastPet);
        userList.add(firstUser);
        userList.add(lastUser);
        petList.add(firstPet);
        addPrevLikesToSrcUser();
        populatePetListByPreference();
        petAdapter = new PetAdapter(this, userList);
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame) ;
        flingContainer.setVisibility(View.INVISIBLE);
        flingContainer.setAdapter(petAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            //for each swipe do
            @Override
            public void removeFirstObjectInAdapter() {
                viewedUser = userList.get(0);
                viewedPet = viewedUser.getPets().get(0);

                userList.get(0).getPets().remove(0);
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
                //do nothing
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                if(viewedPet.equals(firstPet)|| viewedPet.equals(lastPet)){
                    return;
                }
                Like like = new Like(viewedUser.getEmail(), viewedPet.getName(), srcPet.getName(),false);
                if(matchFound()){
                    if(isInLikes(like)){
                        srcUser.getLikes().remove(like);
                    }
                    like.setHasMatch(true);
                    showAlert();
                    if(!isAlreadyMatched(like, viewedUser)){
                        setMatchAtTarget(viewedUser.getEmail(), viewedPet.getName());
                    }
                }
                if(!isInLikes(like)){
                    srcUser.addToLikes(like);
                    updateLikeInDB();
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {}

            @Override
            public void onScroll(float scrollProgressPercent) {}
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyPets();
            }
        });

    }


    private void populatePetListByPreference(){
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                try {
                    userList.remove(1);
                    for(DataSnapshot userSnapSHot : dataSnapshot.getChildren()){
                        User u = userSnapSHot.getValue(User.class);
                        if(!sha256(u.getEmail()).equals(sha256(srcUser.getEmail()))) {
                            ArrayList<Pet> userPetList = new ArrayList();
                            for (DataSnapshot petSnapshot : userSnapSHot.child("Pets").getChildren()) {
                                Pet p = petSnapshot.getValue(Pet.class);
                                if (!p.getType().equals(srcPet.getType()))
                                    continue;
                                if (p.getGender().equals(srcPet.getLookingFor()) || srcPet.getLookingFor().equals("Any")) {
                                    userPetList.add(p);
                                }
                            }
                            if (userPetList.size() > 0) {
                                DataSnapshot userLikesSnapShot = userSnapSHot.child(getString(R.string.likes));
                                try {
                                    for (DataSnapshot likeSnapShot : userLikesSnapShot.getChildren()) {
                                        Like l = likeSnapShot.getValue(Like.class);
                                        u.addToLikes(l);
                                    }
                                } catch (Exception e) {
                                    Like l = userLikesSnapShot.getValue(Like.class);
                                    u.addToLikes(l);
                                }

                                u.setPets(userPetList);
                                userList.add(u);
                            }
                        }
                    }
                    userList.add(lastUser);
                    flingContainer.setVisibility(View.VISIBLE);
                    pb.setVisibility(View.GONE);

                } catch (Exception e) {
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

    private String getSilhouete(){
        ImageView img = (ImageView)findViewById(R.id.demoView);
        Bitmap bitmap= ((BitmapDrawable)img.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void goToMyPets(){
        updateSrcUserLikes();
        Intent intent = new Intent(MatchFinderActivity.this, MyPetsActivity.class);
        startActivity(intent);
        finish();
    }
    private void goToMyMatches(){
        updateSrcUserLikes();
        Intent intent = new Intent(MatchFinderActivity.this, MyMatchesActivity.class);
        startActivity(intent);
        finish();
    }


    private void showAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MatchFinderActivity.this);

        builder.setCancelable(true);
        builder.setTitle("Discovery - A Match from heaven");
        builder.setMessage("Would you like to contact the pet owner?");

        builder.setNegativeButton("Maybe later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.setPositiveButton("Sure thing", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToMyMatches();
            }
        });
        builder.show();
    }

    private void updateSrcUserLikes(){
        if(srcUser.getLikes().isEmpty()){
            return;
        }
        usersRef.child(sha256(srcUser.getEmail())).child(getString(R.string.likes))
                .setValue(srcUser.getLikes()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    makeToast(getApplicationContext(), "Error at updateSrcUserMatch");
                }
            }
        });
    }

    private boolean matchFound(){
        if(viewedUser.getLikes() == null){
            return false;
        }
        for(Like viewedLike : viewedUser.getLikes()){
            if(viewedLike.getSrcPetName().equals(viewedPet.getName())&& viewedLike.getTargetPetName().equals(srcPet.getName()) && viewedLike.getTargetUserEmail().equals(srcUser.getEmail())){
                return true;
            }
        }
        return false;
    }

    private void setMatchAtTarget(final String targetEmail, final String targetPetName){
        usersRef.child(sha256(targetEmail)).child(getString(R.string.likes)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    for(DataSnapshot likeSnapShot: dataSnapshot.getChildren()){
                        Like like = likeSnapShot.getValue(Like.class);
                        if(like.getTargetUserEmail().equals(srcUser.getEmail())&& like.getTargetPetName().equals(srcPet.getName()) && like.getSrcPetName().equals(targetPetName)){
                            likeSnapShot.getRef().removeValue();
                            like.setHasMatch(true);
                            likeSnapShot.getRef().setValue(like);
                            return;
                        }                    }
                }catch (Exception e){
                    Like like = dataSnapshot.getValue(Like.class);
                    if(like.getTargetUserEmail().equals(srcUser.getEmail())&& like.getTargetPetName().equals(srcPet.getName()) && like.getSrcPetName().equals(targetPetName)){
                        dataSnapshot.getRef().removeValue();
                        like.setHasMatch(true);
                        dataSnapshot.getRef().setValue(like);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                makeToast(getApplicationContext(), "Error at removeLike"+ databaseError.getMessage());
            }
        });

    }


    private void updateLikeInDB(){
        usersRef.child(sha256(srcUser.getEmail())).child(getString(R.string.likes)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    makeToast(getApplicationContext(), "Error at updateLikeInDB when removing likes");
                }else{
                    saveLikesAtSrcUser();
                }
            }
        });
    }

    private void saveLikesAtSrcUser(){
        usersRef.child(sha256(srcUser.getEmail())).child(getString(R.string.likes)).setValue(srcUser.getLikes()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    makeToast(getApplicationContext(), "Error at updateLikeInDB when saving likes");
                }
            }
        });
    }

    private void addPrevLikesToSrcUser(){
        usersRef.child(sha256(srcUser.getEmail())).child(getString(R.string.likes)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    for(DataSnapshot likeSnapShot: dataSnapshot.getChildren()){
                        Like l = likeSnapShot.getValue(Like.class);
                        srcUser.addToLikes(l);
                    }
                }catch (Exception e){
                    Like l = dataSnapshot.getValue(Like.class);
                    srcUser.addToLikes(l);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isInLikes(Like like){
        if(srcUser.getLikes() == null){
            return false;
        }
        for(Like userLike : srcUser.getLikes()){
            if(userLike.equals(like)){
                return true;
            }
        }
        return false;
    }

    private boolean isAlreadyMatched(Like srcLike, User targetUser){
        if(targetUser.getLikes()== null || srcUser.getLikes() == null){
            return false;
        }
        Like tempTarget = new Like(srcUser.getEmail(),srcLike.getSrcPetName(),srcLike.getTargetPetName(),true);
        for(Like targetLike : targetUser.getLikes()){
            if(targetLike.equals(tempTarget)){
                return true;
            }
        }
        return false;
    }












//-------------------------------------------------------------------------------------------------------------------------------------------------------------//




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

}
