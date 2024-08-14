package com.c2cb.androidsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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


public class C2CEmbedActivity  extends AppCompatActivity {
    private Activity activity;
    private String origin;
    public static final String TAG = "C2C";
    public HashMap<String, String> params = new HashMap<>();
    public Call activeCall;
    public Call.Listener callListener = callListener();
    public AudioSwitch audioSwitch;
    private C2CAddress c2CAddress;

    public C2CEmbedActivity(Activity activity, String origin) {
        this.activity = activity;
        this.origin = origin;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        audioSwitch = new AudioSwitch(getApplicationContext());
        startAudioSwitch();
    }

    public boolean isOnline() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void getModes(@NotNull String channelId, final Modes modes, ImageView call_icon, ImageView msg_icon, ImageView email_icon) {
        if (!isOnline()){
            return;
        }

        getIP();
        new NetworkManager().getModes(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                Modes modes1 = ((Modes) object);
                if (modes1.status == 200) {
                    modes.setStatus(modes1.status);
                    modes.setMessage(modes1.message);
                    modes.setResponse(modes1.response);
                    modes.setChannel(modes1.channel);
                } else {
                    showError("Error", String.valueOf(modes1.message));
                }

            }
            @Override
            public void OnError(String exception) {
            }
        }, channelId, origin, call_icon, msg_icon, email_icon);
    }

    public void getIP(){
        if (!isOnline()){
            return;
        }
        new NetworkManager().getDeviceIP(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                if (activity != null){
                    c2CAddress = (C2CAddress) object;
                }
            }
            @Override
            public void OnError(String exception) {
            }
        });
    }

    public void getCallDetails(@NotNull String channelId, @NotNull Modes modes, @NotNull String id) {
//        if (isCellularCallActive){
//            showError("Message", "You can't place a call, if you're already on a phone call.");
//            return;
//        }

        boolean isVerificationRequired = false;
        if(id == C2CConstants.CALL){
            isVerificationRequired = modes.channel.preferences.isCallVerificationRequired();
        }else if(id == C2CConstants.EMAIL){
            isVerificationRequired = modes.channel.preferences.isEmailVerificationRequired();
        }else {
            isVerificationRequired = modes.channel.preferences.isSMSVerificationRequired();
        }

        if (isVerificationRequired) {

            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.popup_dialog);
            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            ArrayList<String> countries = new ArrayList<>();
            countries.add("Select Country");
            int currentCountry = -1,countNo = 0;
            for (Country country : modes.channel.countries) {
                countries.add(country.code + " " + country.country);
                if (c2CAddress != null){
                    if(country.country.equalsIgnoreCase(c2CAddress.address.country)){

                        currentCountry = countNo;
                    }
                }
                countNo++;
            }
            Spinner countrySpinner = dialog.findViewById(R.id.country_spinner);
            ArrayAdapter countryAdapter = new ArrayAdapter(
                    activity, R.layout.c2cspinner,
                    countries
            );

            countryAdapter.setDropDownViewResource(R.layout.c2cspinner_dropdown);
            countrySpinner.setAdapter(countryAdapter);
            final String[] selectedCountry = {""};
            if (currentCountry>0){
                countrySpinner.setSelection(currentCountry+1);
            }
            countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                    if (pos>0){
                        selectedCountry[0] = countries.get(pos);
                    }else {
                        selectedCountry[0] = "";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    selectedCountry[0] = "";

                }
            });

            TextView titleTextView = dialog.findViewById(R.id.title_txt_view);
            EditText firstNameEdittxt = dialog.findViewById(R.id.firstNameEditText);
            EditText lastNameEdittxt = dialog.findViewById(R.id.lastNameEditText);
            EditText numberEdittxt = dialog.findViewById(R.id.numberEditText);
            EditText mobileOTPEditText = dialog.findViewById(R.id.mobileOTPEditText);
            EditText emailOTPEditText = dialog.findViewById(R.id.emailOTPEditText);
            EditText msgEdittxt = dialog.findViewById(R.id.messageEditText);
            EditText emailEditText = dialog.findViewById(R.id.emailEditText);
            Button mobileCodeButton = dialog.findViewById(R.id.get_code_button);
            Button verifyEmailOtpButton =
                    dialog.findViewById(R.id.verifyEmailOtpButton);
            LinearLayout codeLayout = dialog.findViewById(R.id.get_code_layout);
            LinearLayout emailVerifyLayout = dialog.findViewById(R.id.verify_email_layout);
            LinearLayout nameLayout = dialog.findViewById(R.id.name_layout);
            LinearLayout numberLayout = dialog.findViewById(R.id.number_layout);
            LinearLayout mobileOTPLayout = dialog.findViewById(R.id.mobileOTPLayout);
            LinearLayout emailLayout = dialog.findViewById(R.id.email_layout);
            LinearLayout emailOTPLayout = dialog.findViewById(R.id.emailOTPLayout);
            LinearLayout detailsLayout = dialog.findViewById(R.id.details_layout);
            LinearLayout messageLayout = dialog.findViewById(R.id.messageLayout);
            TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);
            ImageView cancelImgView = dialog.findViewById(R.id.cancelImgView);
            CheckBox termsCheckBox = dialog.findViewById(R.id.accept_terms_and_conditions);
            Button connectButton = dialog.findViewById(R.id.connectButton);
            EditText messageEditText = dialog.findViewById(R.id.messageEditText);
            TextView termsTextView = dialog.findViewById(R.id.Terms_and_condition_text);
            ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
            TextView poweredByTextView = dialog.findViewById(R.id.poweredByTextView);
            TextView count = dialog.findViewById(R.id.count);
            titleTextView.setText(id);

            if(id == C2CConstants.SMS){
                count.setVisibility(View.VISIBLE);
                messageEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        count.setText(s.toString().length() + "/160");
                    }
                });
            }

            poweredByTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.web_view_popup);
                    Window window = dialog.getWindow();
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);

                    WebView webView = dialog.findViewById(R.id.webview);
                    ProgressBar progressBarWebView = dialog.findViewById(R.id.progressBar);
                    TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);

                    webView.getSettings().setLoadsImagesAutomatically(true);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

                    webView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            progressBarWebView.setVisibility(View.VISIBLE);
                            if(progress == 100){
                                progressBarWebView.setVisibility(View.GONE);
                            }
                        }
                    });
                    webView.loadUrl("https://contexttocall.com/");
                    cancelTextView.setOnClickListener(view1 ->
                            dialog.cancel());
                    dialog.show();

                }
            });

            SpannableString ss = new SpannableString("I agree to the terms and conditions");

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.web_view_popup);
                    Window window = dialog.getWindow();
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);

                    WebView webView = dialog.findViewById(R.id.webview);
                    ProgressBar progressBarWebView = dialog.findViewById(R.id.progressBar);
                    TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);

                    webView.getSettings().setLoadsImagesAutomatically(true);
                    webView.getSettings().setJavaScriptEnabled(true);
                    WebSettings settings = webView.getSettings();
                    settings.setDomStorageEnabled(true);
                    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    webView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            progressBarWebView.setVisibility(View.VISIBLE);
                            if(progress == 100){
                                progressBarWebView.setVisibility(View.GONE);
                            }
                        }
                    });

                    webView.loadUrl("https://app.contexttocall.com/terms");

                    cancelTextView.setOnClickListener(view1 ->
                            dialog.cancel());
                    dialog.show();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#00a6ff"));
                }

            };
            ss.setSpan(clickableSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ss.setSpan(boldSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            termsTextView.setText(ss);
            termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
            termsTextView.setHighlightColor(Color.TRANSPARENT);

            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (termsCheckBox.isChecked()) {
                        if (modes.channel.preferences.isName(id) && TextUtils.isEmpty(firstNameEdittxt.getText().toString())) {
                            showError("Message", "Enter first name.");
                        } else if (modes.channel.preferences.isName(id) &&TextUtils.isEmpty(lastNameEdittxt.getText().toString())) {
                            showError("Message", "Enter last name.");
                        } else if (modes.channel.preferences.isContact(id) && TextUtils.isEmpty(numberEdittxt.getText().toString())) {
                            showError("Message", "Enter valid contact number.");
                        } else if (modes.channel.preferences.isEmail(id) && TextUtils.isEmpty(emailEditText.getText().toString())) {
                            showError("Message", "Please enter email address.");
                        } else if (modes.channel.preferences.isEmail(id) && !isValidString(emailEditText.getText().toString())) {
                            showError("Message", "Please enter valid email address.");
                        } else if (modes.channel.preferences.isMessage(id) && TextUtils.isEmpty(messageEditText.getText().toString())) {
                            showError("Message", "Please enter message here.");
                        } else if (modes.channel.preferences.isEmail(id) && modes.channel.preferences.isVerifyemail(id) && emailOTPEditText.getTag().toString().equals("false")) {
                            showError("Message", "Please enter Email OTP.");
                        } else if (modes.channel.preferences.isContact(id) && modes.channel.preferences.isVerifycontact(id) && mobileOTPEditText.getTag().toString().equals("false")) {
                            showError("Message", "Please enter Contact number OTP.");
                        } else {
                            InitiateC2C initiateC2C = new InitiateC2C();
                            if (activity !=null){
                                C2C_Location locationTrack = new C2C_Location(activity);
                                if (locationTrack.canGetLocation()) {
                                    double longitude = locationTrack.getLongitude();
                                    double latitude = locationTrack.getLatitude();
                                    if (latitude != 0.0 && longitude != 0.0){
                                        initiateC2C.setLatLong(latitude +","+ longitude);
                                    }else if (c2CAddress != null){
                                        setLatlong(initiateC2C);
                                    }
                                }else if (c2CAddress != null){
                                    setLatlong(initiateC2C);
                                }
                            }
                            initiateC2C.setChannelId(channelId);
                            initiateC2C.setName(firstNameEdittxt.getText().toString() + " " + lastNameEdittxt.getText().toString());

                            initiateC2C.setNumotp(mobileOTPEditText.getText().toString());
                            initiateC2C.setMailotp(emailOTPEditText.getText().toString());

                            initiateC2C.setNumber(numberEdittxt.getText().toString());
                            for (Country country : modes.channel.countries) {
                                if ((country.code + " " + country.country).contentEquals(selectedCountry[0])) {
                                    initiateC2C.setCountrycode(country.code);
                                    break;
                                }
                            }

                            if (id.equals(C2CConstants.CALL)) {
                                initiateC2C.setEmail(emailEditText.getText().toString());
                                initiateC2C.setMessage(messageEditText.getText().toString());
                                initiateCall(initiateC2C, dialog, progressBar);
                            } else {

                                if (id.equals(C2CConstants.SMS)) {
                                    initiateC2C.setEmail(emailEditText.getText().toString());
                                    initiateC2C.setMessage(messageEditText.getText().toString());
                                    sendMessage(initiateC2C, dialog, progressBar);
                                } else if (id.equals(C2CConstants.EMAIL)) {
                                    initiateC2C.setMessage(messageEditText.getText().toString());
                                    sendEmail(initiateC2C, dialog, progressBar);
                                }
                            }

                        }
                    } else {
                        showError("Message", "Please check and agree the terms & conditions.");
                    }
                }
            });

            cancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            cancelImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            mobileCodeButton.setOnClickListener(view -> {
                if (numberEdittxt.getText().toString().isEmpty()) {
                    showError("Message", "Enter valid contact number.");
                } else {
                    for (Country country : modes.channel.countries) {
                        if ((country.code + " " + country.country).contentEquals(selectedCountry[0])) {
                            getSMSOTP(
                                    channelId,
                                    country.code,
                                    numberEdittxt.getText().toString(),
                                    mobileOTPLayout, mobileCodeButton
                            );
                            break;
                        }
                    }

                }
            });

            verifyEmailOtpButton.setOnClickListener(view -> {
                if (TextUtils.isEmpty(emailEditText.getText().toString())) {
                    showError("Message", "Please enter email address.");
                } else if (!isValidString(emailEditText.getText().toString())) {
                    showError("Message", "Please enter valid email address.");
                } else {
                    getEmailOTP(channelId, emailEditText.getText().toString(), emailOTPLayout, verifyEmailOtpButton);
                }
            });

            if (modes.channel.preferences.isContact(id) && modes.channel.preferences.isVerifycontact(id)) {

                mobileOTPEditText.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.length() == 4) {
                            ValidateOTP validateOTP = new ValidateOTP();
                            validateOTP.setChannelId(channelId);
                            validateOTP.setNumber(numberEdittxt.getText().toString());
                            validateOTP.setOtp(mobileOTPEditText.getText().toString());
                            for (Country country : modes.channel.countries) {
                                if ((country.code + " " + country.country).contentEquals(selectedCountry[0])) {
                                    validateOTP.setCountrycode(country.code);
                                    break;
                                }
                            }

                            validateOTP(mobileOTPEditText, validateOTP);
                        } else {
                            mobileOTPEditText.setTag(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }
            if (modes.channel.preferences.isEmail(id) && modes.channel.preferences.isVerifyemail(id)) {
                emailOTPEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (charSequence.length() == 4) {
                            ValidateOTP validateOTP = new ValidateOTP();
                            validateOTP.setChannelId(channelId);
                            validateOTP.setEmail(emailEditText.getText().toString());
                            validateOTP.setOtp(emailOTPEditText.getText().toString());

                            validateEmailOTP(emailOTPEditText, validateOTP);
                        } else {
                            emailOTPEditText.setTag(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

            }
            mobileOTPLayout.setVisibility(View.GONE);
            emailOTPLayout.setVisibility(View.GONE);
            nameLayout.setVisibility(modes.channel.preferences.isName(id) ? View.VISIBLE : View.GONE);
            numberLayout.setVisibility(modes.channel.preferences.isContact(id) ? View.VISIBLE : View.GONE);
            emailLayout.setVisibility(modes.channel.preferences.isEmail(id) ? View.VISIBLE : View.GONE);
            messageLayout.setVisibility(modes.channel.preferences.isMessage(id) ? View.VISIBLE : View.GONE);
            codeLayout.setVisibility(modes.channel.preferences.isVerifycontact(id) ? View.VISIBLE : View.GONE);

            emailVerifyLayout.setVisibility(modes.channel.preferences.isVerifyemail(id) ? View.VISIBLE : View.GONE);

            emailOTPEditText.setTag(false);
            mobileOTPEditText.setTag(false);

            dialog.setCancelable(false);
            dialog.show();
        }
        else {
            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.popup_dialog);
            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            ArrayList<String> countries = new ArrayList<>();

            for (Country country : modes.channel.countries) {
                countries.add(country.code + " " + country.country);
            }

            LinearLayout detailsLayout = dialog.findViewById(R.id.details_layout);
            LinearLayout formLayout = dialog.findViewById(R.id.form_layout);
            if (id.equals(C2CConstants.CALL)) {
                detailsLayout.setVisibility(View.GONE);
            } else {
                formLayout.setVisibility(View.GONE);
            }
            TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);
            CheckBox termsCheckBox = dialog.findViewById(R.id.accept_terms_and_conditions);
            Button connectButton = dialog.findViewById(R.id.connectButton);
            EditText messageEditText = dialog.findViewById(R.id.messageEditText);
            TextView titleTextView = dialog.findViewById(R.id.title_txt_view);
            TextView termsTextView = dialog.findViewById(R.id.Terms_and_condition_text);
            ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
            TextView count = dialog.findViewById(R.id.count);
            if (id.equals(C2CConstants.SMS)) {
                count.setVisibility(View.VISIBLE);
                messageEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        count.setText(s.toString().length() + "/160");
                    }
                });
            }
            titleTextView.setText(id);

            SpannableString ss = new SpannableString("I agree to the terms and conditions");


            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.web_view_popup);
                    Window window = dialog.getWindow();
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);

                    WebView webView = dialog.findViewById(R.id.webview);
                    ProgressBar progressBarWebView = dialog.findViewById(R.id.progressBar);
                    TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);

                    webView.getSettings().setLoadsImagesAutomatically(true);
                    webView.getSettings().setJavaScriptEnabled(true);
                    WebSettings settings = webView.getSettings();
                    settings.setDomStorageEnabled(true);
                    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                    webView.setWebChromeClient(new WebChromeClient() {
                        public void onProgressChanged(WebView view, int progress) {
                            progressBarWebView.setVisibility(View.VISIBLE);
                            if(progress == 100){
                                progressBarWebView.setVisibility(View.GONE);
                            }
                        }
                    });

                    webView.loadUrl("https://app.contexttocall.com/terms");

                    cancelTextView.setOnClickListener(view1 ->
                            dialog.cancel());
                    dialog.show();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.BLUE);
                    ds.isUnderlineText();
                }
            };
            ss.setSpan(clickableSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ss.setSpan(boldSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            termsTextView.setText(ss);
            termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
            termsTextView.setHighlightColor(Color.TRANSPARENT);

            cancelTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            connectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (termsCheckBox.isChecked()) {
                        InitiateC2C initiateC2C = new InitiateC2C();
                        if (activity !=null){
                            C2C_Location locationTrack = new C2C_Location(activity);
                            if (locationTrack.canGetLocation()) {
                                double longitude = locationTrack.getLongitude();
                                double latitude = locationTrack.getLatitude();
                                if (latitude != 0.0 && longitude != 0.0){
                                    initiateC2C.setLatLong(latitude +","+ longitude);
                                }else if (c2CAddress != null){
                                    setLatlong(initiateC2C);
                                }
                            }else if (c2CAddress != null){
                                setLatlong(initiateC2C);
                            }
                        }

                        initiateC2C.setChannelId(channelId);
                        if (id.equals(C2CConstants.CALL)) {
                            initiateCall(initiateC2C, dialog, progressBar);
                        } else {
                            if (TextUtils.isEmpty(messageEditText.getText().toString())) {
                                showError("Message", "Please enter message");
                            } else if (id.equals(C2CConstants.SMS)) {
                                initiateC2C.setMessage(messageEditText.getText().toString());
                                sendMessage(initiateC2C, dialog, progressBar);
                            } else if (id.equals(C2CConstants.EMAIL)) {
                                initiateC2C.setMessage(messageEditText.getText().toString());
                                sendEmail(initiateC2C, dialog, progressBar);
                            }
                        }
                    } else {
                        showError("Message", "Please check and agree the terms & conditions.");
                    }
                }
            });
            dialog.setCancelable(false);
            dialog.show();

        }
    }

    private void setLatlong(InitiateC2C initiateC2C) {
        if (c2CAddress.address.geometry.coordinates.size()>0){
            String latLong= c2CAddress.address.geometry.coordinates.get(1)+","+ c2CAddress.address.geometry.coordinates.get(0);
            initiateC2C.setLatLong(latLong);
        }
    }

    private void validateEmailOTP(EditText emailOTPEditText, ValidateOTP validateOTP) {
        if (!isOnline()){
            return;
        }
        String jsonString = new Gson().toJson(validateOTP);
        new NetworkManager().verifyEmailOTP(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    emailOTPEditText.setTag(true);
                    emailOTPEditText.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.verified_icon,
                            0
                    );
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }
            @Override
            public void OnError(String exception) {
            }
        }, jsonString,origin);

    }

    private void validateOTP(EditText mobileOTPEditText, ValidateOTP validateOTP) {
        if (!isOnline()){
            return;
        }
        String jsonString = new Gson().toJson(validateOTP);
        new NetworkManager().verifyMobileOTP(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    mobileOTPEditText.setTag(true);
                    mobileOTPEditText.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.verified_icon,
                            0
                    );
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }

            @Override
            public void OnError(String exception) {
            }
        }, jsonString,origin);


    }

    private void getEmailOTP(String channelId, String emailID, LinearLayout emailOTPLayout, Button verifyEmailOtpButton) {
        if (!isOnline()){
            return;
        }
        new NetworkManager().getOTPForEmail(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    emailOTPLayout.setVisibility(View.VISIBLE);
                    verifyEmailOtpButton.setClickable(false);
                    new CountDownTimer(60000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            verifyEmailOtpButton.setText("" + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            verifyEmailOtpButton.setText("VERIFY");
                            verifyEmailOtpButton.setClickable(true);
                        }

                    }.start();
                    showError("Success", "verification code sent successfully.");
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }

            @Override
            public void OnError(String exception) {
            }
        }, channelId, emailID, origin);
    }


    private void getSMSOTP(String channelId, String countryCode, String number, LinearLayout mobileOTPLayout, Button mobileCodeButton) {
        if (!isOnline()){
            return;
        }
        new NetworkManager().getOTPForSMS(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    mobileOTPLayout.setVisibility(View.VISIBLE);
                    mobileCodeButton.setClickable(false);
                    new CountDownTimer(60000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            mobileCodeButton.setText("" + millisUntilFinished / 1000);
                        }
                        public void onFinish() {
                            mobileCodeButton.setText("GET CODE");
                            mobileCodeButton.setClickable(true);
                        }

                    }.start();
                    showError("Success", "verification code sent successfully.");
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }
            @Override
            public void OnError(String exception) {
            }
        }, channelId, countryCode, number, origin);
    }

    private void sendEmail(InitiateC2C initiateC2C, Dialog dialog, ProgressBar progressBar) {
        if (!isOnline()){
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String jsonString = new Gson().toJson(initiateC2C);
        new NetworkManager().sendEmail(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    showError("Success", "Email sent successfully");
                    dialog.dismiss();
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }
            @Override
            public void OnError(String exception) {
                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
            }
        }, jsonString, origin, initiateC2C.getLatLong());
    }

    private void sendMessage(InitiateC2C initiateC2C, Dialog dialog, ProgressBar progressBar) {
        if (!isOnline()){
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String jsonString = new Gson().toJson(initiateC2C);
        new NetworkManager().sendSMS(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    showError("Success", "SMS sent successfully");
                    dialog.dismiss();
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }

            @Override
            public void OnError(String exception) {
                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
            }
        }, jsonString, origin, initiateC2C.getLatLong());

    }
    Chronometer chronometer;
    ImageView holdActionFab;
    ImageView muteActionFab ;
    ImageView hangUpActionFab;
    ImageView dialPadFab;
    Dialog callConnectedDialog;
    private void initiateCall(InitiateC2C initiateC2C, Dialog dialogDismiss, ProgressBar progressBar) {
        if (!isOnline()){
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String jsonString = new Gson().toJson(initiateC2C);
        new NetworkManager().initiateCall(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                TokenPojo tokenPojo = ((TokenPojo) object);
                if (tokenPojo.status == 200) {
                    progressBar.setVisibility(View.GONE);
                    dialogDismiss.dismiss();
                    startCall(
                            tokenPojo.token.id,
                            tokenPojo.verified.call,
                            tokenPojo.token.value,
                            activity
                    );
                    callConnectedDialog = new Dialog(activity);
                    callConnectedDialog.setContentView(R.layout.call_connected);
                    chronometer = callConnectedDialog.findViewById(R.id.chronometer);
                    holdActionFab = callConnectedDialog.findViewById(R.id.hold_action_fab);
                    muteActionFab = callConnectedDialog.findViewById(R.id.mute_action_fab);

                    hangUpActionFab = callConnectedDialog.findViewById(R.id.hangup_action_fab);
                    dialPadFab = callConnectedDialog.findViewById(R.id.dialpad_fab);
                    muteActionFab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.icon_mic));
                    LinearLayout dialPadLayout = callConnectedDialog.findViewById(R.id.dialPadLayout);

                    EditText numberEditText = callConnectedDialog.findViewById(R.id.numberEditText);
                    RelativeLayout one = callConnectedDialog.findViewById(R.id.one);
                    RelativeLayout two = callConnectedDialog.findViewById(R.id.two);
                    RelativeLayout three = callConnectedDialog.findViewById(R.id.three);
                    RelativeLayout four = callConnectedDialog.findViewById(R.id.four);
                    RelativeLayout five = callConnectedDialog.findViewById(R.id.five);
                    RelativeLayout six = callConnectedDialog.findViewById(R.id.six);
                    RelativeLayout seven = callConnectedDialog.findViewById(R.id.seven);
                    RelativeLayout eight = callConnectedDialog.findViewById(R.id.eight);
                    RelativeLayout nine = callConnectedDialog.findViewById(R.id.nine);
                    RelativeLayout zero = callConnectedDialog.findViewById(R.id.zero);
                    RelativeLayout asterisk = callConnectedDialog.findViewById(R.id.asterisk);
                    RelativeLayout hash = callConnectedDialog.findViewById(R.id.hash);

                    hangUpActionFab.setEnabled(false);
                    dialPadFab.setEnabled(false);
                    holdActionFab.setEnabled(false);

                    StringBuffer digits = new StringBuffer();

                    one.setOnClickListener(view -> {
                        digits.append("1");
                        numberEditText.setText(digits);
                        sendDigits("1");
//                        sendDigits(digits.toString());
                    });
                    two.setOnClickListener(view -> {
                        digits.append("2");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    three.setOnClickListener(view -> {
                        digits.append("3");
                        numberEditText.setText(digits);
                        sendDigits("3");
//                        sendDigits(digits.toString());
                    });
                    four.setOnClickListener(view -> {
                        digits.append("4");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    five.setOnClickListener(view -> {
                        digits.append("5");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    six.setOnClickListener(view -> {
                        digits.append("6");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    seven.setOnClickListener(view -> {
                        digits.append("7");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    eight.setOnClickListener(view -> {
                        digits.append("8");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    nine.setOnClickListener(view -> {
                        digits.append("9");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    zero.setOnClickListener(view -> {
                        digits.append("0");
                        numberEditText.setText(digits);
                        sendDigits("0");
//                        sendDigits(digits.toString());
                    });
                    asterisk.setOnClickListener(view -> {
                        digits.append("*");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });
                    hash.setOnClickListener(view -> {
                        digits.append("#");
                        numberEditText.setText(digits);
                        sendDigits(digits.toString());
                    });

                    holdActionFab.setOnClickListener(view -> {
                        hold(activity, holdActionFab);
                    });

                    muteActionFab.setOnClickListener(view -> {
                        mute(activity, muteActionFab);
                    });

                    hangUpActionFab.setOnClickListener(view -> {
                        disconnect();
                        callConnectedDialog.dismiss();
                    });
                    dialPadFab.setOnClickListener(view -> {
                        if (dialPadLayout.getVisibility() == View.VISIBLE) {
                            dialPadLayout.setVisibility(View.GONE);
                            digits.delete(0, digits.length());
                            applyFabState(dialPadFab, false, activity, 2);
                        } else {
                            dialPadLayout.setVisibility(View.VISIBLE);
                            applyFabState(dialPadFab, true, activity, 2);
                        }
                    });


                    callConnectedDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    callConnectedDialog.setCancelable(false);
                    callConnectedDialog.show();


                } else {
                    showError("Error", String.valueOf(tokenPojo.message));
                }
            }

            @Override
            public void OnError(String exception) {
                progressBar.setVisibility(View.GONE);
                dialogDismiss.dismiss();
            }
        }, jsonString, activity, origin,initiateC2C.getLatLong());

    }

//    private void getToken(String call, String value, Activity activity) {
//
//    }

    private boolean isValidString(String emailID) {
        if (TextUtils.isEmpty(emailID)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(emailID).matches();
        }
    }

    public void showError(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message)
                .setTitle(title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
    public void disconnect() {
        if (activeCall != null) {
            activeCall.disconnect();
            activeCall = null;
        }
    }

    public void hold(Context context, ImageView holdActionFab) {
        if (activeCall != null) {
            boolean hold = !activeCall.isOnHold();
            activeCall.hold(hold);
            applyFabState(holdActionFab, hold, context,2);
        }
    }

    public void mute(Context context, ImageView muteActionFab) {
        if (activeCall != null) {
            boolean mute = !activeCall.isMuted();
            activeCall.mute(mute);
            if (mute){
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.icon_mic));
            }else {
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.mic_mute));
            }
            applyFabState(muteActionFab, mute, context,1);
        }
    }

    public void sendDigits(String digits) {
        if (activeCall != null) {
            activeCall.sendDigits(digits);
        }
    }

    private void applyFabState(ImageView button, boolean enabled, Context context, int imageItem) {
        ColorStateList colorStateList = enabled ?
                ColorStateList.valueOf(Color.parseColor("#00A6FF")) :
                ColorStateList.valueOf(Color.parseColor("#b0bec5"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            button.setImageTintList(colorStateList);
        }

    }

    public void startCall(String id,String mobileNumber, String token, Context context) {
        if (mobileNumber.isEmpty()) {
            return;
        }
        params.put("To", mobileNumber);
        params.put("From", "+16098065088");
        params.put("Token", id);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .params(params)
                .build();
        activeCall = Voice.connect(context.getApplicationContext(), connectOptions, callListener);
    }

    private void startAudioSwitch() {
        /*
         * Start the audio device selector after the menu is created and update the icon when the
         * selected audio device changes.
         */
        audioSwitch.start((audioDevices, audioDevice) -> {
            Log.d(TAG, "Updating AudioDeviceIcon");
            return Unit.INSTANCE;
        });
    }


    private Call.Listener callListener() {
        return new Call.Listener() {
            /*
             * This callback is emitted once before the Call.Listener.onConnected() callback when
             * the callee is being alerted of a Call. The behavior of this callback is determined by
             * the answerOnBridge flag provided in the Dial verb of your TwiML application
             * associated with this client. If the answerOnBridge flag is false, which is the
             * default, the Call.Listener.onConnected() callback will be emitted immediately after
             * Call.Listener.onRinging(). If the answerOnBridge flag is true, this will cause the
             * call to emit the onConnected callback only after the call is answered.
             * See answeronbridge for more details on how to use it with the Dial TwiML verb. If the
             * twiML response contains a Say verb, then the call will emit the
             * Call.Listener.onConnected callback immediately after Call.Listener.onRinging() is
             * raised, irrespective of the value of answerOnBridge being set to true or false
             */
            @Override
            public void onRinging(@NonNull Call call) {
                Log.d(TAG, "Ringing");
                /*
                 * When [answerOnBridge](https://www.twilio.com/docs/voice/twiml/dial#answeronbridge)
                 * is enabled in the <Dial> TwiML verb, the caller will not hear the ringback while
                 * the call is ringing and awaiting to be accepted on the callee's side. The application
                 * can use the `SoundPoolManager` to play custom audio files between the
                 * `Call.Listener.onRinging()` and the `Call.Listener.onConnected()` callbacks.
                 */
                if(chronometer !=null){
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                }
                if (hangUpActionFab != null){
                    hangUpActionFab.setEnabled(true);
                }
                if (dialPadFab != null){
                    dialPadFab.setEnabled(true);
                }
                if (holdActionFab !=null){
                    holdActionFab.setEnabled(true);
                }
            }

            @Override
            public void onConnectFailure(@NonNull Call call, @NonNull CallException error) {
                if (audioSwitch != null) {
                    audioSwitch.deactivate();
                    SoundPoolManager.getInstance(getApplicationContext()).stopRinging();
                    String message = String.format(
                            Locale.US,
                            "Call Error: %d, %s",
                            error.getErrorCode(),
                            error.getMessage());
                    Log.e(TAG, message);
                }
//                Toast.makeText(activity, "Connect failure", Toast.LENGTH_SHORT).show();
                if (callConnectedDialog != null && callConnectedDialog.isShowing()){
                    callConnectedDialog.dismiss();
                }
            }

            @Override
            public void onConnected(@NonNull Call call) {
                if (audioSwitch != null) {
                    audioSwitch.activate();
                    SoundPoolManager.getInstance(getApplicationContext()).stopRinging();
                    activeCall = call;
                }
                Log.d(TAG, "Connected");
//                Toast.makeText(activity, "Connected", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onReconnecting(@NonNull Call call, @NonNull CallException callException) {
                Log.d(TAG, "onReconnecting");
            }
            @Override
            public void onReconnected(@NonNull Call call) {
                Log.d(TAG, "onReconnected");
            }

            @Override
            public void onDisconnected(@NonNull Call call, CallException error) {
                if (audioSwitch != null) {
                    audioSwitch.deactivate();
                    SoundPoolManager.getInstance(getApplicationContext()).stopRinging();
                    Log.d(TAG, "Disconnected");
                    if (error != null) {
                        String message = String.format(
                                Locale.US,
                                "Call Error: %d, %s",
                                error.getErrorCode(),
                                error.getMessage());
                        Log.e(TAG, message);
                    }
                }
//                Toast.makeText(activity, "Disconnected", Toast.LENGTH_SHORT).show();
                if (callConnectedDialog != null && callConnectedDialog.isShowing()){
                    callConnectedDialog.dismiss();
                }
            }

            /*
             * currentWarnings: existing quality warnings that have not been cleared yet
             * previousWarnings: last set of warnings prior to receiving this callback
             *
             * Example:
             *   - currentWarnings: { A, B }
             *   - previousWarnings: { B, C }
             *
             * Newly raised warnings = currentWarnings - intersection = { A }
             * Newly cleared warnings = previousWarnings - intersection = { C }
             */
            public void onCallQualityWarningsChanged(@NonNull Call call,
                                                     @NonNull Set<Call.CallQualityWarning> currentWarnings,
                                                     @NonNull Set<Call.CallQualityWarning> previousWarnings) {

                if (previousWarnings.size() > 1) {
                    Set<Call.CallQualityWarning> intersection = new HashSet<>(currentWarnings);
                    currentWarnings.removeAll(previousWarnings);
                    intersection.retainAll(previousWarnings);
                    previousWarnings.removeAll(intersection);
                }

                String message = String.format(
                        Locale.US,
                        "Newly raised warnings: " + currentWarnings + " Clear warnings " + previousWarnings);
                Log.e(TAG, message);
            }
        };
    }

}
