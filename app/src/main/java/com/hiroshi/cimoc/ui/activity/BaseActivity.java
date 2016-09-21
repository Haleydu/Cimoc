package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;

import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initToolbar();
        initProgressBar();
        initPresenter();
        initView();
        initData();
    }

    protected void initTheme() {
        boolean night = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_NIGHT, false);
        setTheme(night ? R.style.AppThemeDark : R.style.AppTheme);
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
    }

    protected View getLayoutView() {
        return null;
    }

    protected String getDefaultTitle() {
        return null;
    }

    protected void initPresenter() {}

    protected void initView() {}

    protected void initData() {}

    protected abstract int getLayoutRes();

    protected void showSnackbar(String msg) {
        View layout = getLayoutView();
        if (layout != null && layout.isShown()) {
            Snackbar.make(layout, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    protected void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

    protected void showSnackbar(int resId, Object... args) {
        showSnackbar(String.format(Locale.CHINA, getString(resId), args));
    }

}
