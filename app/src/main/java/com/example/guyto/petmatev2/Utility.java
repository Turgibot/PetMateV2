package com.example.guyto.petmatev2;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;

public final class Utility extends AppCompatActivity {
    public static Context c;
    
    @Override
    public void onStart() {
        super.onStart();
        c = getApplicationContext();
    }
    public static void makeToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
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

    public static boolean isTextValid(EditText t, String s){
        if (TextUtils.isEmpty(s)){
            t.setError(c.getString(R.string.error_field_required));
            return false;
        }
        if(!isPureString(s)) {
            t.setError(c.getString(R.string.error_invalid_format));
            return false;
        }
        return true;
    }

    public static boolean isPhoneValid(EditText t, String s){
        if (TextUtils.isEmpty(s)){
            t.setError(c.getString(R.string.error_field_required));
            return false;
        }
        if(!isPureNum(s)){
            t.setError(c.getString(R.string.error_invalid_format));
            return false;
        }
        if(s.length()!=10){
            t.setError(c.getString(R.string.error_invalid_length));
            return false;
        }
        return true;
    }
    public static boolean isPasswordValid(EditText p, String s){

        if (TextUtils.isEmpty(s)){
            p.setError(c.getString(R.string.error_field_required));
            return false;
        }
        if(s.length()<4){
            p.setError(c.getString(R.string.error_invalid_password));
            return false;
        }
        if(!containsDigitAndLetterOnly(s)) {
            p.setError(c.getString(R.string.error_no_legal_password));
            return false;
        }

        return true;
    }

    public static boolean isEmailValid(EditText e, String email) {
        if (TextUtils.isEmpty(email)) {
            e.setError(c.getString(R.string.error_field_required));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            e.setError(c.getString(R.string.error_invalid_email));
            return false;
        }
        return true;
    }


    public static boolean isMatchingPassword(EditText p , String src, String target){
        if(!src.equals(target)){
            p.setError(c.getString(R.string.error_matched_passwords));
            return false;
        }
        return true;
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
    
    
}
