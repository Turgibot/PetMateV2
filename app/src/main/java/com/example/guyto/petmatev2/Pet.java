package com.example.guyto.petmatev2;

public class Pet {
    private String name;
    private String type;
    private String gender;
    private String lookingFor;
    private String purpose;
    private String area;
    private String age;
    private String image;

    public Pet(String name, String age, String type, String gender, String lookingFor, String purpose, String area, String image) {
        this.name = name;
        this.age = age;
        this.type = type;
        this.gender = gender;
        this.lookingFor = lookingFor;
        this.purpose = purpose;
        this.area = area;
        this.image = image;
    }

    public Pet(){
        this.name = "example";
        this.age = "5";
        this.type = "Dog";
        this.gender = "Male";
        this.lookingFor = "Female";
        this.purpose = "Sport";
        this.area = "Sharon";
        this.image = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
