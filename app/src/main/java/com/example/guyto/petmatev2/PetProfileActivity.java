package com.example.guyto.petmatev2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

import static com.example.guyto.petmatev2.Utility.isPureString;
import static com.example.guyto.petmatev2.Utility.makeToast;
import static com.example.guyto.petmatev2.Utility.sha256;
//Todo implement delete, name change insert back btn logic , change save btn to add a pet
public class PetProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private ImageView photoView;
    private EditText petName;
    private Button cancelBtn, saveBtn;
    private Spinner typeSpinner, genderSpinner, lookingSpinner, purposeSpinner, areaSpinner, ageSpinner;
    private static final String[] typeOptions = {"Dog", "Cat", "Pig", "Horse", "Donkey", "Other"};
    private static final String[] ageOptions = {"less than a","1", "2", "3", "4", "5", "6", "7", "8", "9","older than 10","older than 20","older than 30","older than 50",};
    private static final String[] genderOptions = {"Male", "Female", "Any"};
    private static final String[] lookingOptions = {"Female", "Male", "Any"};
    private static final String[] purposeOptions = {"Sport", "Breeding", "Fun", "Any"};
    private static final String[] areaOptions = {"Galil", "Golan", "Shfela", "Sharon", "Gush Dan", "Negev"};
    private String selectedType, selectedAge, selectedGender, selectedLooking, selectedPurpose, selectedArea;
    private static final int PERMISIONS = 101;
    private Uri imageUri;
    private boolean isEdit;
    private Pet editablePet;
    private User user;
    private Utility utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_profile);

        utils = new Utility();
        user = utils.getSPUser(getApplicationContext());
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        photoView = (ImageView) findViewById(R.id.photoView);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        lookingSpinner = (Spinner) findViewById(R.id.lookingSpinner);
        purposeSpinner = (Spinner) findViewById(R.id.purposeSpinner);
        areaSpinner = (Spinner) findViewById(R.id.areaSpinner);
        typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
        ageSpinner = (Spinner)findViewById(R.id.ageSpinner);
        cancelBtn = (Button)findViewById(R.id.cancelProfileBtn);
        saveBtn = (Button) findViewById(R.id.saveProfileBtn);
        petName = (EditText)findViewById(R.id.petName);
        isEdit = getIsEdit();


        final ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, typeOptions);
        final ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, genderOptions);
        final ArrayAdapter<String> lookingAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, lookingOptions);
        final ArrayAdapter<String> purposeAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, purposeOptions);
        final ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, areaOptions);
        final ArrayAdapter<String> ageAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, ageOptions);

        setAdapterAndListener(typeAdapter, typeSpinner);
        setAdapterAndListener(genderAdapter, genderSpinner);
        setAdapterAndListener(lookingAdapter, lookingSpinner);
        setAdapterAndListener(purposeAdapter, purposeSpinner);
        setAdapterAndListener(areaAdapter, areaSpinner);
        setAdapterAndListener(ageAdapter, ageSpinner);
        setDefaultValues();


        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(PetProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(PetProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISIONS);
                    }
                    else{
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(PetProfileActivity.this);
                    }
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyPets();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri == null && !isEdit){
                    makeToast(getApplicationContext(), "Please set your pets profile image first!");
                    return;
                }
                getSpinnerValues();
                final String currPetName = petName.getText().toString();
                if(!isTextValid(petName, currPetName)){
                    return;
                }
                if(!currPetName.equals(editablePet.getName())){
                    deletePet();
                }


                String base64Image = getBase64Image();
                Pet pet = new Pet(currPetName, selectedAge, selectedType, selectedGender, selectedLooking, selectedPurpose, selectedArea, base64Image, null);
                String hashedEmail = sha256(user.getEmail());
                usersRef.child(hashedEmail).child("Pets").child(currPetName).setValue(pet).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            makeToast(getApplicationContext(), "successfully updated "+currPetName+" to your pets");
                            goToMyPets();
                        }else{
                            makeToast(getApplicationContext(),"Error when adding pet");
                        }
                    }
                });

            }
        });


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                photoView.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void setAdapterAndListener(final ArrayAdapter<String> adapter, Spinner spinner){
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    private void goToMyPets(){
        Intent intent = new Intent(PetProfileActivity.this, MyPetsActivity.class);
        intent.putExtra("prevActivity", "PetProfileActivity");
        startActivity(intent);
        finish();

    }
    private boolean isTextValid(EditText t, String s){
        if (TextUtils.isEmpty(s)){
            t.setError(getString(R.string.error_field_required));
            return false;
        }
        if(!isPureString(s)) {
            t.setError(getString(R.string.error_invalid_format));
            return false;
        }
        return true;
    }

    private void setDefaultValues(){
        if(isEdit){
            editablePet = utils.getSPPet(getApplicationContext());
            areaSpinner.setSelection(getIndex(editablePet.getArea(), areaOptions));
            ageSpinner.setSelection(getIndex(editablePet.getAge(), ageOptions));
            typeSpinner.setSelection(getIndex(editablePet.getType(), typeOptions));
            genderSpinner.setSelection(getIndex(editablePet.getGender(), genderOptions));
            lookingSpinner.setSelection(getIndex(editablePet.getLookingFor(), lookingOptions));
            purposeSpinner.setSelection(getIndex(editablePet.getPurpose(), purposeOptions));
            areaSpinner.setSelection(getIndex(editablePet.getArea(), areaOptions));
            displayImage(editablePet.getImage());
            petName.setText(editablePet.getName());
        }else {
            photoView.setImageResource(R.drawable.silhouette);
            selectedArea = areaOptions[0];
            selectedGender = genderOptions[0];
            selectedLooking = lookingOptions[0];
            selectedPurpose = purposeOptions[0];
            selectedType = typeOptions[0];
            selectedAge = ageOptions[1];
        }
    }

    private void getSpinnerValues(){
        selectedArea = areaSpinner.getSelectedItem().toString();
        selectedGender = genderSpinner.getSelectedItem().toString();
        selectedLooking = lookingSpinner.getSelectedItem().toString();
        selectedPurpose = purposeSpinner.getSelectedItem().toString();
        selectedType = typeSpinner.getSelectedItem().toString();
        selectedAge = ageSpinner.getSelectedItem().toString();
    }
    private boolean getIsEdit(){
        Intent intent = getIntent();
        Boolean isEdit = intent.getBooleanExtra(getString(R.string.is_edit),false);
        return (isEdit == null)?false:isEdit;
    }
    private int getIndex(String value, final String[] options ){
        int i = 0;
        for(String s: options){
            if(s.equals(value))
                return i;
            i++;
        }
        return -1;
    }
    private void displayImage(String imageStr){
        byte[] imageBytes = Base64.decode(imageStr, Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 100;
        options.outWidth = 200;
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
        photoView.setImageBitmap(decodedImage);
    }
    private String getBase64Image(){
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1; // shrink it down otherwise we will use stupid amounts of memory

        if(isEdit){
            bitmap= ((BitmapDrawable)photoView.getDrawable()).getBitmap();
        }else{
            bitmap = BitmapFactory.decodeFile(imageUri.getPath(), options);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void deletePet(){
        usersRef.child(sha256(user.getEmail())).child("Pets").child(editablePet.getName()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    makeToast(getApplicationContext(), "Error at deletePet");
                }
            }
        });
    }
}
