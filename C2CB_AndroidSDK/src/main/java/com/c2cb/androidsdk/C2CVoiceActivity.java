package com.c2cb.androidsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.c2cb.androidsdk.pojo.C2CAddress;
import com.c2cb.androidsdk.pojo.Country;
import com.c2cb.androidsdk.pojo.InitiateC2C;
import com.c2cb.androidsdk.pojo.Modes;
import com.c2cb.androidsdk.pojo.SuccessC2C;
import com.c2cb.androidsdk.pojo.TokenPojo;
import com.c2cb.androidsdk.pojo.ValidateOTP;
import com.google.gson.Gson;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import kotlin.Unit;

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
        c2CEmbedActivity = new C2CEmbedActivity(this.activity,origin);
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


}
