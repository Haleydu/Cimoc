package com.hiroshi.cimoc.ui.fragment.coordinator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.TagPresenter;
import com.hiroshi.cimoc.ui.activity.PartFavoriteActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TagAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.TagView;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagFragment extends CoordinatorFragment implements TagView {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_EDITOR = 1;

    private TagPresenter mPresenter;
    private TagAdapter mTagAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new TagPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected BaseAdapter initAdapter() {
        mTagAdapter = new TagAdapter(getActivity(), new ArrayList<Tag>());
        return mTagAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(R.drawable.ic_add_white_24dp);
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
        Tag tag = mTagAdapter.getItem(position);
        Intent intent = PartFavoriteActivity.createIntent(getActivity(), tag.getId(), tag.getTitle());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_DIALOG_BUNDLE_ARG_1, position);
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm, R.string.tag_delete_confirm, true, bundle, DIALOG_REQUEST_DELETE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                showProgressDialog();
                int pos = bundle.getBundle(EXTRA_DIALOG_BUNDLE).getInt(EXTRA_DIALOG_BUNDLE_ARG_1);
                mPresenter.delete(mTagAdapter.getItem(pos));
                break;
            case DIALOG_REQUEST_EDITOR:
                String text = bundle.getString(EXTRA_DIALOG_RESULT_VALUE);
                if (!StringUtils.isEmpty(text)) {
                    Tag tag = new Tag(null, text);
                    mPresenter.insert(tag);
                    mTagAdapter.add(tag);
                    showSnackbar(R.string.common_add_success);
                }
                break;
        }
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.tag_add, null, DIALOG_REQUEST_EDITOR);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onTagDeleteSuccess(Tag tag) {
        hideProgressDialog();
        mTagAdapter.remove(tag);
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onTagDeleteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_delete_fail);
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        hideProgressBar();
        mTagAdapter.addAll(list);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
        mTagAdapter.setColor(ContextCompat.getColor(getActivity(), primary));
        mTagAdapter.notifyDataSetChanged();
    }

}
