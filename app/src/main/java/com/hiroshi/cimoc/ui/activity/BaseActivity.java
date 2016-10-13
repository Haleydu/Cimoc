package com.hiroshi.cimoc.ui.activity;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected View mNightMask;
    protected Toolbar mToolbar;
    protected ProgressBar mProgressBar;
    protected PreferenceManager mPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = ((CimocApplication) getApplication()).getPreferenceManager();
        initTheme();
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initNight();
        initToolbar();
        initProgressBar();
        initPresenter();
        initView();
        initData(savedInstanceState);
    }

    protected void initTheme() {
        int theme = mPreference.getInt(PreferenceManager.PREF_THEME, ThemeUtils.THEME_BLUE);
        setTheme(ThemeUtils.getThemeById(theme));
    }

    protected void initNight() {
        mNightMask = ButterKnife.findById(this, R.id.custom_night_mask);
        boolean night = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
        if (night) {
            mNightMask.setVisibility(View.VISIBLE);
        }
    }

    protected void initToolbar() {
        mToolbar = ButterKnife.findById(this, R.id.custom_toolbar);
        mToolbar.setTitle(getDefaultTitle());
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void initProgressBar() {
        mProgressBar = ButterKnife.findById(this, R.id.custom_progress_bar);
        int resId = ThemeUtils.getResourceId(this, R.attr.colorAccent);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, resId), PorterDuff.Mode.SRC_ATOP);
    }

    protected View getLayoutView() {
        return null;
    }

    protected String getDefaultTitle() {
        return null;
    }

    protected void initPresenter() {}

    protected void initView() {}

    protected void initData(Bundle savedInstanceState) {}

    protected abstract int getLayoutRes();

    protected void showSnackbar(String msg) {
        HintUtils.showSnackbar(getLayoutView(), msg);
    }

    protected void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

    protected void showSnackbar(int resId, Object... args) {
        showSnackbar(StringUtils.format(getString(resId), args));
    }

    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

}
