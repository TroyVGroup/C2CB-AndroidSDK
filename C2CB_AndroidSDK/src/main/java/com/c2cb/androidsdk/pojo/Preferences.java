package com.c2cb.androidsdk.pojo;


import com.c2cb.androidsdk.C2CConstants;

public class Preferences{
//    public boolean verifyemail;

    public boolean isVerifyemail(String id) {
        if(id == C2CConstants.CALL){
            return call_verifyemail;
        }else if(id == C2CConstants.EMAIL){
            return email_verifyemail;
        }else {
            return sms_verifyemail;
        }
    }

    public boolean isContact(String id) {
        if(id == C2CConstants.CALL){
            return call_contact;
        }else if(id == C2CConstants.EMAIL){
            return email_contact;
        }else {
           return sms_contact;
        }
    }

    public boolean isName(String id) {
        if(id == C2CConstants.CALL){
            return call_name;
        }else if(id == C2CConstants.EMAIL){
            return email_name;
        }else {
            return sms_name;
        }
    }

    public boolean isVerifycontact(String id) {
        if(id == C2CConstants.CALL){
            return call_verifycontact;
        }else if(id == C2CConstants.EMAIL){
            return email_verifycontact;
        }else {
            return sms_verifycontact;
        }
    }

    public boolean isMessage(String id) {
        if(id == C2CConstants.CALL){
            return call_message;
        }else if(id == C2CConstants.EMAIL){
            return email_message;
        }else {
            return sms_message;
        }
    }

    public boolean isEmail(String id) {
        if(id == C2CConstants.CALL){
            return call_email;
        }else if(id == C2CConstants.EMAIL){
            return email_email;
        }else {
            return sms_email;
        }
    }

//    public boolean contact;
//    public boolean name;
//    public boolean verifycontact;
//    public boolean message;
//    public boolean email;

    public boolean call_contact;
    public boolean call_verifyemail;
    public boolean call_email;
    public boolean call_message;
    public boolean call_verifycontact;
    public boolean call_name;

    public boolean sms_verifyemail;
    public boolean sms_verifycontact;
    public boolean sms_name;
    public boolean sms_message;
    public boolean sms_contact;
    public boolean sms_email;

    public boolean email_verifycontact;
    public boolean email_email;
    public boolean email_message;
    public boolean email_name;
    public boolean email_contact;
    public boolean email_verifyemail;

    public Boolean isCallVerificationRequired(){
        if (call_contact == false && call_name == false && call_message == false && call_email == false){
            return false;
        }
        return true;
    }

    public Boolean isSMSVerificationRequired(){
        if (sms_contact == false && sms_name == false && sms_message == false && sms_email == false){
            return false;
        }
        return true;
    }
    public Boolean isEmailVerificationRequired(){
        if (email_contact == false && email_name == false && email_message == false && email_email == false){
            return false;
        }
        return true;
    }
}
