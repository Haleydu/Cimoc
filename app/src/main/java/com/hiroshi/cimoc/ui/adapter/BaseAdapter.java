package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.hiroshi.cimoc.model.Comic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context mContext;
    protected List<Comic> mDataSet;
    protected LayoutInflater mInflater;

    public BaseAdapter(Context context, List<Comic> list) {
        mContext = context;
        mDataSet = list;
        mInflater = LayoutInflater.from(context);
    }

    public void update(Comic data) {
        mDataSet.remove(data);
        mDataSet.add(data);
        notifyDataSetChanged();
    }

    public void add(Comic data) {
        mDataSet.add(data);
        notifyDataSetChanged();
    }

    public void remove(Comic data) {
        if (mDataSet.remove(data)) {
            notifyDataSetChanged();
        }
    }

    public void remove(int position) {
        mDataSet.remove(position);
        notifyDataSetChanged();
    }

    public void setData(List<Comic> list) {
        mDataSet.clear();
        mDataSet.addAll(list);
        notifyDataSetChanged();
    }

    public Comic getItem(int position) {
        return mDataSet.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}
