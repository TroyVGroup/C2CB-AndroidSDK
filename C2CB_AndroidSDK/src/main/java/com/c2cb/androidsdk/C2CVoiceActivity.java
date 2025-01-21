package com.c2cb.androidsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.c2cb.androidsdk.pojo.Modes;

import org.jetbrains.annotations.NotNull;

public class C2CVoiceActivity extends AppCompatActivity {
    private Activity activity;
    private String origin;
    private C2CEmbedActivity c2CEmbedActivity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public C2CVoiceActivity(Activity activity) {
        this.activity = activity;
        try {
            origin = activity.getClass().getPackage().getName();
        }catch (Exception e){
            e.printStackTrace();
        }
//        c2CEmbedActivity = new C2CEmbedActivity(this.activity,origin);
        c2CEmbedActivity = new C2CEmbedActivity(this.activity,"com.vgroup.c2c");
    }

    public void getModes(@NotNull String channelId, @NotNull Modes modes, @org.jetbrains.annotations.Nullable ImageView callIcon, @org.jetbrains.annotations.Nullable ImageView msgIcon, @org.jetbrains.annotations.Nullable ImageView emailIcon) {
        if (c2CEmbedActivity != null){
            c2CEmbedActivity.getModes(channelId, modes, callIcon, msgIcon, emailIcon);
        }
    }

    public void getCallDetails(@NotNull String channelId, @NotNull Modes modes, @NotNull String call) {
        if (c2CEmbedActivity != null){
            c2CEmbedActivity.getCallDetails(channelId, modes, call);
        }
    }

    public void showError(@NotNull String title, @NotNull String message) {
        if (c2CEmbedActivity != null){
            c2CEmbedActivity.showError(title, message);
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, @org.jetbrains.annotations.Nullable Intent data) {
        try {
            c2CEmbedActivity.handleActivityResult(requestCode,resultCode,data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
