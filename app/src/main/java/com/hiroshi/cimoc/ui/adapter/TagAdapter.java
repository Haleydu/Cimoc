package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Tag;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class TagAdapter extends BaseAdapter<Tag> {

    private @ColorInt int color = -1;

    static class TagHolder extends BaseViewHolder {
        @BindView(R.id.item_tag_title) TextView tagTitle;

        TagHolder(final View view) {
            super(view);
        }
    }

    public TagAdapter(Context context, List<Tag> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_tag, parent, false);
        return new TagHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Tag tag = mDataSet.get(position);
        TagHolder viewHolder = (TagHolder) holder;
        viewHolder.tagTitle.setText(tag.getTitle());
        if (color != -1) {
            viewHolder.tagTitle.setBackgroundColor(color);
        }
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, 0, offset, (int) (offset * 1.5));
            }
        };
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

}
