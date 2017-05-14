package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Local;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public class LocalAdapter extends BaseAdapter<Local> {

    static class LocalHolder extends BaseViewHolder {
        @BindView(R.id.item_local_title)
        TextView localTitle;

        LocalHolder(final View view) {
            super(view);
        }
    }

    public LocalAdapter(Context context, List<Local> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_local, parent, false);
        return new TagAdapter.TagHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Local local = mDataSet.get(position);
        LocalHolder viewHolder = (LocalHolder) holder;
        viewHolder.localTitle.setText(local.getTitle());
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 10);
            }
        };
    }

}
