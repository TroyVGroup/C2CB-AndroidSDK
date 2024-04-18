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
