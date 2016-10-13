package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.presenter.TagComicPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SelectDialogFragment;
import com.hiroshi.cimoc.ui.view.TagComicView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class TagComicActivity extends BackActivity implements TagComicView,
        BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener,
        SelectDialogFragment.SelectDialogListener, MessageDialogFragment.MessageDialogListener {

    @BindView(R.id.tag_comic_layout) View mLayoutView;
    @BindView(R.id.tag_comic_recycler_view) RecyclerView mRecyclerView;

    private TagComicPresenter mPresenter;
    private GridAdapter mGridAdapter;
    private List<MiniComic> mComicList;
    private List<MiniComic> mTempList;
    private int mTempPosition = -1;

    @Override
    protected void initPresenter() {
        mPresenter = new TagComicPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mGridAdapter = new GridAdapter(this, new LinkedList<MiniComic>());
        mGridAdapter.setOnItemClickListener(this);
        mGridAdapter.setOnItemLongClickListener(this);
        mGridAdapter.setProvider(((CimocApplication) getApplication()).getBuilderProvider());
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(mGridAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mGridAdapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mTempList = new LinkedList<>();
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        mPresenter.loadTagComic(id);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @OnClick(R.id.tag_comic_action_button) void onActionButtonClick() {
        if (mComicList == null) {
            mPresenter.loadComic(new HashSet<>(mGridAdapter.getDateSet()));
        } else {
            showSelectDialog();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(this, comic.getId(), comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.tag_comic_delete_confirm, true);
        mTempPosition = position;
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onMessagePositiveClick(int type) {
        long tid = getIntent().getLongExtra(EXTRA_ID, -1);
        long cid = mGridAdapter.getItem(mTempPosition).getId();
        mPresenter.delete(tid, cid);
        mGridAdapter.remove(mTempPosition);
        mTempPosition = -1;
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onSelectPositiveClick(int type, List<Selectable> list) {
        for (int i = 0; i != list.size(); ++i) {
            if (list.get(i).isChecked()) {
                mTempList.add(mComicList.get(i));
            }
        }
        if (!mTempList.isEmpty()) {
            long id = getIntent().getLongExtra(EXTRA_ID, -1);
            mPresenter.insert(id, mTempList);
        }
    }

    @Override
    public void onSelectNeutralClick(int type, List<Selectable> list) {}

    @Override
    public void onTagComicLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
        hideProgressBar();
    }

    @Override
    public void onTagComicLoadSuccess(List<MiniComic> list) {
        mGridAdapter.addAll(list);
        hideProgressBar();
    }

    @Override
    public void onComicLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        mComicList = list;
        showSelectDialog();
    }

    private void showSelectDialog() {
        ArrayList<Selectable> list = new ArrayList<>(mComicList.size());
        for (MiniComic comic : mComicList) {
            list.add(new Selectable(false, false, comic.getTitle()));
        }
        SelectDialogFragment fragment = SelectDialogFragment.newInstance(list, R.string.tag_comic_select);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onComicInsertSuccess() {
        mComicList.removeAll(mTempList);
        mGridAdapter.addAll(0, mTempList);
        mTempList.clear();
        showSnackbar(R.string.common_add_success);
    }

    @Override
    public void onComicInsertFail() {
        showSnackbar(R.string.common_add_fail);
    }

    @Override
    public void onComicUnFavorite(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicFavorite(MiniComic comic) {
        // We can insert it because we can assert the comic we unfavorite belong to the tag.
        mGridAdapter.add(0, comic);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.tag_comic);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_tag_comic;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    private static final String EXTRA_ID = "a";

    public static Intent createIntent(Context context, long id) {
        Intent intent = new Intent(context, TagComicActivity.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

}
