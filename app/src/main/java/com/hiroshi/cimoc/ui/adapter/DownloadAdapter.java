package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/4.
 */
public class DownloadAdapter extends BaseAdapter<String> {

    private boolean[] disable;
    private boolean[] select;

    public class ChapterHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.download_chapter_title) TextView chapterTitle;
        @BindView(R.id.download_chapter_checkbox) CheckBox chapterChoice;

        public ChapterHolder(View view) {
            super(view);
        }
    }

    public DownloadAdapter(Context context, List<String> list, boolean[] select) {
        super(context, list);
        this.disable = select;
        this.select = select;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_download, parent, false);
        return new ChapterHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChapterHolder viewHolder = (ChapterHolder) holder;
        viewHolder.chapterTitle.setText(mDataSet.get(position));
        viewHolder.chapterChoice.setEnabled(!disable[position]);
        viewHolder.chapterChoice.setChecked(select[position]);
    }

    public void onClick(int position) {
        if (!disable[position]) {
            select[position] = !select[position];
        }
    }

}
