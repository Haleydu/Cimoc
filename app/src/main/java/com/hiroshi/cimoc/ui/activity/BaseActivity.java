package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.fragment.dialog.ProgressDialogFragment;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Nullable @BindView(R.id.custom_night_mask) View mNightMask;
    @Nullable @BindView(R.id.custom_toolbar) Toolbar mToolbar;
    protected PreferenceManager mPreference;
    private ProgressDialogFragment mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreference = ((CimocApplication) getApplication()).getPreferenceManager();
        initTheme();
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initNight();
        initToolbar();
        initPresenter();
        mProgressDialog = ProgressDialogFragment.newInstance();
        initView();
        initData();
    }

    protected void initTheme() {
        int theme = mPreference.getInt(PreferenceManager.PREF_THEME, ThemeUtils.THEME_BLUE);
        setTheme(ThemeUtils.getThemeById(theme));
    }

    protected void initNight() {
        if (mNightMask != null) {
            boolean night = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
            if (night) {
                mNightMask.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void initToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(getDefaultTitle());
            setSupportActionBar(mToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
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
        HintUtils.showSnackbar(getLayoutView(), msg);
    }

    protected void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

    protected void showSnackbar(int resId, Object... args) {
        showSnackbar(StringUtils.format(getString(resId), args));
    }

    public void showProgressDialog() {
        mProgressDialog.show(getFragmentManager(), null);
    }

    public void hideProgressDialog() {
        mProgressDialog.dismiss();
    }

}
