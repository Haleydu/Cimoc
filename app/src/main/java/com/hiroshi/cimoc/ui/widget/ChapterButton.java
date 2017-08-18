package com.hiroshi.cimoc.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/10/2.
 */

public class ChapterButton extends AppCompatTextView {

    private static final int[] NORMAL_STATE = new int[] { -android.R.attr.state_selected };
    private static final int[] SELECTED_STATE = new int[] { android.R.attr.state_selected };

    private int normalColor;
    private int accentColor;
    private boolean download;

    public ChapterButton(Context context) {
        this(context, null);
    }

    public ChapterButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChapterButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ChapterButton, 0, 0);
        accentColor = typedArray.getColor(R.styleable.ChapterButton_selected_color, Color.BLACK);
        typedArray.recycle();

        normalColor = 0x8A000000;

        setClickable(true);
        download = false;
        initColorDrawableState();
        initDrawableState();
    }

    private void initColorDrawableState() {
        ColorStateList colorStateList = new ColorStateList(new int[][] {NORMAL_STATE, SELECTED_STATE},
                new int[] { normalColor, Color.WHITE });
        setTextColor(colorStateList);
    }

    private void initDrawableState() {
        GradientDrawable normalDrawable = new GradientDrawable();
        normalDrawable.setStroke((int) ViewUtils.dpToPixel(1, getContext()), normalColor);
        normalDrawable.setCornerRadius(ViewUtils.dpToPixel(18, getContext()));
        normalDrawable.setColor(Color.TRANSPARENT);

        GradientDrawable selectedDrawable = new GradientDrawable();
        selectedDrawable.setStroke((int) ViewUtils.dpToPixel(1, getContext()), accentColor);
        selectedDrawable.setCornerRadius(ViewUtils.dpToPixel(18, getContext()));
        selectedDrawable.setColor(accentColor);

        StateListDrawable stateList = new StateListDrawable();
        stateList.addState(NORMAL_STATE, normalDrawable);
        stateList.addState(SELECTED_STATE, selectedDrawable);
        setBackgroundDrawable(stateList);
    }

    public void setDownload(boolean download) {
        if (this.download != download) {
            this.download = download;
            normalColor = download ? accentColor : 0x8A000000;
            initColorDrawableState();
            initDrawableState();
        }
    }

}
