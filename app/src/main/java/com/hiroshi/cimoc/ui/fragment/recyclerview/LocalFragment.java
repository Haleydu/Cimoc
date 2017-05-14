package com.hiroshi.cimoc.ui.fragment.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Local;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.LocalPresenter;
import com.hiroshi.cimoc.ui.activity.PartFavoriteActivity;
import com.hiroshi.cimoc.ui.activity.SearchActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.LocalAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.LocalView;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;

/**
 * Created by Hiroshi on 2017/4/19.
 */

public class LocalFragment extends RecyclerViewFragment implements LocalView {

    private static final int DIALOG_REQUEST_DELETE = 0;

    private LocalPresenter mPresenter;
    private LocalAdapter mLocalAdapter;

    private Local mSavedLocal;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new LocalPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        super.initView();
    }

    @Override
    protected BaseAdapter initAdapter() {
        mLocalAdapter = new LocalAdapter(getActivity(), new ArrayList<Local>());
        return mLocalAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_local, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tag_add:
                EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.tag_add, null, DIALOG_REQUEST_EDITOR);
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Local local = mLocalAdapter.getItem(position);
//        Intent intent = PartFavoriteActivity.createIntent(getActivity(), local.getId(), local.getTitle());
//        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        mSavedLocal = mLocalAdapter.getItem(position);
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.local_delete_confirm, true, DIALOG_REQUEST_DELETE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                showProgressDialog();
                mPresenter.delete(mSavedLocal);
                break;
        }
    }

    @Override
    public void onDataLoadSuccess() {

    }

    @Override
    public void onDataLoadFail() {

    }

    @Override
    public void onLocalAddSuccess() {

    }

}

