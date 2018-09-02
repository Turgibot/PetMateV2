package com.example.guyto.petmatev2;
import android.content.Context;
import android.widget.Toast;

import java.security.MessageDigest;



public final class Utility {

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

}
