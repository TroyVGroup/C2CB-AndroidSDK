package com.c2cb.androidsdk;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.c2cb.androidsdk.pojo.Modes;

public class MainActivity extends AppCompatActivity {
    ImageView call_icon, msg_icon, email_icon;
    int ALL_PERMISSIONS = 101;
    String channelId = "";
    Modes modes = new Modes();
    C2CVoiceActivity c2cVoiceActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        call_icon = findViewById(R.id.c2c_Call);
        msg_icon = findViewById(R.id.c2c_Msg);
        email_icon = findViewById(R.id.c2c_Email);
        c2cVoiceActivity = new C2CVoiceActivity(MainActivity.this);// Your Activity Name
        c2cVoiceActivity.getModes(channelId, modes, call_icon, msg_icon, email_icon);
        call_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    c2cVoiceActivity.getCallDetails(channelId, modes, C2CConstants.CALL);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.RECORD_AUDIO)) {
                        c2cVoiceActivity.showError("Message", "Allow permission from setting to make call");
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, ALL_PERMISSIONS);
                    }
                }
            }
        });

        msg_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c2cVoiceActivity.getCallDetails(channelId, modes, C2CConstants.SMS);
            }
        });

        email_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c2cVoiceActivity.getCallDetails(channelId, modes, C2CConstants.EMAIL);
            }
        });

        String[] permissions = new String[]{android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
        ActivityCompat.requestPermissions(MainActivity.this, permissions, ALL_PERMISSIONS);

    }
}