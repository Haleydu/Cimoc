package com.hiroshi.cimoc.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/20.
 */
public class AboutFragment extends BaseFragment {

    private boolean isEnable;
    private int count;

    @OnClick(R.id.about_support_btn) void onSupportClick() {
        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, getString(R.string.about_support_email)));
        showSnackbar(R.string.about_already_clip);
    }

    @OnClick(R.id.about_resource_btn) void onResourceClick() {
        if (++count > 9) {
            isEnable = !isEnable;
            CimocApplication.getPreferences().putBoolean(PreferenceMaster.PREF_EX, isEnable);
            showSnackbar(isEnable ? R.string.about_turn_on : R.string.about_turn_off);
            count = 0;
        }
    }

    @Override
    protected void initView() {
        isEnable = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_EX, false);
        count = 0;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_about;
    }

}
