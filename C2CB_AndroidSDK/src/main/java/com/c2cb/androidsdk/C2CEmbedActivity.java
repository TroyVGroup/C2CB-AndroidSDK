package com.c2cb.androidsdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.ConnectOptions;
import com.twilio.voice.Voice;
import com.c2cb.androidsdk.call_back.ButtonCallBack;
import com.c2cb.androidsdk.network.NetworkEventListener;
import com.c2cb.androidsdk.network.NetworkManager;
import com.c2cb.androidsdk.pojo.C2CAddress;
import com.c2cb.androidsdk.pojo.Country;
import com.c2cb.androidsdk.pojo.ImageUploadResponse;
import com.c2cb.androidsdk.pojo.InitiateC2C;
import com.c2cb.androidsdk.pojo.Modes;
import com.c2cb.androidsdk.pojo.SuccessC2C;
import com.c2cb.androidsdk.pojo.TokenPojo;
import com.c2cb.androidsdk.pojo.ValidateOTP;
import com.c2cb.androidsdk.view.FloatingLabelEditText;
import com.c2cb.androidsdk.view.FloatingMessageEditTxt;
import com.c2cb.androidsdk.view.PoppinsBoldTextView;
import com.c2cb.androidsdk.view.PoppinsEditTextView;
import com.c2cb.androidsdk.view.PoppinsNormalTextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import kotlin.Unit;
public class C2CEmbedActivity extends AppCompatActivity {
    private Activity activity;
    private String origin;
    public static final String TAG = "C2C";
    public HashMap<String, String> params = new HashMap<>();
    public Call activeCall;
    public Call.Listener callListener = callListener();
    public AudioSwitch audioSwitch;
    private C2CAddress c2CAddress;
    private int REQUEST_CODE_CAPTURE_IMAGE = 1002;
    private int REQUEST_CODE_PICK_IMAGES = 1001;
    private Uri imageUri;
    private String channelID;
    private TextView previewImageTxt;
    private ImageView icon_verified;
    private String imageName = "", imageFolder ="";
    private  ProgressBar progressBar;
    private boolean isImageUploaded = false;

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
        if (!isOnline()) {
            return;
        }
        this.channelID = channelId;
        getIP();
        new NetworkManager().getModes(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                Modes modes1 = ((Modes) object);
                if (modes1.status == 200 && TextUtils.isEmpty(modes1.getChannel().versionerror)) {
                    modes.setStatus(modes1.status);
                    modes.setMessage(modes1.message);
                    modes.setResponse(modes1.response);
                    modes.setChannel(modes1.channel);
                } else if(!TextUtils.isEmpty(modes1.getChannel().versionerror)){
                    showError("Message", modes1.getChannel().versionerror);
                } else {
                    showError("Error", String.valueOf(modes1.message));
                }

            }

            @Override
            public void OnError(String exception) {
            }
        }, channelId, origin, call_icon, msg_icon, email_icon);
    }

    public void getIP() {
        if (!isOnline()) {
            return;
        }
        new NetworkManager().getDeviceIP(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                if (activity != null) {
                    c2CAddress = (C2CAddress) object;
                }
            }

            @Override
            public void OnError(String exception) {
            }
        });
    }

    public void getCallDetails(@NotNull String channelId, @NotNull Modes modes, @NotNull String id) {


        boolean isVerificationRequired = false;
        if (id == C2CConstants.CALL) {
            isVerificationRequired = modes.channel.preferences.isCallVerificationRequired();
        } else if (id == C2CConstants.EMAIL) {
            isVerificationRequired = modes.channel.preferences.isEmailVerificationRequired();
        } else {
            isVerificationRequired = modes.channel.preferences.isSMSVerificationRequired();
        }


            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.popup_dialog);
            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            ArrayList<String> countries = new ArrayList<>();
            countries.add("Select Country");
            int currentCountry = -1, countNo = 0;
            for (Country country : modes.channel.countries) {
                countries.add(country.code + " " + country.country);
                if (c2CAddress != null) {
                    if (country.country.equalsIgnoreCase(c2CAddress.address.country)) {

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
            if (currentCountry > 0) {
                countrySpinner.setSelection(currentCountry + 1);
            }
            countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                    if (pos > 0) {
                        selectedCountry[0] = countries.get(pos);
                    } else {
                        selectedCountry[0] = "";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    selectedCountry[0] = "";

                }
            });

            TextView titleTextView = dialog.findViewById(R.id.title_txt_view);
            LinearLayout extLayout = dialog.findViewById(R.id.extLayout);
            PoppinsNormalTextView noteTextView = dialog.findViewById(R.id.noteTextView);
            PoppinsNormalTextView context_label = dialog.findViewById(R.id.context_label);
            FloatingLabelEditText firstNameEdittxt = dialog.findViewById(R.id.firstNameEditText);
            FloatingLabelEditText lastNameEdittxt = dialog.findViewById(R.id.lastNameEditText);
            FloatingLabelEditText numberEdittxt = dialog.findViewById(R.id.numberEditText);
            FloatingLabelEditText mobileOTPEditText = dialog.findViewById(R.id.mobileOTPEditText);
            FloatingLabelEditText emailOTPEditText = dialog.findViewById(R.id.emailOTPEditText);
            FloatingLabelEditText emailEditText = dialog.findViewById(R.id.emailEditText);
            PoppinsEditTextView extensionEditTxt = dialog.findViewById(R.id.extensionEditTxt);

            LinearLayout nameLayout = dialog.findViewById(R.id.name_layout);
            LinearLayout numberLayout = dialog.findViewById(R.id.number_layout);
            LinearLayout mobileOTPLayout = dialog.findViewById(R.id.mobileOTPLayout);
            LinearLayout emailLayout = dialog.findViewById(R.id.email_layout);
            LinearLayout emailOTPLayout = dialog.findViewById(R.id.emailOTPLayout);
            LinearLayout detailsLayout = dialog.findViewById(R.id.details_layout);
            LinearLayout messageLayout = dialog.findViewById(R.id.messageLayout);
            LinearLayout subjectLayout = dialog.findViewById(R.id.subjectLayout);
            LinearLayout attachLayout = dialog.findViewById(R.id.attachLayout);
            TextView cancelTextView = dialog.findViewById(R.id.cancelTextView);
            ImageView cancelImgView = dialog.findViewById(R.id.cancelImgView);
            ImageView icon_attach = dialog.findViewById(R.id.icon_attach);
            ImageView icon_camera = dialog.findViewById(R.id.icon_camera);
            icon_verified = dialog.findViewById(R.id.icon_verified);
            CheckBox termsCheckBox = dialog.findViewById(R.id.accept_terms_and_conditions);
            CheckBox extensionChkBox = dialog.findViewById(R.id.extensionChkBox);
            PoppinsBoldTextView connectTxt = dialog.findViewById(R.id.connectTxt);
            FloatingMessageEditTxt messageEditText = dialog.findViewById(R.id.messageEditText);
            FloatingLabelEditText subjectEditText = dialog.findViewById(R.id.subjectEditText);
            TextView termsTextView = dialog.findViewById(R.id.Terms_and_condition_text);
            previewImageTxt = dialog.findViewById(R.id.previewImageTxt);
            previewImageTxt.setVisibility(View.GONE);
            progressBar = dialog.findViewById(R.id.progressBar);
            ImageView poweredByImgView = dialog.findViewById(R.id.poweredByImgView);
            ChipGroup chipGroup = dialog.findViewById(R.id.chip_group);
            TextView count = dialog.findViewById(R.id.count);
            RelativeLayout bubbleLayout = dialog.findViewById(R.id.bubbleLayout);

            setSpannableText(noteTextView,activity.getString(R.string.notes));
            if(modes.channel.preferences.isContextMandatory(id) ){
                setSpannableText(context_label,activity.getString(R.string.context_option)+"*");
            }else {
                setSpannableText(context_label,activity.getString(R.string.context_option));
            }
            if(modes.channel.preferences.isVerifycontact(id) && id == C2CConstants.CALL ){
                extLayout.setVisibility(View.VISIBLE);
            }else {
                extLayout.setVisibility(View.GONE);
            }
            if(modes.channel.preferences.isUploadImageMandatory(id) ){
                setSpannableText(dialog.findViewById(R.id.attach_label),activity.getString(R.string.attach_image)+"*");
            }else {
                setSpannableText(dialog.findViewById(R.id.attach_label),activity.getString(R.string.attach_image));
            }
            if(modes.channel.preferences.isBubbleRequired(id) ){
                bubbleLayout.setVisibility(View.VISIBLE);
            }else {
                bubbleLayout.setVisibility(View.GONE);
            }

            if (id == C2CConstants.CALL) {
                titleTextView.setText(C2CConstants.CALL_Form);
            } else if (id == C2CConstants.EMAIL) {
                titleTextView.setText(C2CConstants.EMAIL_Form);
            } else {
                titleTextView.setText(C2CConstants.SMS_Form);
            }

            icon_attach.setColorFilter(activity.getResources().getColor(R.color.bubbleColor));
            icon_camera.setColorFilter(activity.getResources().getColor(R.color.bubbleColor));
            messageEditText.setHeight(120);

            if (id == C2CConstants.SMS) {
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

        extensionChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Checkbox is checked
                    extensionEditTxt.setFocusable(true);
                    extensionEditTxt.setFocusableInTouchMode(true);
                    extensionEditTxt.setCursorVisible(true);

//                    Toast.makeText(getApplicationContext(), "Checked!", Toast.LENGTH_SHORT).show();
                } else {
                    // Checkbox is unchecked
                    extensionEditTxt.setFocusable(false);
                    extensionEditTxt.setFocusableInTouchMode(false);
                    extensionEditTxt.setCursorVisible(false); // Hides the cursor
//                    Toast.makeText(getApplicationContext(), "Unchecked!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        extensionEditTxt.setFocusable(false);
        extensionEditTxt.setFocusableInTouchMode(false);
        extensionEditTxt.setCursorVisible(false); // Hides the cursor

        poweredByImgView.setOnClickListener(view -> {
            Dialog dialog1 = new Dialog(activity);
            dialog1.setContentView(R.layout.web_view_popup);
            Window window1 = dialog1.getWindow();
            window1.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);

            WebView webView = dialog1.findViewById(R.id.webview);
            ProgressBar progressBarWebView = dialog1.findViewById(R.id.progressBar);
            TextView cancelTextView1 = dialog1.findViewById(R.id.cancelTextView);

            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

            webView.setWebChromeClient(new WebChromeClient() {
                public void onProgressChanged(WebView view, int progress) {
                    progressBarWebView.setVisibility(View.VISIBLE);
                    if (progress == 100) {
                        progressBarWebView.setVisibility(View.GONE);
                    }
                }
            });
            webView.loadUrl("https://contexttocall.com/");
            cancelTextView1.setOnClickListener(view1 ->
                    dialog1.cancel());
            dialog1.show();

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
                            if (progress == 100) {
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
                    ds.setColor(activity.getResources().getColor(R.color.bubbleColor));
                }

            };
            ss.setSpan(clickableSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ss.setSpan(boldSpan, 15, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            termsTextView.setText(ss);
            termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
            termsTextView.setHighlightColor(Color.TRANSPARENT);

            connectTxt.setOnClickListener(view -> {

                if (progressBar.getVisibility() == View.VISIBLE){
                    showError("Message", "Please wait");
                }
                else if (termsCheckBox.isChecked()) {
                    if (modes.channel.preferences.isName(id) && TextUtils.isEmpty(firstNameEdittxt.getEditText())) {
                        showError("Message", "Enter first name.");
                    } else if (modes.channel.preferences.isName(id) && TextUtils.isEmpty(lastNameEdittxt.getEditText())) {
                        showError("Message", "Enter last name.");
                    } else if (modes.channel.preferences.isContact(id) && TextUtils.isEmpty(numberEdittxt.getEditText())) {
                        showError("Message", "Enter valid contact number.");
                    } else if (modes.channel.preferences.isEmail(id) && TextUtils.isEmpty(emailEditText.getEditText())) {
                        showError("Message", "Please enter email address.");
                    } else if (modes.channel.preferences.isEmail(id) && !isValidString(emailEditText.getEditText())) {
                        showError("Message", "Please enter valid email address.");
                    }  else if (modes.channel.preferences.isEmail(id) && modes.channel.preferences.isVerifyemail(id) && emailOTPEditText.getTag().toString().equals("false")) {
                        showError("Message", "Please enter Email OTP.");
                    } else if (modes.channel.preferences.isContact(id) && modes.channel.preferences.isVerifycontact(id) && mobileOTPEditText.getTag().toString().equals("false")) {
                        showError("Message", "Please enter Contact number OTP.");
                    } else if(modes.channel.preferences.isSubjectRequired(id) && TextUtils.isEmpty(subjectEditText.getEditText()) ){
                        showError("Message", "Please enter subject.");
                    }else if( modes.channel.preferences.isContextMandatory(id)  && !isChipSelected(chipGroup)){
                        showError("Message", "Please select context.");
                    }else if( modes.channel.preferences.isUploadImageMandatory(id) && TextUtils.isEmpty(imageName)){
                        showError("Message", "Please attach image.");
                    } else if (modes.channel.preferences.isMessage(id) && TextUtils.isEmpty(messageEditText.getEditText())) {
                        showError("Message", "Please enter message here.");
                    }else {
                        InitiateC2C initiateC2C = new InitiateC2C();
                        if (activity != null) {
                            C2C_Location locationTrack = new C2C_Location(activity);
                            if (locationTrack.canGetLocation()) {
                                double longitude = locationTrack.getLongitude();
                                double latitude = locationTrack.getLatitude();
                                if (latitude != 0.0 && longitude != 0.0) {
                                    initiateC2C.setLatLong(latitude + "," + longitude);
                                } else if (c2CAddress != null) {
                                    setLatlong(initiateC2C);
                                }
                            } else if (c2CAddress != null) {
                                setLatlong(initiateC2C);
                            }
                        }
                        initiateC2C.setChannelId(channelId);
                        initiateC2C.setName(firstNameEdittxt.getEditText() + " " + lastNameEdittxt.getEditText());
                        initiateC2C.setFname(firstNameEdittxt.getEditText());
                        StringBuilder selectedChips = new StringBuilder("");


                        // Iterate through all chips in the ChipGroup
                        for (int i = 0; i < chipGroup.getChildCount(); i++) {
                            Chip chip = (Chip) chipGroup.getChildAt(i);
                            if (chip.isChecked()) {
                                if (!TextUtils.isEmpty(selectedChips)){
                                    selectedChips.append("| ");
                                }
                                selectedChips.append(chip.getText());

                            }
                        }
                        if (!TextUtils.isEmpty(subjectEditText.getEditText())){
                            if (!TextUtils.isEmpty(selectedChips)){
                                selectedChips.append("| ").append(subjectEditText.getEditText());
                            }else {
                                selectedChips.append(subjectEditText.getEditText());
                            }
                        }

                        initiateC2C.setSubject(selectedChips.toString());

                        initiateC2C.setImageFolder(imageFolder);
                        initiateC2C.setImageName(imageName);
                        initiateC2C.setLname(lastNameEdittxt.getEditText());
                        initiateC2C.setNumotp(mobileOTPEditText.getEditText());
                        initiateC2C.setMailotp(emailOTPEditText.getEditText());

                        initiateC2C.setNumber(numberEdittxt.getEditText());
                        for (Country country : modes.channel.countries) {
                            if ((country.code + " " + country.country).contentEquals(selectedCountry[0])) {
                                initiateC2C.setCountrycode(country.code);
                                break;
                            }
                        }

                        if (id.equals(C2CConstants.CALL)) {
                            if (!TextUtils.isEmpty(extensionEditTxt.getText().toString())){
                                initiateC2C.setExtension(extensionEditTxt.getText().toString());
                            }
                            initiateC2C.setEmail(emailEditText.getEditText());
                            initiateC2C.setMessage(messageEditText.getEditText());
                            initiateCall(initiateC2C, dialog, progressBar);
                        } else {

                            if (id.equals(C2CConstants.SMS)) {
                                initiateC2C.setEmail(emailEditText.getEditText());
                                initiateC2C.setMessage(messageEditText.getEditText());
                                sendMessage(initiateC2C, dialog, progressBar);
                            } else if (id.equals(C2CConstants.EMAIL)) {
                                initiateC2C.setMessage(messageEditText.getEditText());
                                sendEmail(initiateC2C, dialog, progressBar);
                            }
                        }

                    }
                } else {
                    showError("Message", "Please check and agree the terms & conditions.");
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
            previewImageTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dialog dialog = new Dialog(activity);
                    dialog.setContentView(R.layout.preview_dialog);
                    Window window = dialog.getWindow();
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);
                    ImageView cancelImgView = dialog.findViewById(R.id.cancelImgView);
                    ImageView deleteImgView = dialog.findViewById(R.id.deleteImgView);
                    ImageView selectedImage = dialog.findViewById(R.id.selectedImage);
                    deleteImgView.setColorFilter(activity.getResources().getColor(R.color.white));
                    cancelImgView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    deleteImgView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!TextUtils.isEmpty(imageFolder) && !TextUtils.isEmpty(imageName)){
                                deleteImage(channelId);
                            }

                            dialog.cancel();
                        }
                    });
                    selectedImage.setImageURI(imageUri);
                    dialog.setCancelable(false);
                    dialog.show();

                }
            });
            icon_attach.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activity.startActivityForResult(galleryIntent, REQUEST_CODE_PICK_IMAGES);
                }
            });
            icon_camera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                        imageUri = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                        // Pass the file URI to the intent
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        activity.startActivityForResult(takePictureIntent, REQUEST_CODE_CAPTURE_IMAGE);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
                            validateOTP.setNumber(numberEdittxt.getEditText());
                            validateOTP.setOtp(mobileOTPEditText.getEditText());
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
                            validateOTP.setEmail(emailEditText.getEditText());
                            validateOTP.setOtp(emailOTPEditText.getEditText());

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
            if (id == C2CConstants.CALL){
                messageLayout.setVisibility(modes.channel.preferences.isMessage(id) ? View.VISIBLE : View.GONE);
            }
            numberEdittxt.setInputType();
            extensionEditTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
            numberEdittxt.setButtonVisibility(modes.channel.preferences.isVerifycontact(id),"Verify", new ButtonCallBack() {

                @Override
                public void onCLick(TextView txtView) {
                    if (numberEdittxt.getEditText().isEmpty()) {
                        showError("Message", "Enter valid contact number.");
                    } else {
                        for (Country country : modes.channel.countries) {
                            if ((country.code + " " + country.country).contentEquals(selectedCountry[0])) {
                                getSMSOTP(
                                        channelId,
                                        country.code,
                                        numberEdittxt.getEditText(),
                                        mobileOTPLayout, txtView
                                );
                                break;
                            }
                        }

                    }
                }
            });
            emailEditText.setButtonVisibility(modes.channel.preferences.isVerifyemail(id),"Verify", new ButtonCallBack() {
                @Override
                public void onCLick(TextView txtView) {
                    if (TextUtils.isEmpty(emailEditText.getEditText())) {
                        showError("Message", "Please enter email address.");
                    } else if (!isValidString(emailEditText.getEditText())) {
                        showError("Message", "Please enter valid email address.");
                    } else {
                        getEmailOTP(channelId, emailEditText.getEditText(), emailOTPLayout, txtView);
                    }
                }


            });
            subjectLayout.setVisibility(modes.channel.preferences.isSubjectRequired(id) ? View.VISIBLE : View.GONE);
            attachLayout.setVisibility(modes.channel.preferences.isUploadImage(id) ? View.VISIBLE : View.GONE);
            chipGroup.setVisibility(modes.channel.preferences.isBubbleRequired(id) ? View.VISIBLE : View.GONE);
            emailOTPEditText.setTag(false);
            mobileOTPEditText.setTag(false);
            if (modes.channel.preferences.isBubbleRequired(id)){
                if (id == C2CConstants.CALL) {
                    for (String item : modes.channel.contexts.callContext) {
                        addChipToGroup(item,chipGroup);
                    }
                }
                else if (id == C2CConstants.EMAIL) {
                    for (String item : modes.channel.contexts.emailContext) {
                        addChipToGroup(item,chipGroup);
                    }
                }
                else if (id == C2CConstants.SMS){
                    for (String item : modes.channel.contexts.smsContext) {
                        addChipToGroup(item,chipGroup);
                    }
                }
                if (modes.channel.preferences.isContextMultiSelect(id)){
                    chipGroup.setSingleSelection(true);
                }else {
                    chipGroup.setSingleSelection(false);
                }
            }

            dialog.setCancelable(false);
            dialog.show();

    }

    private void setSpannableText(PoppinsNormalTextView textView, String notesTxt) {
        // Create a SpannableString from the full text
        SpannableString spannable = new SpannableString(notesTxt); // Label Text Define the text with an asterisk

        // Find the index of the asterisk
        int asteriskIndex = notesTxt.indexOf("*");

        // Apply red color to the asterisk if it exists
        if (asteriskIndex != -1) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), asteriskIndex, asteriskIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spannable);
    }

    private boolean isChipSelected(ChipGroup chipGroup) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                return true;
            }
        }
        return false;
    }


    private void addChipToGroup(String label, ChipGroup chipGroup) {
        // Inflate a new chip from custom layout
        Chip chip = (Chip) LayoutInflater.from(activity).inflate(R.layout.custom_chip, null, false);

        // Set the chip's text (label)
        chip.setText(label);
        chip.setTypeface(null, Typeface.BOLD);
        chipGroup.addView(chip);
    }


    private void setLatlong(InitiateC2C initiateC2C) {
        if (c2CAddress.address.geometry.coordinates.size() > 0) {
            String latLong = c2CAddress.address.geometry.coordinates.get(1) + "," + c2CAddress.address.geometry.coordinates.get(0);
            initiateC2C.setLatLong(latLong);
        }
    }

    private void validateEmailOTP(FloatingLabelEditText emailOTPEditText, ValidateOTP validateOTP) {
        if (!isOnline()) {
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
        }, jsonString, origin);

    }

    private void validateOTP(FloatingLabelEditText mobileOTPEditText, ValidateOTP validateOTP) {
        if (!isOnline()) {
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
        }, jsonString, origin);


    }

    private void getEmailOTP(String channelId, String emailID, LinearLayout emailOTPLayout, TextView verifyEmailOtpTxtView) {
        if (!isOnline()) {
            return;
        }
        new NetworkManager().getOTPForEmail(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    emailOTPLayout.setVisibility(View.VISIBLE);
                    verifyEmailOtpTxtView.setClickable(false);
                    new CountDownTimer(60000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            verifyEmailOtpTxtView.setText("" + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            verifyEmailOtpTxtView.setText("Verify");
                            verifyEmailOtpTxtView.setClickable(true);
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

    private void getSMSOTP(String channelId, String countryCode, String number, LinearLayout mobileOTPLayout, TextView mobileCodeTxtView) {
        if (!isOnline()) {
            return;
        }
        new NetworkManager().getOTPForSMS(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    mobileOTPLayout.setVisibility(View.VISIBLE);
                    mobileCodeTxtView.setClickable(false);
                    new CountDownTimer(60000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            mobileCodeTxtView.setText("" + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            mobileCodeTxtView.setText("Verify");
                            mobileCodeTxtView.setClickable(true);
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
        if (!isOnline()) {
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
        if (!isOnline()) {
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
    ImageView muteActionFab;
    ImageView hangUpActionFab;
    ImageView dialPadFab;
    Dialog callConnectedDialog;

    private void initiateCall(InitiateC2C initiateC2C, Dialog dialogDismiss, ProgressBar progressBar) {
        if (!isOnline()) {
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
        }, jsonString, activity, origin, initiateC2C.getLatLong());

    }

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
            applyFabState(holdActionFab, hold, context, 2);
        }
    }

    public void mute(Context context, ImageView muteActionFab) {
        if (activeCall != null) {
            boolean mute = !activeCall.isMuted();
            activeCall.mute(mute);
            if (mute) {
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.icon_mic));
            } else {
                muteActionFab.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.mic_mute));
            }
            applyFabState(muteActionFab, mute, context, 1);
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

    public void startCall(String id, String mobileNumber, String token, Context context) {
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
                if (chronometer != null) {
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                }
                if (hangUpActionFab != null) {
                    hangUpActionFab.setEnabled(true);
                }
                if (dialPadFab != null) {
                    dialPadFab.setEnabled(true);
                }
                if (holdActionFab != null) {
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
                if (callConnectedDialog != null && callConnectedDialog.isShowing()) {
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
                if (callConnectedDialog != null && callConnectedDialog.isShowing()) {
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

    public void handleActivityResult(int requestCode, int resultCode, Intent data)  {
        if (requestCode == REQUEST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            uploadImage(this.imageUri,activity, origin,channelID);
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGES && resultCode == RESULT_OK) {
                if (data.getData() != null) {
                // Single image selected
                this.imageUri = data.getData();
                uploadImage(this.imageUri,activity, origin,channelID);
            }
        }
    }

    private void uploadImage(Uri imageUri, Activity activity, String origin, String channelId) {
        isImageUploaded = false;
        icon_verified.setVisibility(View.GONE);
        previewImageTxt.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new NetworkManager().uploadImageToServer(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                ImageUploadResponse imageUploadResponse = ((ImageUploadResponse) object);
                if (imageUploadResponse.getStatus() == 200) {
                    imageName = imageUploadResponse.getImageName();
                    imageFolder = imageUploadResponse.getImageFolder();
                    isImageUploaded = true;
                    icon_verified.setVisibility(View.VISIBLE);
                    previewImageTxt.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    showError("Error", String.valueOf(imageUploadResponse.getMessage()));
                }
            }

            @Override
            public void OnError(String exception) {
                showError("Error", "Something went wrong.");

            }
        },imageUri,activity, origin,channelId,imageName,imageFolder);

    }

    private void deleteImage(String channelId) {
        if (!isOnline()) {
            return;
        }

        new NetworkManager().deleteImage(new NetworkEventListener() {
            @Override
            public void OnSuccess(Object object) {
                SuccessC2C successC2C = ((SuccessC2C) object);
                if (successC2C.status == 200) {
                    isImageUploaded = false;
                    imageFolder = "";
                    imageName = "";
                    imageUri = null;
                    icon_verified.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    previewImageTxt.setVisibility(View.GONE);
                } else {
                    showError("Error", String.valueOf(successC2C.message));
                }
            }

            @Override
            public void OnError(String exception) {
            }
        }, channelId,imageFolder,imageName, origin);


    }


}
