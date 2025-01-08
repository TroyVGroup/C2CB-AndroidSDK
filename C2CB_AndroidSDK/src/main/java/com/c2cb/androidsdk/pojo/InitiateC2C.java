package com.c2cb.androidsdk.pojo;

import android.text.TextUtils;

public class InitiateC2C {
    private String channelId;
    private String email;
    private String message;
    private String number;
    private String name;
    private String numotp;
    private String mailotp;
    private String countrycode;
    private String latLong;
    private String extension;


    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    private String fname;
    private String lname;
    private String imageFolder;

    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    private String imageName;


    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    private String subject;


//    {
//        "channelId": "string",
//            "countrycode": "string",
//            "email": "string",
//            "fname": "string",
//            "imageFolder": "string",
//            "imageName": "string",
//            "lname": "string",
//            "mailotp": "string",
//            "message": "string",
//            "name": "string",
//            "number": "string",
//            "numotp": "string",
//            "saveContact": true,
//            "subject": "string",
//            "userId": "string"
//    }
    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        message = message.replace("%", "%25");
        this.message = message;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumotp() {
        return numotp;
    }

    public void setNumotp(String numotp) {
        this.numotp = numotp;
    }

    public String getMailotp() {
        return mailotp;
    }

    public void setMailotp(String mailotp) {
        this.mailotp = mailotp;
    }

    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public String getLatLong() {
        if (TextUtils.isEmpty(latLong)){
            return "";
        }
        return latLong;
    }

    public void setLatLong(String latLong) {
        this.latLong = latLong;
    }
}
