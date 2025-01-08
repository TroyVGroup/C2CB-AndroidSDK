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

    public boolean isSubjectRequired(String id) {
        if(id == C2CConstants.CALL){
            return call_subject;
        }else if(id == C2CConstants.EMAIL){
            return email_subject;
        }else {
            return sms_subject;
        }

    }

   public boolean isUploadImageMandatory(String id) {
        if(id == C2CConstants.CALL){
            return call_image_mandatory;
        }else if(id == C2CConstants.EMAIL){
            return email_image_mandatory;
        }else {
            return sms_image_mandatory;
        }

    }

   public boolean isUploadImage(String id) {
        if(id == C2CConstants.CALL){
            return call_image;
        }else if(id == C2CConstants.EMAIL){
            return email_image;
        }else {
            return sms_image;
        }

    }

    public boolean isContextMandatory(String id) {
        if(id == C2CConstants.CALL){
            return call_context_mandatory;
        }else if(id == C2CConstants.EMAIL){
            return email_context_mandatory;
        }else {
            return sms_context_mandatory;
        }
    }


    public boolean isContextMultiSelect(String id) {
        if(id == C2CConstants.CALL){
            return call_context_multiple;
        }else if(id == C2CConstants.EMAIL){
            return email_context_multiple;
        }else {
            return sms_context_multiple;
        }

    }

    public boolean isBubbleRequired(String id) {
        if(id == C2CConstants.CALL){
            return call_context;
        }else if(id == C2CConstants.EMAIL){
            return email_context;
        }else {
            return sms_context;
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
    public boolean call_contact;
    public boolean call_verifyemail;
    public boolean call_email;
    public boolean call_message;
    public boolean call_verifycontact;
    public boolean call_name;

    public boolean call_image_mandatory;

    public boolean call_subject;

    public boolean call_context;

    public boolean call_context_mandatory;

    public boolean call_context_multiple;

    public boolean call_image;
    public Boolean isCallVerificationRequired(){
        if (call_contact == false && call_name == false && call_message == false && call_email == false && call_image == false && call_context == false && call_subject == false){
            return false;
        }
        return true;
    }

    public boolean sms_verifyemail;
    public boolean sms_verifycontact;
    public boolean sms_name;
    public boolean sms_message;
    public boolean sms_contact;
    public boolean sms_email;

    public boolean sms_image_mandatory;

    public boolean sms_subject;

    public boolean email_context;

    public boolean sms_context_mandatory;

    public boolean sms_context_multiple;

    public boolean sms_image;


    public boolean email_verifycontact;
    public boolean email_email;
    public boolean email_message;
    public boolean email_name;
    public boolean email_contact;
    public boolean email_verifyemail;

    public boolean email_image_mandatory;

    public boolean email_subject;

    public boolean sms_context;

    public boolean email_context_mandatory;

    public boolean email_context_multiple;

    public boolean email_image;



    public Boolean isSMSVerificationRequired(){
        if (sms_contact == false && sms_name == false && sms_message == false && sms_email == false  && sms_image == false && sms_context == false && sms_subject == false){
            return false;
        }
        return true;
    }
    public Boolean isEmailVerificationRequired(){
        if (email_contact == false && email_name == false && email_message == false && email_email == false  && email_image == false && email_context == false && email_subject == false){
            return false;
        }
        return true;
    }

}
