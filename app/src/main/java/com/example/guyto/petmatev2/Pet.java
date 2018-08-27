package com.example.guyto.petmatev2;

public class Pet {
    private String name;
    private String type;
    private String gender;
    private String lookingFor;
    private String purpose;
    private String area;


    public Pet(String name, String type, String gender, String lookingFor, String purpose, String area) {
        this.name = name;
        this.type = type;
        this.gender = gender;
        this.lookingFor = lookingFor;
        this.purpose = purpose;
        this.area = area;
    }

    public Pet(){
        this.name = "example";
        this.type = "Dog";
        this.gender = "Female";
        this.lookingFor = "Any";
        this.purpose = "Sport";
        this.area = "Sharon";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLookingFor() {
        return lookingFor;
    }

    public void setLookingFor(String lookingFor) {
        this.lookingFor = lookingFor;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
