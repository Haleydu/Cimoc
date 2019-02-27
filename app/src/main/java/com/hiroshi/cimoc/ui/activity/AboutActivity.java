package com.hiroshi.cimoc.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.Constants;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Update;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.AboutPresenter;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.ui.view.AboutView;
import com.hiroshi.cimoc.ui.widget.CustomToast;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class AboutActivity extends BackActivity implements AboutView, AdapterView.OnItemSelectedListener {

    @BindView(R.id.about_update_summary)
    TextView mUpdateText;
    @BindView(R.id.about_version_name)
    TextView mVersionName;
    @BindView(R.id.about_layout)
    View mLayoutView;
    @BindView(R.id.about_update_auto_switch)
    SwitchCompat aboutAutoUpdateSwitch;
    @BindView(R.id.about_spinner_source)
    AppCompatSpinner spinner_download_source;

    private AboutPresenter mPresenter;
    private boolean update = false;
    private boolean checking = false;

    private List<String> listSources = new ArrayList<>();

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new AboutPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    // TODO 更新源独立出一个模块？
    @Override
    protected void initView() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            mVersionName.setText(StringUtils.format("version: %s", info.versionName));
            autoUpdateSwitch();

            listSources.add("请选择更新源");
            listSources.add("Gitee源");
            listSources.add("Github源");
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.item_spinner, listSources);
            spinner_download_source.setAdapter(arrayAdapter);
            spinner_download_source.setOnItemSelectedListener(this);
            checkSpinnerSelected(spinner_download_source);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置是否自动检查更新App
    private void autoUpdateSwitch() {
        aboutAutoUpdateSwitch.setChecked(mPreference.getBoolean(PreferenceManager.PREF_UPDATE_APP_AUTO, true));
        aboutAutoUpdateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //存放数据
                mPreference.putBoolean(PreferenceManager.PREF_UPDATE_APP_AUTO, isChecked);
                if (isChecked) {
//                    Toast.makeText(AboutActivity.this, "已开启自动检查App更新", Toast.LENGTH_SHORT).show();
                    CustomToast.showToast(AboutActivity.this, "已开启自动检查App更新", 2000);
                } else {
//                    Toast.makeText(AboutActivity.this, "已关闭自动检查App更新", Toast.LENGTH_SHORT).show();
                    CustomToast.showToast(AboutActivity.this, "已关闭自动检查App更新", 2000);
                }
            }
        });
    }

    @OnClick(R.id.about_support_btn)
    void onSupportClick() {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, getString(R.string.about_support_email)));
        showSnackbar(R.string.about_already_clip);
    }

    @OnClick(R.id.about_resource_btn)
    void onResourceClick() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_resource_url)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            showSnackbar(R.string.about_resource_fail);
        }
    }

    @OnClick(R.id.about_update_btn)
    void onUpdateClick() {
        if (update) {
            update();
        } else if (!checking) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
                mUpdateText.setText(R.string.about_update_doing);
                checking = true;
                mPresenter.checkUpdate(info.versionName);
            } catch (Exception e) {
                mUpdateText.setText(R.string.about_update_fail);
                checking = false;
            }
        }
    }

    @Override
    public void onUpdateNone() {
        mUpdateText.setText(R.string.about_update_latest);
        HintUtils.showToast(this, R.string.about_update_latest);
        checking = false;
    }

    @Override
    public void onUpdateReady() {
//        mUpdateText.setText(R.string.about_update_download);
        update();
        checking = false;
        update = true;
    }

    @Override
    public void onCheckError() {
        mUpdateText.setText(R.string.about_update_fail);
        checking = false;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_about);
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_about;
    }

    private void update() {
        if (Update.update(this)) {
            mUpdateText.setText(R.string.about_update_summary);
        } else {
            showSnackbar(R.string.about_resource_fail);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                showSnackbar("请选择一个下载源");
                checkSpinnerSelected(spinner_download_source);
                break;
            case 1:
                App.setUpdateCurrentUrl(Constants.UPDATE_GITEE_URL);
                update = false;
                App.getPreferenceManager().putString(PreferenceManager.PREF_UPDATE_CURRENT_URL, App.getUpdateCurrentUrl());
                break;
            case 2:
                App.setUpdateCurrentUrl(Constants.UPDATE_GITHUB_URL);
                update = false;
                App.getPreferenceManager().putString(PreferenceManager.PREF_UPDATE_CURRENT_URL, App.getUpdateCurrentUrl());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void checkSpinnerSelected(AppCompatSpinner spinner) {
        if (App.getPreferenceManager().getString(PreferenceManager.PREF_UPDATE_CURRENT_URL).equals(Constants.UPDATE_GITEE_URL)) {
            spinner.setSelection(1);
        } else if (App.getPreferenceManager().getString(PreferenceManager.PREF_UPDATE_CURRENT_URL).equals(Constants.UPDATE_GITHUB_URL)) {
            spinner.setSelection(2);
        }
    }
}
