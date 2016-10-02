package com.hiroshi.cimoc.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/10/2.
 */

public class UpdateSign extends ImageView {

    public UpdateSign(Context context) {
        this(context, null);
    }

    public UpdateSign(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UpdateSign(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDrawable(context, attrs);
    }

    private void initDrawable(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.UpdateSign, 0, 0);
        int color = typedArray.getColor(R.styleable.UpdateSign_sign_color, Color.BLACK);
        typedArray.recycle();

        float density = getResources().getDisplayMetrics().density;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setStroke(dpToPx(1, density), Color.WHITE);
        drawable.setSize(dpToPx(14, density), dpToPx(14, density));

        setBackgroundDrawable(drawable);
    }

    private int dpToPx(float dp, float density) {
        float px = dp * density;
        if (dp > 0 && (px > 0 && px < 1)) {
            px = 1;
        }
        return (int) px;
    }

}
