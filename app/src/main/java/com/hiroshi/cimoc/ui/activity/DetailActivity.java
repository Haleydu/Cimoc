package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.DetailAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.SelectDialogFragment;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BackActivity implements DetailView, DetailAdapter.OnTitleClickListener,
        SelectDialogFragment.SelectDialogListener, BaseAdapter.OnItemClickListener {

    @BindView(R.id.detail_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.detail_layout) View mLayoutView;
    @BindView(R.id.detail_action_button) FloatingActionButton mActionButton;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;

    @Override
    protected void initPresenter() {
        mPresenter = new DetailPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mDetailAdapter = new DetailAdapter(this, new LinkedList<Chapter>());
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mDetailAdapter);
        mRecyclerView.addItemDecoration(mDetailAdapter.getItemDecoration());
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
            String cid = getIntent().getStringExtra(EXTRA_CID);
            if (source == -1 || cid == null) {
                mPresenter.loadDetail();
            } else {
                mPresenter.loadDetail(source, cid);
            }
        } else {
            mPresenter.loadDetail(id);
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.detail_download:
                    Intent intent = ChapterListActivity.createIntent(this, new ArrayList<>(mDetailAdapter.getDateSet()));
                    startActivity(intent);
                    break;
                case R.id.detail_tag:
                    if (mPresenter.isFavorite()) {
                        showProgressDialog();
                        mPresenter.loadTag();
                    } else {
                        showSnackbar(R.string.detail_tag_favorite);
                    }
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.detail_action_button) void onActionButtonClick() {
        if (mPresenter.isFavorite()) {
            mPresenter.unfavoriteComic();
            mActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            mActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position != 0) {
            String last = mDetailAdapter.getItem(position - 1).getPath();
            mPresenter.updateLast(last, getIntent().getBooleanExtra(EXTRA_FAVORITE, false));
            mDetailAdapter.setLast(last);
            int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
            Intent intent = ReaderActivity.createIntent(this, mode, mDetailAdapter.getDateSet());
            startActivity(intent);
        }
    }

    @Override
    public void onTitleClick() {
        String last = mPresenter.getLast();
        List<Chapter> list = mDetailAdapter.getDateSet();
        if (last == null) {
            last = mDetailAdapter.getItem(list.size() - 1).getPath();
            mDetailAdapter.setLast(last);
        }
        mPresenter.updateLast(last, getIntent().getBooleanExtra(EXTRA_FAVORITE, false));
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, mode, list);
        startActivity(intent);
    }

    @Override
    public void onChapterChange(String last) {
        mDetailAdapter.setLast(last);
    }

    @Override
    public void onTagLoadSuccess(List<Selectable> list) {
        hideProgressDialog();
        if (!list.isEmpty()) {
            SelectDialogFragment fragment =
                    SelectDialogFragment.newInstance(new ArrayList<>(list), R.string.detail_select_tag, -1);
            fragment.show(getFragmentManager(), null);
        } else {
            showSnackbar(R.string.backup_save_tag_not_found);
        }
    }

    @Override
    public void onTagLoadFail() {
        showSnackbar(R.string.detail_tag_load_fail);
        hideProgressDialog();
    }

    @Override
    public void onTagUpdateSuccess() {
        showSnackbar(R.string.detail_tag_update_success);
        hideProgressDialog();
    }

    @Override
    public void onTagUpdateFail() {
        showSnackbar(R.string.detail_tag_update_fail);
        hideProgressDialog();
    }

    @Override
    public void onSelectPositiveClick(int type, List<Selectable> list) {
        showProgressDialog();
        List<Long> newTagList = new LinkedList<>();
        for (Selectable selectable : list) {
            if (selectable.isChecked()) {
                newTagList.add(selectable.getId());
            }
        }
        mPresenter.updateRef(newTagList);
    }

    @Override
    public void onSelectNeutralClick(int type, List<Selectable> list) {}

    /**
     *  init: load comic -> load chapter -> load download
     *  if load download fail, we still show the layout
     */

    @Override
    public void onComicLoadSuccess(Comic comic) {
        mDetailAdapter.setInfo(comic.getSource(), comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());

        if (comic.getTitle() != null && comic.getCover() != null) {
            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mActionButton.setImageResource(resId);
            mActionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoadSuccess(List<Chapter> list) {
        mDetailAdapter.setOnItemClickListener(this);
        mDetailAdapter.setOnTitleClickListener(this);
        mDetailAdapter.addAll(list);
    }

    @Override
    public void onDetailLoadSuccess() {
        mPresenter.loadDownload();
    }

    @Override
    public void onDownloadLoadSuccess(List<Task> list) {
        Set<String> set = new HashSet<>();
        for (Task task : list) {
            if (task.isFinish()) {
                set.add(task.getPath());
            }
        }
        for (Chapter chapter : mDetailAdapter.getDateSet()) {
            chapter.setComplete(set.contains(chapter.getPath()));
        }
        hideProgressBar();
    }

    @Override
    public void onDownloadLoadFail() {
        showSnackbar(R.string.detail_download_load_fail);
        hideProgressBar();
    }

    @Override
    public void onNetworkError() {
        showSnackbar(R.string.common_network_error);
        hideProgressBar();
    }

    @Override
    public void onParseError() {
        showSnackbar(R.string.common_parse_error);
        hideProgressBar();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    public static final String EXTRA_ID = "a";
    public static final String EXTRA_SOURCE = "b";
    public static final String EXTRA_CID = "c";
    public static final String EXTRA_FAVORITE = "d";

    public static Intent createIntent(Context context) {
        return createIntent(context, null, -1, null, false);
    }

    public static Intent createIntent(Context context, Long id) {
        return createIntent(context, id, -1, null, false);
    }

    public static Intent createIntent(Context context, int source, String cid) {
        return createIntent(context, null, source, cid, false);
    }

    public static Intent createIntent(Context context, Long id, boolean favorite) {
        return createIntent(context, id, -1, null, favorite);
    }

    public static Intent createIntent(Context context, Long id, int source, String cid, boolean favorite) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        intent.putExtra(EXTRA_FAVORITE, favorite);
        return intent;
    }

}
