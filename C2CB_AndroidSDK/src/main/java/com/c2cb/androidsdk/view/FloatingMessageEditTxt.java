package com.c2cb.androidsdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.c2cb.androidsdk.R;

public class FloatingMessageEditTxt extends RelativeLayout {

    private TextView floatingLabel;
    private EditText editText;

    private SpannableString spannable;
    private String labelText;
    private String buttonName;
    private int labelColor;
    private boolean isLabelShown;

    public FloatingMessageEditTxt(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public FloatingMessageEditTxt(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FloatingMessageEditTxt(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Inflate the custom layout
        LayoutInflater.from(context).inflate(R.layout.message_edittext, this, true);

        // Bind the views
        floatingLabel = findViewById(R.id.floating_label);
        editText = findViewById(R.id.edit_text);

        // Set default properties
        labelText = "Label";
        labelColor = R.color.edittextColor;

        if (attrs != null) {
            // Get attributes from XML
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.FloatingLabelEditText, 0, 0);

            try {
                labelText = a.getString(R.styleable.FloatingLabelEditText_labelText);
                buttonName = a.getString(R.styleable.FloatingLabelEditText_buttonName);
                labelColor = a.getColor(R.styleable.FloatingLabelEditText_labelColor,getResources().getColor(R.color.edittextColor));
            } finally {
                a.recycle();
            }
        }
        //set color of the text
        floatingLabel.setTextColor(labelColor);
        editText.setTextColor(labelColor);

        // Create a SpannableString from the full text
        spannable = new SpannableString(labelText); // Label Text Define the text with an asterisk

        // Find the index of the asterisk
        int asteriskIndex = labelText.indexOf("*");

        // Apply red color to the asterisk if it exists
        if (asteriskIndex != -1) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), asteriskIndex, asteriskIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        // Set label properties
        floatingLabel.setText(spannable); // Set the spannable text to the TextView
        floatingLabel.setVisibility(INVISIBLE);

        editText.setHint(spannable);
        editText.setHintTextColor(Color.LTGRAY);

        // Add listeners to EditText
        setupEditTextListeners();
    }

    private void setupEditTextListeners() {
        // TextWatcher to manage floating label visibility
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().isEmpty()) {
                    hideFloatingLabel();
                } else {
                    showFloatingLabel();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Focus listener to handle focus changes
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus || !editText.getText().toString().isEmpty()) {
                    showFloatingLabel();
                } else {
                    hideFloatingLabel();
                }
            }
        });
    }

    private void showFloatingLabel() {
        if (!isLabelShown) {
            floatingLabel.setVisibility(VISIBLE);
            editText.setHint("");
            floatingLabel.animate().translationY(-2).scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
            isLabelShown = true;
        }
    }

    private void hideFloatingLabel() {
        if (isLabelShown && editText.getText().toString().isEmpty()) {
            editText.setHint(spannable);
            floatingLabel.animate().translationY(0).scaleX(1f).scaleY(1f).setDuration(200)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            floatingLabel.setVisibility(INVISIBLE);
                        }
                    }).start();
            isLabelShown = false;
        }
    }

    // Additional setters and getters for custom properties
    public void setLabelText(String text) {
        labelText = text;
        floatingLabel.setText(text);
    }

    public void setLabelColor(int color) {
        labelColor = color;
        floatingLabel.setTextColor(color);
    }

    public String getEditText() {
        if (editText != null && !TextUtils.isEmpty(editText.getText().toString())){
            return editText.getText().toString();
        }
        return "";
    }

    public void setCompoundDrawablesWithIntrinsicBounds(int i, int i1, int verified_icon, int i2) {
        editText.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.verified_icon,
                0
        );
    }

    public void addTextChangedListener(TextWatcher textWatcher) {
        editText.addTextChangedListener(textWatcher);
    }

    public void setHeight(int height) {
        // Change the height of the EditText programmatically
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        params.height = dpToPx(height); // Set height in pixels
        editText.setLayoutParams(params);
    }

    // Utility method to convert dp to pixels
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}


