package com.haleydu.cimoc.ui.widget.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.R;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.ui.fragment.BaseFragment;
import com.haleydu.cimoc.ui.fragment.dialog.SliderDialogFragment;
import com.haleydu.cimoc.ui.widget.Option;

/**
 * Created by Hiroshi on 2017/1/10.
 */

public class SliderPreference extends Option implements View.OnClickListener {

    private PreferenceManager mPreferenceManager;
    private FragmentManager mFragmentManager;
    private Fragment mTargetFragment;
    private String mPreferenceKey;
    private int mMin, mMax;
    private int mTitle;
    private int mValue;
    private int mRequestCode;

    public SliderPreference(Context context) {
        this(context, null);
    }

    public SliderPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.custom_option, this);

        mPreferenceManager = ((App) context.getApplicationContext()).getPreferenceManager();

        initRange(context, attrs);

        setOnClickListener(this);
    }

    private void initRange(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Preference);

        mMin = typedArray.getInt(R.styleable.Preference_min, -1);
        mMax = typedArray.getInt(R.styleable.Preference_max, -1);

        typedArray.recycle();
    }

    @Override
    public void onClick(View v) {
        if (mFragmentManager != null) {
            SliderDialogFragment fragment = SliderDialogFragment.newInstance(mTitle, mMin, mMax, mValue, mRequestCode);
            if (mTargetFragment != null) {
                fragment.setTargetFragment(mTargetFragment, 0);
            }
            fragment.show(mFragmentManager, null);
        }
    }

    public void bindPreference(FragmentManager manager, String key, int def, int title, int request) {
        bindPreference(manager, null, key, def, title, request);
    }

    public void bindPreference(FragmentManager manager, BaseFragment fragment, String key, int def, int title, int request) {
        mFragmentManager = manager;
        mTargetFragment = fragment;
        mPreferenceKey = key;
        mValue = mPreferenceManager.getInt(key, def);
        mTitle = title;
        mRequestCode = request;
        mSummaryView.setText(String.valueOf(mValue));
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mPreferenceManager.putInt(mPreferenceKey, value);
        mValue = value;
        mSummaryView.setText(String.valueOf(mValue));
    }

}

