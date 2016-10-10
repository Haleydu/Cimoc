package com.hiroshi.cimoc.ui.fragment;

import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.TagPresenter;
import com.hiroshi.cimoc.ui.adapter.CardAdapter;
import com.hiroshi.cimoc.ui.view.TagView;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagFragment extends CardFragment implements TagView {

    private TagPresenter mPresenter;
    private CardAdapter<Tag> mCardAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new TagPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mCardAdapter = new CardAdapter<>(getActivity(), new LinkedList<Tag>());
        mCardAdapter.setOnItemLongClickListener(this);
        mCardAdapter.setOnItemClickListener(this);
        mCardAdapter.setOnItemCheckedListener(this);
        mRecyclerView.addItemDecoration(mCardAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mCardAdapter);
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
    public void onItemCheckedListener(boolean isChecked, int position) {
        Tag tag = mCardAdapter.getItem(position);
        tag.setEnable(isChecked);
        mPresenter.update(tag);
    }

    @Override
    protected void onActionConfirm(String text) {
        if (!StringUtils.isEmpty(text)) {
            for (Tag tag : mCardAdapter.getDateSet()) {
                if (tag.getTitle().equals(text)) {
                    showSnackbar(R.string.tag_add_exist);
                    return;
                }
            }
            mPresenter.insert(text);
        } else {
            showSnackbar(R.string.tag_add_empty);
        }
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        mCardAdapter.addAll(list);
        onInitSuccess();
    }

    @Override
    public void onTagAddSuccess(Tag tag) {
        mCardAdapter.add(tag);
        showSnackbar(R.string.card_add_success);
    }

    @Override
    protected int getActionTitle() {
        return R.string.tag_add;
    }

}
