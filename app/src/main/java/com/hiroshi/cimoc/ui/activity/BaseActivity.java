package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected View maskView;
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isPortrait()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initNightly();
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
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).removeViewImmediate(maskView);
        if (getPresenter() != null) {
            getPresenter().onDestroy();
        }
    }

    private void initNightly() {
        maskView = new View(this);
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).addView(maskView, getParams());
        boolean nightly = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_NIGHTLY, false);
        if (nightly) {
            maskView.setBackgroundColor(getResources().getColor(R.color.trans_black));
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

    protected WindowManager.LayoutParams getParams() {
        return new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
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

    protected boolean isPortrait() {
        return true;
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
