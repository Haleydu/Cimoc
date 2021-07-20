package com.haleydu.cimoc.ui.fragment.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.model.Tag;
import com.haleydu.cimoc.presenter.BasePresenter;
import com.haleydu.cimoc.presenter.TagPresenter;
import com.haleydu.cimoc.ui.activity.PartFavoriteActivity;
import com.haleydu.cimoc.ui.activity.SearchActivity;
import com.haleydu.cimoc.ui.adapter.BaseAdapter;
import com.haleydu.cimoc.ui.adapter.TagAdapter;
import com.haleydu.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.haleydu.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.haleydu.cimoc.ui.view.TagView;
import com.haleydu.cimoc.utils.HintUtils;
import com.haleydu.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagFragment extends RecyclerViewFragment implements TagView {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_EDITOR = 1;

    private TagPresenter mPresenter;
    private TagAdapter mTagAdapter;

    private Tag mSavedTag;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new TagPresenter();
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
        mTagAdapter = new TagAdapter(getActivity(), new ArrayList<Tag>());
        return mTagAdapter;
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
        inflater.inflate(R.menu.menu_tag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comic_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.tag_add:
                EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.tag_add, null, DIALOG_REQUEST_EDITOR);
                fragment.setTargetFragment(this, 0);
                fragment.show(requireActivity().getSupportFragmentManager(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Tag tag = mTagAdapter.getItem(position);
        Intent intent = PartFavoriteActivity.createIntent(getActivity(), tag.getId(), tag.getTitle());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        mSavedTag = mTagAdapter.getItem(position);
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.tag_delete_confirm, true, DIALOG_REQUEST_DELETE);
        fragment.setTargetFragment(this, 0);
        fragment.show(requireActivity().getSupportFragmentManager(), null);
        return true;
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                showProgressDialog();
                mPresenter.delete(mSavedTag);
                break;
            case DIALOG_REQUEST_EDITOR:
                String text = bundle.getString(EXTRA_DIALOG_RESULT_VALUE);
                if (!StringUtils.isEmpty(text)) {
                    Tag tag = new Tag(null, text);
                    mPresenter.insert(tag);
                    mTagAdapter.add(tag);
                    HintUtils.showToast(getActivity(), R.string.common_execute_success);
                }
                break;
        }
    }

    @Override
    public void onTagRestore(List<Tag> list) {
        mTagAdapter.addAll(list);
    }

    @Override
    public void onTagDeleteSuccess(Tag tag) {
        hideProgressDialog();
        mTagAdapter.remove(tag);
        HintUtils.showToast(getActivity(), R.string.common_execute_success);
    }

    @Override
    public void onTagDeleteFail() {
        hideProgressDialog();
        HintUtils.showToast(getActivity(), R.string.common_execute_fail);
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        hideProgressBar();
        mTagAdapter.addAll(list);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressBar();
        HintUtils.showToast(getActivity(), R.string.common_data_load_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mTagAdapter.setColor(ContextCompat.getColor(getActivity(), primary));
        mTagAdapter.notifyDataSetChanged();
    }

}
