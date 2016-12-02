package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Pair;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/11/15.
 */

public class ChapterAdapter extends BaseAdapter<Pair<Chapter, Boolean>> {

    static class ChapterHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_select_title) TextView chapterTitle;
        @BindView(R.id.item_select_checkbox) CheckBox chapterChoice;

        ChapterHolder(View view) {
            super(view);
        }
    }

    public ChapterAdapter(Context context, List<Pair<Chapter, Boolean>> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_select, parent, false);
        return new ChapterHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ChapterHolder viewHolder = (ChapterHolder) holder;
        Pair<Chapter, Boolean> pair = mDataSet.get(position);
        viewHolder.chapterTitle.setText(pair.first.getTitle());
        viewHolder.chapterChoice.setEnabled(!pair.first.isDownload());
        viewHolder.chapterChoice.setChecked(pair.second);
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
