package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceAdapter extends BaseAdapter<Source> {

    private OnItemCheckedListener mOnItemCheckedListener;

    public class SourceViewHolder extends BaseViewHolder {
        @BindView(R.id.source_title) TextView sourceTitle;
        @BindView(R.id.source_switch) Switch sourceSwitch;

        public SourceViewHolder(final View view) {
            super(view);
            sourceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mOnItemCheckedListener.onItemCheckedListener(isChecked, getAdapterPosition());
                }
            });
        }
    }

    public SourceAdapter(Context context, List<Source> list) {
        super(context, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_source, parent, false);
        return new SourceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Source source = mDataSet.get(position);
        SourceViewHolder viewHolder = (SourceViewHolder) holder;
        viewHolder.sourceTitle.setText(SourceManager.getTitle(source.getSid()));
        viewHolder.sourceSwitch.setChecked(source.getEnable());
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(10, 10, 10, 0);
            }
        };
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mOnItemCheckedListener = listener;
    }

    public interface OnItemCheckedListener {
        void onItemCheckedListener(boolean isChecked, int position);
    }

}
