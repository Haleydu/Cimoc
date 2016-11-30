package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.hiroshi.cimoc.model.Chapter;

import java.util.List;

/**
 * Created by Hiroshi on 2016/11/15.
 */

public class ChapterListAdapter extends BaseAdapter<Chapter> {

    public ChapterListAdapter(Context context, List<Chapter> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return null;
    }

}
