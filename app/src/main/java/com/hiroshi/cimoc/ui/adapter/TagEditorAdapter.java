package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Tag;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorAdapter extends BaseAdapter<Pair<Tag, Boolean>> {

    static class TagHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_select_title) TextView tagTitle;
        @BindView(R.id.item_select_checkbox) CheckBox tagChoice;

        TagHolder(View view) {
            super(view);
        }
    }

    public TagEditorAdapter(Context context, List<Pair<Tag, Boolean>> list) {
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
        Pair<Tag, Boolean> pair = mDataSet.get(position);
        viewHolder.tagTitle.setText(pair.first.getTitle());
        viewHolder.tagChoice.setChecked(pair.second);
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
