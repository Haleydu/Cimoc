package com.haleydu.cimoc.ui.activity;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.R;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.presenter.BasePresenter;
import com.haleydu.cimoc.ui.fragment.dialog.ProgressDialogFragment;
import com.haleydu.cimoc.ui.view.BaseView;
import com.haleydu.cimoc.ui.widget.ViewUtils;
import com.haleydu.cimoc.utils.HintUtils;
import com.haleydu.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity implements BaseView {

    protected PreferenceManager mPreference;
    @Nullable
    @BindView(R.id.custom_night_mask)
    View mNightMask;
    @Nullable
    @BindView(R.id.custom_toolbar)
    Toolbar mToolbar;
    private ProgressDialogFragment mProgressDialog;
    private BasePresenter mBasePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAdMob();
        mPreference = App.getPreferenceManager();
        initTheme();
        setContentView(getLayoutRes());
        ButterKnife.bind(this);
        initNight();
        initToolbar();
        mBasePresenter = initPresenter();
        mProgressDialog = ProgressDialogFragment.newInstance();
        initView();
        initData();
        initUser();
    }

    @Override
    protected void onDestroy() {
        if (mBasePresenter != null) {
            mBasePresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public App getAppInstance() {
        return (App) getApplication();
    }

    @Override
    public void onNightSwitch() {
        initNight();
    }

    protected void initTheme() {
        int theme = mPreference.getInt(PreferenceManager.PREF_OTHER_THEME, ThemeUtils.THEME_BLUE);
        setTheme(ThemeUtils.getThemeById(theme));
        if (isNavTranslation() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void initNight() {
        if (mNightMask != null) {
            boolean night = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
            int color = mPreference.getInt(PreferenceManager.PREF_OTHER_NIGHT_ALPHA, 0xB0) << 24;
            mNightMask.setBackgroundColor(color);
            mNightMask.setVisibility(night ? View.VISIBLE : View.INVISIBLE);
        }
    }

    protected void initToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(getDefaultTitle());
            setSupportActionBar(mToolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mToolbar.setPadding(0, ViewUtils.getStatusBarHeight(this), 0, mToolbar.getPaddingBottom());
            }
        }
    }

    protected View getLayoutView() {
        return null;
    }

    protected String getDefaultTitle() {
        return null;
    }

    protected BasePresenter initPresenter() {
        return null;
    }

    protected void initView() {
    }

    protected void initData() {
    }

    protected void initUser() {
    }

    protected void initAdMob() {
    }

    protected abstract int getLayoutRes();

    protected boolean isNavTranslation() {
        return false;
    }

    protected void showSnackbar(String msg) {
        HintUtils.showSnackbar(getLayoutView(), msg);
    }

    protected void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

    public void showProgressDialog() {
        mProgressDialog.show(getSupportFragmentManager(), null);
    }

    public void hideProgressDialog() {
        // 可能 onSaveInstanceState 后任务结束，需要取消对话框，直接 dismiss 会抛异常
        mProgressDialog.dismissAllowingStateLoss();
    }

}
