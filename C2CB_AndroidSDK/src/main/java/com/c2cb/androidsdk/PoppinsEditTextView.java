package com.c2cb.androidsdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;

@SuppressLint("AppCompatCustomView")
public class PoppinsEditTextView extends EditText {

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

