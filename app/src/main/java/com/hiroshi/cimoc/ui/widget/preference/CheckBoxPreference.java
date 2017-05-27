package com.hiroshi.cimoc.ui.widget.preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2017/1/10.
 */

public class CheckBoxPreference extends FrameLayout implements View.OnClickListener {

    private AppCompatCheckBox mCheckBox;
    private PreferenceManager mPreferenceManager;
    private String mPreferenceKey;

    public CheckBoxPreference(Context context) {
        this(context, null);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.custom_option_checkbox, this);

        mPreferenceManager = ((App) context.getApplicationContext()).getPreferenceManager();

        init(context, attrs);

        setOnClickListener(this);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Option);
        String title = typedArray.getString(R.styleable.Option_title);
        String summary = typedArray.getString(R.styleable.Option_summary);

        mCheckBox = ButterKnife.findById(this, R.id.custom_option_checkbox);
        TextView titleView = ButterKnife.findById(this, R.id.custom_option_title);
        TextView summaryView = ButterKnife.findById(this, R.id.custom_option_summary);

        titleView.setText(title);
        summaryView.setText(summary);

        typedArray.recycle();
    }

    @Override
    public void onClick(View v) {
        if (mPreferenceKey != null) {
            boolean checked = !mCheckBox.isChecked();
            mCheckBox.setChecked(checked);
            mPreferenceManager.putBoolean(mPreferenceKey, checked);
        }
    }

    public void bindPreference(String key, boolean def) {
        mPreferenceKey = key;
        mCheckBox.setChecked(mPreferenceManager.getBoolean(key, def));
    }

    public void setColorStateList(ColorStateList stateList) {
        mCheckBox.setSupportButtonTintList(stateList);
    }

}
