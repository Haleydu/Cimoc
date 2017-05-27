package com.hiroshi.cimoc.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hiroshi.cimoc.R;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2017/1/11.
 */

public class Option extends FrameLayout {

    protected TextView mTitleView;
    protected TextView mSummaryView;

    public Option(Context context) {
        this(context, null);
    }

    public Option(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Option(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.custom_option, this);

        initText(context, attrs);
    }

    private void initText(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Option);
        String title = typedArray.getString(R.styleable.Option_title);
        String summary = typedArray.getString(R.styleable.Option_summary);

        mTitleView = ButterKnife.findById(this, R.id.custom_option_title);
        mSummaryView = ButterKnife.findById(this, R.id.custom_option_summary);

        mTitleView.setText(title);
        mSummaryView.setText(summary);

        typedArray.recycle();
    }

    public void setSummary(CharSequence text) {
        mSummaryView.setText(text);
    }

}
