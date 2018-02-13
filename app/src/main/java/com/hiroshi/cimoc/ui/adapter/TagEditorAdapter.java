package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.misc.Switcher;
import com.hiroshi.cimoc.model.Tag;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorAdapter extends BaseAdapter<Switcher<Tag>> {

    static class TagHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_select_title) TextView tagTitle;
        @BindView(R.id.item_select_checkbox) CheckBox tagChoice;

        TagHolder(View view) {
            super(view);
        }
    }

    public TagEditorAdapter(Context context, List<Switcher<Tag>> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_select, parent, false);
        return new TagHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        TagHolder viewHolder = (TagHolder) holder;
        Switcher<Tag> switcher = mDataSet.get(position);
        viewHolder.tagTitle.setText(switcher.getElement().getTitle());
        viewHolder.tagChoice.setChecked(switcher.isEnable());
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return null;
    }

    @Override
    protected boolean isClickValid() {
        return true;
    }

}
