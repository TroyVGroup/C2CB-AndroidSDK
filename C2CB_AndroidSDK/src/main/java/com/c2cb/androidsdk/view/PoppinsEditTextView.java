package com.c2cb.androidsdk.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.c2cb.androidsdk.R;

public class PoppinsEditTextView extends androidx.appcompat.widget.AppCompatEditText {

    public PoppinsEditTextView(Context context) {
        super(context);
        style(context);
    }

    public PoppinsEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        style(context);
    }

    public PoppinsEditTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        style(context);
    }

    private void style(Context context) {
        Typeface typeface = ResourcesCompat.getFont(context, R.font.poppins_regular);
        setTypeface(typeface);

    }

}

