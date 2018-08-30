package com.example.guyto.petmatev2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.guyto.petmatev2.Utility.isPureString;
import static com.example.guyto.petmatev2.Utility.sha256;

public class PetProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private ImageView photoView;
    private EditText petName;
    private Button cancelBtn, saveBtn;
    private Spinner typeSpinner, genderSpinner, lookingSpinner, purposeSpinner, areaSpinner;
    private static final String[] typeOptions = {"Dog", "Cat", "Pig", "Horse", "Donkey", "Other"};
    private static final String[] genderOptions = {"Male", "Female", "Other"};
    private static final String[] purposeOptions = {"Sport", "Breeding", "Fun", "Other"};
    private static final String[] areaOptions = {"Galil", "Golan", "Shfela", "Sharon", "Gush Dan", "Negev"};
    private String selectedType, selectedGender, selectedLooking, selectedPurpose, selectedArea;
    private static final int PERMISIONS = 101;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_profile);
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference(getString(R.string.users));
        photoView = (ImageView) findViewById(R.id.photoView);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        lookingSpinner = (Spinner) findViewById(R.id.lookingSpinner);
        purposeSpinner = (Spinner) findViewById(R.id.purposeSpinner);
        areaSpinner = (Spinner) findViewById(R.id.areaSpinner);
        typeSpinner = (Spinner)findViewById(R.id.typeSpinner);
        cancelBtn = (Button)findViewById(R.id.cancelProfileBtn);
        saveBtn = (Button) findViewById(R.id.saveProfileBtn);
        petName = (EditText)findViewById(R.id.petName);



        final ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, typeOptions);
        final ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, genderOptions);
        final ArrayAdapter<String> lookingAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, genderOptions);
        final ArrayAdapter<String> purposeAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, purposeOptions);
        final ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(PetProfileActivity.this,
                android.R.layout.simple_spinner_item, areaOptions);

        setAdapterAndListener(typeAdapter, typeSpinner);
        setAdapterAndListener(genderAdapter, genderSpinner);
        setAdapterAndListener(lookingAdapter, lookingSpinner);
        setAdapterAndListener(purposeAdapter, purposeSpinner);
        setAdapterAndListener(areaAdapter, areaSpinner);

        lookingSpinner.setSelection(1);

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
                                .setAspectRatio(3, 2)
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
                String currPetName = petName.getText().toString();
                if(!isTextValid(petName, currPetName)){
                    return;
                }
                Pet pet = new Pet(currPetName, typeSpinner.getSelectedItem().toString(), genderSpinner.getSelectedItem().toString()
                        , lookingSpinner.getSelectedItem().toString(), purposeSpinner.getSelectedItem().toString(), areaSpinner.getSelectedItem().toString());
                //TODO read email from shared preferences and add pet to db
                String hashedEmail = sha256(/*get email from shared mem */);
                usersRef.child(hashedEmail).child("Pets").setValue(pet);


            }
        });
        //String name, String type, String gender, String lookingFor, String purpose, String area)

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                //photoView.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void setAdapterAndListener( final ArrayAdapter<String> adapter, Spinner spinner){
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(PetProfileActivity.this, "selected: "+adapter.getItem(position), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void goToMyPets(){
        Intent intent = new Intent(PetProfileActivity.this, MyPetsActivity.class);
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
}
