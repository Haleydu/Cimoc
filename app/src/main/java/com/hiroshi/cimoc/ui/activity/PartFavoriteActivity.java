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
import com.hiroshi.cimoc.presenter.PartFavoritePresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MultiDialogFragment;
import com.hiroshi.cimoc.ui.view.PartFavoriteView;

import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class PartFavoriteActivity extends CoordinatorActivity implements PartFavoriteView {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_ADD = 1;

    private PartFavoritePresenter mPresenter;
    private GridAdapter mGridAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new PartFavoritePresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected BaseAdapter initAdapter() {
        mGridAdapter = new GridAdapter(this, new LinkedList<MiniComic>());
        mGridAdapter.setProvider(((CimocApplication) getApplication()).getBuilderProvider());
        return mGridAdapter;
    }

    @Override
    protected void initActionButton() {
        if (getIntent().getLongExtra(EXTRA_ID, -1) < 0) {
            mLayoutView.removeView(mActionButton);
            mActionButton = null;
        }
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(this, 3);
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        mPresenter.load(id, title);
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
        long[] arr3 = new long[size];
        for (int i = 0; i < size; ++i) {
            MiniComic comic = list.get(i);
            arr1[i] = comic.getTitle();
            arr2[i] = false;
            arr3[i] = comic.getId();
        }
        Bundle bundle = new Bundle();
        bundle.putLongArray(EXTRA_DIALOG_BUNDLE_ARG_1, arr3);
        MultiDialogFragment fragment = MultiDialogFragment.newInstance(R.string.tag_comic_select, arr1, arr2, bundle, DIALOG_REQUEST_ADD);
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
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_DIALOG_BUNDLE_ARG_1, position);
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.tag_comic_delete_confirm, true, bundle, DIALOG_REQUEST_DELETE);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                int pos = bundle.getBundle(EXTRA_DIALOG_BUNDLE).getInt(EXTRA_DIALOG_BUNDLE_ARG_1);
                long tid = getIntent().getLongExtra(EXTRA_ID, -1);
                long cid = mGridAdapter.getItem(pos).getId();
                mPresenter.delete(tid, cid);
                mGridAdapter.remove(pos);
                showSnackbar(R.string.common_execute_success);
                break;
            case DIALOG_REQUEST_ADD:
                showProgressDialog();
                Bundle extra = bundle.getBundle(EXTRA_DIALOG_BUNDLE);
                if (extra != null) {
                    long[] id = extra.getLongArray(EXTRA_DIALOG_BUNDLE_ARG_1);
                    boolean[] check = bundle.getBooleanArray(EXTRA_DIALOG_RESULT_VALUE);
                    if (id != null && check != null && id.length == check.length) {
                        List<Long> list = new LinkedList<>();
                        for (int i = 0; i != check.length; ++i) {
                            if (check[i]) {
                                list.add(id[i]);
                            }
                        }
                        mPresenter.insert(list);
                    } else {
                        onComicInsertFail();
                    }
                } else {
                    onComicInsertFail();
                }
                break;
        }
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
        if (mActionButton != null) {
            mActionButton.setImageResource(R.drawable.ic_add_white_24dp);
            mActionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onComicInsertSuccess(List<MiniComic> list) {
        hideProgressDialog();
        mGridAdapter.addAll(list);
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onComicInsertFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onComicRemove(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicAdd(MiniComic comic) {
        mGridAdapter.add(0, comic);
    }

    @Override
    protected String getDefaultTitle() {
        return getIntent().getStringExtra(EXTRA_TITLE);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_part_favorite;
    }

    private static final String EXTRA_ID = "a";
    private static final String EXTRA_TITLE = "b";

    public static Intent createIntent(Context context, long id, String title) {
        Intent intent = new Intent(context, PartFavoriteActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }

}
