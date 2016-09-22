package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.utils.HintUtils;

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
        HintUtils.showSnackBar(getLayoutView(), msg);
    }

    protected void showSnackbar(int resId) {
        HintUtils.showSnackBar(getLayoutView(), getString(resId));
    }

    protected void showSnackbar(int resId, Object... args) {
        HintUtils.showSnackBar(getLayoutView(), getString(resId), args);
    }

}
