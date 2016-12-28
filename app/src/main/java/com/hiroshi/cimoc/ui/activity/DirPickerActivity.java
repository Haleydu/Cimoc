package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.DirAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/12/6.
 */

public class DirPickerActivity extends CoordinatorActivity {

    private DirAdapter mDirAdapter;
    private File mFile;

    @Override
    protected BaseAdapter initAdapter() {
        mDirAdapter = new DirAdapter(this, new ArrayList<String>());
        return mDirAdapter;
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(R.drawable.ic_done_white_24dp);
        mActionButton.show();
    }

    @Override
    protected void initData() {
        mFile = Environment.getExternalStorageDirectory();
        updateData();
        hideProgressBar();
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        Intent intent = new Intent();
        intent.putExtra(Extra.EXTRA_PICKER_PATH, mFile.getAbsolutePath());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position == 0) {
            if (mFile.getParentFile() == null) {
                return;
            }
            mFile = mFile.getParentFile();
        } else {
            String title = mDirAdapter.getItem(position);
            mFile = new File(mFile.getAbsolutePath(), title);
        }
        updateData();
        mActionButton.show();
    }

    private void updateData() {
        mDirAdapter.setData(listDir(mFile));
        if (mToolbar != null) {
            mToolbar.setTitle(mFile.getAbsolutePath());
        }
    }

    private List<String> listDir(File parent) {
        List<String> list = new ArrayList<>();
        File[] files = parent.listFiles();
        if (files != null) {
            for (File dir : parent.listFiles()) {
                if (dir.isDirectory()) {
                    list.add(dir.getName());
                }
            }
            Collections.sort(list);
        }
        list.add(0, getString(R.string.dir_picker_parent));
        return list;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.dir_picker);
    }

}
