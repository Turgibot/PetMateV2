package com.example.guyto.petmatev2;

import java.util.Objects;

public class Like {

    private String targetUserEmail;
    private String targetPetName;
    private String srcPetName;
    private Boolean hasMatch;

    public Like(String targetUserEmail, String targetPetName, String srcPetName , Boolean hasMatch) {
        this.targetUserEmail = targetUserEmail;
        this.targetPetName = targetPetName;
        this.srcPetName = srcPetName;
        this. hasMatch = hasMatch;
    }
    public Like(){

        this.targetUserEmail = "";
        this.targetPetName = "";
        this.srcPetName = "";
        this.hasMatch = null;
    }

    public String getTargetUserEmail() {
        return targetUserEmail;
    }

    public void setTargetUserEmail(String targetUserEmail) {
        this.targetUserEmail = targetUserEmail;
    }

    public String getTargetPetName() {
        return targetPetName;
    }

    public void setTargetPetName(String targetPetName) {
        this.targetPetName = targetPetName;
    }

    public String getSrcPetName() {
        return srcPetName;
    }

    public void setSrcPetName(String srcPetName) {
        this.srcPetName = srcPetName;
    }

    public Boolean getHasMatch() {
        return hasMatch;
    }

    public void setHasMatch(Boolean hasMatch) {
        this.hasMatch = hasMatch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Like)) return false;
        Like like = (Like) o;
        return  this.getTargetUserEmail().equals(like.getTargetUserEmail()) &&
                this.getTargetPetName().equals(like.getTargetPetName()) &&
                this.getSrcPetName().equals(like.getSrcPetName()) &&
                this.getHasMatch().equals(like.getHasMatch());
    }

}
