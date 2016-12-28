package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.cimoc.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterActivity extends CoordinatorActivity {

    private ChapterAdapter mChapterAdapter;
    private boolean mTaskOrder;

    @Override
    protected BaseAdapter initAdapter() {
        mChapterAdapter = new ChapterAdapter(this, getAdapterList());
        return mChapterAdapter;
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(R.drawable.ic_done_white_24dp);
        mActionButton.show();
        hideProgressBar();
    }

    private List<Pair<Chapter, Boolean>> getAdapterList() {
        mTaskOrder = mPreference.getBoolean(PreferenceManager.PREF_DOWNLOAD_ORDER, false);
        List<Chapter> list = getIntent().getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
        List<Pair<Chapter, Boolean>> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            result.add(Pair.create(list.get(i), list.get(i).isDownload()));
        }
        if (mTaskOrder) {
            Collections.reverse(result);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.chapter_all:
                    for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
                        pair.second = true;
                    }
                    mChapterAdapter.notifyDataSetChanged();
                    break;
                case R.id.chapter_sort:
                    mChapterAdapter.reverse();
                    mTaskOrder = !mTaskOrder;
                    mPreference.putBoolean(PreferenceManager.PREF_DOWNLOAD_ORDER, mTaskOrder);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Pair<Chapter, Boolean> pair = mChapterAdapter.getItem(position);
        if (!pair.first.isDownload()) {
            pair.second = !pair.second;
            mChapterAdapter.notifyItemChanged(position);
        }
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        showProgressDialog();
        ArrayList<Chapter> list = new ArrayList<>();
        for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
            if (!pair.first.isDownload() && pair.second) {
                list.add(pair.first);
            }
        }

        if (list.isEmpty()) {
            showSnackbar(R.string.chapter_download_empty);
        } else if (PermissionUtils.hasStoragePermission(this)) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Extra.EXTRA_CHAPTER, list);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            showSnackbar(R.string.chapter_download_perm_fail);
        }
        hideProgressBar();
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.chapter);
    }

    public static Intent createIntent(Context context, ArrayList<Chapter> list) {
        Intent intent = new Intent(context, ChapterActivity.class);
        intent.putExtra(Extra.EXTRA_CHAPTER, list);
        return intent;
    }

}
