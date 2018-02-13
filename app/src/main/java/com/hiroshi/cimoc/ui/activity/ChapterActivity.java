package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.misc.Switcher;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.cimoc.ui.widget.ViewUtils;
import com.hiroshi.cimoc.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterActivity extends BackActivity implements BaseAdapter.OnItemClickListener {

    @BindView(R.id.chapter_recycler_view) RecyclerView mRecyclerView;

    private ChapterAdapter mChapterAdapter;
    private boolean isAscendMode;
    private boolean isButtonMode;
    private Handler mHandler = new Handler();
    private RecyclerView.OnItemTouchListener mListener = new CustomTouchListener();
    private RecyclerView.ItemDecoration mDecoration;

    @Override
    protected void initView() {
        isButtonMode = mPreference.getBoolean(PreferenceManager.PREF_CHAPTER_BUTTON_MODE, false);
        mChapterAdapter = new ChapterAdapter(this, getAdapterList());
        mDecoration = mChapterAdapter.getItemDecoration();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mChapterAdapter);
        switchMode();
    }

    private void switchMode() {
        mChapterAdapter.setButtonMode(isButtonMode);
        if (isButtonMode) {
            mChapterAdapter.setOnItemClickListener(null);
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
            mRecyclerView.addItemDecoration(mDecoration);
            mRecyclerView.addOnItemTouchListener(mListener);
            mRecyclerView.setPadding((int) ViewUtils.dpToPixel(4, this), (int) ViewUtils.dpToPixel(10, this), (int) ViewUtils.dpToPixel(4, this), 0);
        } else {
            mChapterAdapter.setOnItemClickListener(this);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.removeItemDecoration(mDecoration);
            mRecyclerView.removeOnItemTouchListener(mListener);
            mRecyclerView.setPadding(0, 0, 0, 0);
        }
    }

    private List<Switcher<Chapter>> getAdapterList() {
        isAscendMode = mPreference.getBoolean(PreferenceManager.PREF_CHAPTER_ASCEND_MODE, false);
        List<Chapter> list = getIntent().getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
        List<Switcher<Chapter>> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            result.add(new Switcher<>(list.get(i), list.get(i).isDownload()));
        }
        if (isAscendMode) {
            Collections.reverse(result);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.chapter_all:
                    for (Switcher<Chapter> switcher : mChapterAdapter.getDateSet()) {
                        switcher.setEnable(true);
                    }
                    mChapterAdapter.notifyDataSetChanged();
                    break;
                case R.id.chapter_sort:
                    mChapterAdapter.reverse();
                    isAscendMode = !isAscendMode;
                    mPreference.putBoolean(PreferenceManager.PREF_CHAPTER_ASCEND_MODE, isAscendMode);
                    break;
                case R.id.chapter_switch_view:
                    isButtonMode = !isButtonMode;
                    switchMode();
                    mPreference.putBoolean(PreferenceManager.PREF_CHAPTER_BUTTON_MODE, isButtonMode);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Switcher<Chapter> switcher = mChapterAdapter.getItem(position);
        if (!switcher.getElement().isDownload()) {
            switcher.switchEnable();
            mChapterAdapter.notifyItemChanged(position);
        }
    }

    @OnClick(R.id.chapter_action_button) void onActionButtonClick() {
        ArrayList<Chapter> list = new ArrayList<>();
        for (Switcher<Chapter> switcher : mChapterAdapter.getDateSet()) {
            if (!switcher.getElement().isDownload() && switcher.isEnable()) {
                list.add(switcher.getElement());
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
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_chapter;
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

    class CustomTouchListener implements RecyclerView.OnItemTouchListener {
        private int mLastPosition = -1;
        private boolean isLongPress = false;

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (isLongPress) {
                return true;
            }

            int pos = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastPosition = pos;
                    if (mLastPosition != -1) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isLongPress = true;
                                update(mLastPosition);
                            }
                        }, 500);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mLastPosition != pos) {
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mHandler.removeCallbacksAndMessages(null);
                    if (pos != -1 && mLastPosition == pos) {
                        update(pos);
                    }
                    break;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            int pos = rv.getChildAdapterPosition(rv.findChildViewUnder(e.getX(), e.getY()));
            switch (e.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if (pos != -1 && mLastPosition != pos) {
                        update(pos);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    isLongPress = false;
                    break;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }

        private void update(int pos) {
            mLastPosition = pos;
            mChapterAdapter.getItem(pos).switchEnable();
            mChapterAdapter.notifyItemChanged(pos);
        }
    }

}
