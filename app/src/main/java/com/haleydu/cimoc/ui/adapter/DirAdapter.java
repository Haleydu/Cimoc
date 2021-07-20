package com.haleydu.cimoc.ui.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haleydu.cimoc.R;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/6.
 */

public class DirAdapter extends BaseAdapter<String> {

    public DirAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_dir, parent, false);
        return new DirHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        DirHolder viewHolder = (DirHolder) holder;
        viewHolder.mDirTitle.setText(mDataSet.get(position));
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return null;
    }

    static class DirHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_dir_title)
        TextView mDirTitle;

        DirHolder(View view) {
            super(view);
        }
    }

}
