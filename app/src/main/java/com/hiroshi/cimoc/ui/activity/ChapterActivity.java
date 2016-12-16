package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.cimoc.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterActivity extends CoordinatorActivity {

    private ChapterAdapter mChapterAdapter;

    @Override
    protected void initView() {
        super.initView();
        mChapterAdapter = new ChapterAdapter(this, getAdapterList());
        mChapterAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mChapterAdapter);
        mActionButton.setImageResource(R.drawable.ic_done_white_24dp);
        mActionButton.setVisibility(View.VISIBLE);
        hideProgressBar();
    }

    private List<Pair<Chapter, Boolean>> getAdapterList() {
        List<Chapter> list = getIntent().getParcelableArrayListExtra(EXTRA_CHAPTER);
        List<Pair<Chapter, Boolean>> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            result.add(Pair.create(list.get(i), list.get(i).isDownload()));
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
            intent.putParcelableArrayListExtra(EXTRA_CHAPTER, list);
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            showSnackbar(R.string.chapter_download_perm_fail);
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.chapter);
    }

    public static final String EXTRA_CHAPTER = "cimoc.intent.extra.EXTRA_CHAPTER";

    public static Intent createIntent(Context context, ArrayList<Chapter> list) {
        Intent intent = new Intent(context, ChapterActivity.class);
        intent.putExtra(EXTRA_CHAPTER, list);
        return intent;
    }

}
