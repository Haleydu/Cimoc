package com.hiroshi.cimoc.ui.activity;

import android.support.v7.app.AlertDialog;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.utils.DialogUtils;

/**
 * Created by Hiroshi on 2016/9/11.
 */
public abstract class BackActivity extends BaseActivity {

    protected AlertDialog mProgressDialog;

    @Override
    protected void initToolbar() {
        super.initToolbar();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    @Override
    protected void initView() {
        mProgressDialog = DialogUtils.buildCancelableFalseDialog(this, R.string.dialog_doing);
    }

}
