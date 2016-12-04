package com.hiroshi.cimoc.ui.fragment.coordinator;

import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SourcePresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.SourceAdapter;
import com.hiroshi.cimoc.ui.view.SourceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends CoordinatorFragment implements SourceView, SourceAdapter.OnItemCheckedListener {

    private SourcePresenter mPresenter;
    private SourceAdapter mSourceAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new SourcePresenter();
        mPresenter.attachView(this);
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
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = ResultActivity.createIntent(getActivity(), mSourceAdapter.getItem(position).getType());
        startActivity(intent);
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mSourceAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
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
        mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
        mSourceAdapter.setColor(ContextCompat.getColor(getActivity(), accent));
        mSourceAdapter.notifyDataSetChanged();
    }

}
