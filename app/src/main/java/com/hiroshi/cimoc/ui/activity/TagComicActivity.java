package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.TagComicPresenter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MultiDialogFragment;
import com.hiroshi.cimoc.ui.view.TagComicView;

import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class TagComicActivity extends CoordinatorActivity implements TagComicView,
        MultiDialogFragment.MultiDialogListener, MessageDialogFragment.MessageDialogListener {

    private TagComicPresenter mPresenter;
    private GridAdapter mGridAdapter;
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
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        mPresenter.loadTagComic(id, title);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        List<MiniComic> list = mPresenter.getComicList();
        int size = list.size();
        String[] arr1 = new String[size];
        boolean[] arr2 = new boolean[size];
        for (int i = 0; i < size; ++i) {
            arr1[i] = list.get(i).getTitle();
            arr2[i] = false;
        }
        MultiDialogFragment fragment = MultiDialogFragment.newInstance(R.string.tag_comic_select, arr1, arr2, -1);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(this, comic.getId(), -1, null, true);
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
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onMultiPositiveClick(int type, boolean[] check) {
        showProgressDialog();
        mPresenter.insert(check);
    }

    @Override
    public void onComicLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        hideProgressBar();
        mGridAdapter.addAll(list);
        mActionButton.setImageResource(R.drawable.ic_add_white_24dp);
        mActionButton.show();
    }

    @Override
    public void onComicInsertSuccess(List<MiniComic> list) {
        hideProgressDialog();
        mGridAdapter.addAll(list);
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
    public void onTagComicDelete(MiniComic comic) {
        mGridAdapter.remove(comic);
    }

    @Override
    public void onTagComicInsert(MiniComic comic) {
        mGridAdapter.add(0, comic);
    }

    @Override
    protected String getDefaultTitle() {
        return getIntent().getStringExtra(EXTRA_TITLE);
    }

    private static final String EXTRA_ID = "a";
    private static final String EXTRA_TITLE = "b";

    public static Intent createIntent(Context context, long id, String title) {
        Intent intent = new Intent(context, TagComicActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

}
