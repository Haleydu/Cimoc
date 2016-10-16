package com.hiroshi.cimoc.ui.fragment.classical;

import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.TagPresenter;
import com.hiroshi.cimoc.ui.activity.TagComicActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TagAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.TagView;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagFragment extends ClassicalFragment implements TagView, EditorDialogFragment.EditorDialogListener,
        MessageDialogFragment.MessageDialogListener {

    private TagPresenter mPresenter;
    private TagAdapter mTagAdapter;
    private int mTempPosition = -1;

    @Override
    protected void initPresenter() {
        mPresenter = new TagPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        mTagAdapter = new TagAdapter(getActivity(), new ArrayList<Tag>());
        mTagAdapter.setOnItemLongClickListener(this);
        super.initView();
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = TagComicActivity.createIntent(getActivity(), mTagAdapter.getItem(position).getId());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        mTempPosition = position;
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm, R.string.tag_delete_confirm, true);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onMessagePositiveClick(int type) {
        showProgressDialog();
        mPresenter.delete(mTagAdapter.getItem(mTempPosition));
    }

    @Override
    protected void onActionButtonClick() {
        EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.tag_add);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onEditorPositiveClick(String text) {
        if (!StringUtils.isEmpty(text)) {
            Tag tag = new Tag(null, text);
            mPresenter.insert(tag);
            mTagAdapter.add(tag);
            showSnackbar(R.string.common_add_success);
        }
    }

    @Override
    public void onTagDeleteSuccess() {
        hideProgressDialog();
        mTagAdapter.remove(mTempPosition);
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onTagDeleteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_delete_fail);
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        mTagAdapter.addAll(list);
    }

    @Override
    public void onTagLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
        mTagAdapter.setColor(ContextCompat.getColor(getActivity(), primary));
        mTagAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_add_white_24dp;
    }

    @Override
    protected BaseAdapter getAdapter() {
        return mTagAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
    }

}
