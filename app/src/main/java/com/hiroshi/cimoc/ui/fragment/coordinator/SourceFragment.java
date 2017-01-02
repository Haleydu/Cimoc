package com.hiroshi.cimoc.ui.fragment.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.global.ImageServer;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SourcePresenter;
import com.hiroshi.cimoc.ui.activity.CategoryActivity;
import com.hiroshi.cimoc.ui.activity.SearchActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.SourceAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.view.DialogView;
import com.hiroshi.cimoc.ui.view.SourceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends CoordinatorFragment implements SourceView, SourceAdapter.OnItemCheckedListener {

    private static final int DIALOG_REQUEST_EDITOR = 0;

    private SourcePresenter mPresenter;
    private SourceAdapter mSourceAdapter;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SourcePresenter();
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
        mSourceAdapter = new SourceAdapter(getActivity(), new ArrayList<Source>());
        mSourceAdapter.setOnItemCheckedListener(this);
        return mSourceAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

    @Override
    protected void initActionButton() {
        mLayoutView.removeView(mActionButton);
        mActionButton = null;
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.source_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comic_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = CategoryActivity.createIntent(getActivity(), mSourceAdapter.getItem(position).getType());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        int source = mSourceAdapter.getItem(position).getType();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogView.EXTRA_DIALOG_BUNDLE_ARG_1, source);
        EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.source_server,
                ImageServer.get(source), bundle, DIALOG_REQUEST_EDITOR);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mSourceAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_EDITOR:
                Bundle extra = bundle.getBundle(EXTRA_DIALOG_BUNDLE);
                if (extra != null) {
                    int source = extra.getInt(EXTRA_DIALOG_BUNDLE_ARG_1);
                    String value = bundle.getString(DialogView.EXTRA_DIALOG_RESULT_VALUE);
                    ImageServer.update(mPreference, source, value);
                }
                break;
        }
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        mSourceAdapter.addAll(list);
        hideProgressBar();
    }

    @Override
    public void onSourceLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
        hideProgressBar();
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mSourceAdapter.setColor(ContextCompat.getColor(getActivity(), accent));
        mSourceAdapter.notifyDataSetChanged();
    }

}
