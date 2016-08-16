package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.PreferenceMaster;
import com.hiroshi.cimoc.presenter.BasePresenter;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initToolbar();
        initPresenter();
        initView();
        if (getPresenter() != null) {
            getPresenter().onCreate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getPresenter() != null) {
            getPresenter().onDestroy();
        }
    }

    protected void initTheme() {
        boolean nightly = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_NIGHT, false);
        if (nightly) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    protected void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        mToolbar.setTitle(getDefaultTitle());
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected View getLayoutView() {
        return null;
    }

    protected String getDefaultTitle() {
        return null;
    }

    protected BasePresenter getPresenter() {
        return null;
    }

    protected void initPresenter() {}

    protected void initView() {}

    protected abstract int getLayoutRes();

    public void showSnackbar(String msg) {
        View layout = getLayoutView();
        if (layout != null && layout.isShown()) {
            Snackbar.make(layout, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

}
