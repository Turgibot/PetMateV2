package com.example.guyto.petmatev2;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.security.MessageDigest;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.annotations.Nullable;
import io.reactivex.internal.operators.completable.CompletableFromAction;

import static android.content.Context.MODE_PRIVATE;


public class Utility extends AppCompatActivity{

    public static boolean isPureString(String name) {
        char[] chars = name.toCharArray();

        for (char c : chars) {
            if(!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPureNum(String name) {
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean containsDigitAndLetterOnly(String s){
        char[] chars = s.toCharArray();
        boolean isLetter = false, isDigit = false;
        for (char c : chars) {
            if(Character.isLetter(c) && !isLetter) {
                isLetter = true;
            }
            if(Character.isDigit(c) && !isDigit) {
                isDigit = true;
            }
        }
        return isDigit && isLetter;
    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public static void makeToast(Context c, String s){
        Toast.makeText(c, s,
                Toast.LENGTH_LONG).show();
    }

    public String getSPEmail(){
        SharedPreferences sharedPreferences = getSharedPreferences(
                getString(R.string.preference_file_key), MODE_PRIVATE);
        return sharedPreferences.getString("email", "errorGettingEmail");
    }
    public void setSPUser(Context context, User user){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "com.guy.petmatev2.2018", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", user.getFirstName());
        editor.putString("lastName", user.getLastName());
        editor.putString("email", user.getEmail());
        editor.putString("phone", user.getPhone());
        editor.putString("password", user.getPassword());

        editor.apply();
    }
    public User getSPUser(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "com.guy.petmatev2.2018", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");
        String password = sharedPreferences.getString("password", "");
        User user = new User(firstName, lastName, email, password, phone, null, null);
        return user;
    }
    public void setSPPet(Context context, Pet pet){

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "com.guy.petmatev2.2018", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(pet == null){
            editor.putString("name", "");
            editor.putString("purpose", "");
            editor.putString("looking", "");
            editor.putString("gender", "");
            editor.putString("age", "");
            editor.putString("area", "");
            editor.putString("image", "");
            editor.putString("type", "");
        }else{
            editor.putString("name", pet.getName());
            editor.putString("purpose", pet.getPurpose());
            editor.putString("looking", pet.getLookingFor());
            editor.putString("gender", pet.getGender());
            editor.putString("age", pet.getAge());
            editor.putString("area", pet.getArea());
            editor.putString("image", pet.getImage());
            editor.putString("type", pet.getType());
        }
        editor.apply();
    }

    public Pet getSPPet(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                "com.guy.petmatev2.2018", MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String purpose = sharedPreferences.getString("purpose", "");
        String looking = sharedPreferences.getString("looking", "");
        String gender = sharedPreferences.getString("gender", "");
        String area = sharedPreferences.getString("area", "");
        String age = sharedPreferences.getString("age", "");
        String type = sharedPreferences.getString("type", "");
        String image = sharedPreferences.getString("image", "");

        return new Pet(name,age,type,gender,looking,purpose,area,image);
    }


}
