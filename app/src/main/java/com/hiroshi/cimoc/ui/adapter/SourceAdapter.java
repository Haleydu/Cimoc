package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Source;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class SourceAdapter extends BaseAdapter<Source> {

    private OnItemCheckedListener mOnItemCheckedListener;
    private @ColorInt int color = -1;

    static class SourceHolder extends BaseViewHolder {
        @BindView(R.id.item_source_title) TextView sourceTitle;
        @BindView(R.id.item_source_switch) SwitchCompat sourceSwitch;

        SourceHolder(final View view) {
            super(view);
        }
    }

    public SourceAdapter(Context context, List<Source> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_source, parent, false);
        return new SourceHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Source source = mDataSet.get(position);
        final SourceHolder viewHolder = (SourceHolder) holder;
        viewHolder.sourceTitle.setText(source.getTitle());
        viewHolder.sourceSwitch.setChecked(source.getEnable());
        viewHolder.sourceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mOnItemCheckedListener != null) {
                    mOnItemCheckedListener.onItemCheckedListener(isChecked, viewHolder.getAdapterPosition());
                }
            }
        });
        if (color != -1) {
            ColorStateList thumbList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked }, { android.R.attr.state_checked }},
                    new int[]{Color.WHITE, color});
            viewHolder.sourceSwitch.setThumbTintList(thumbList);
            ColorStateList trackList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked }, { android.R.attr.state_checked }},
                    new int[]{0x4C000000, (0x00FFFFFF & color | 0x4C000000)});
            viewHolder.sourceSwitch.setTrackTintList(trackList);
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

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mOnItemCheckedListener = listener;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    public interface OnItemCheckedListener {
        void onItemCheckedListener(boolean isChecked, int position);
    }

}
