package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.TagPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TagAdapter;
import com.hiroshi.cimoc.ui.view.TagView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagFragment extends ClassicalFragment implements TagView {

    private TagPresenter mPresenter;
    private TagAdapter mTagAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new TagPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        mTagAdapter = new TagAdapter(getActivity(), new LinkedList<Tag>());
        mTagAdapter.setOnItemLongClickListener(this);
        super.initView();
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onItemLongClick(View view, int position) {

    }

    @Override
    protected void onActionButtonClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_single_editor, null);
        final EditText editText = (EditText) view.findViewById(R.id.dialog_single_edit_text);
        builder.setTitle(R.string.tag_add);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPresenter.insert(editText.getText().toString());
            }
        });
        builder.show();
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
    public void onTagAddSuccess(Tag tag) {
        mTagAdapter.add(tag);
        showSnackbar(R.string.tag_add_success);
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
        return new GridLayoutManager(getActivity(), 3);
    }

}
