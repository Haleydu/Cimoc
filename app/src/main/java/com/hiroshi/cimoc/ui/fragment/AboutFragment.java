package com.hiroshi.cimoc.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.hiroshi.cimoc.R;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/20.
 */
public class AboutFragment extends BaseFragment {

    @OnClick(R.id.about_support_btn) void onSupportClick() {
        ClipboardManager manager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText(null, getString(R.string.about_support_email)));
        showSnackbar(R.string.about_already_clip);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_about;
    }

}
