package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Selectable;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/4.
 */
public class SelectAdapter extends BaseAdapter<Selectable> {

    class SelectHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_select_title) TextView chapterTitle;
        @BindView(R.id.item_select_checkbox) CheckBox chapterChoice;

        SelectHolder(View view) {
            super(view);
        }
    }

    public SelectAdapter(Context context, List<Selectable> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_select, parent, false);
        return new SelectHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        SelectHolder viewHolder = (SelectHolder) holder;
        Selectable selectable = mDataSet.get(position);
        viewHolder.chapterTitle.setText(selectable.getTitle());
        viewHolder.chapterChoice.setEnabled(!selectable.isDisable());
        viewHolder.chapterChoice.setChecked(selectable.isChecked());
    }

    public List<Integer> getCheckedList() {
        List<Integer> list = new LinkedList<>();
        int size = mDataSet.size();
        for (int i = 0; i != size; ++i) {
            if (mDataSet.get(i).isChecked() && !mDataSet.get(i).isDisable()) {
                list.add(i);
            }
        }
        return list;
    }

    public void checkAll() {
        for (Selectable selectable : mDataSet) {
            selectable.setChecked(true);
        }
    }

    public void refreshChecked() {
        for (Selectable selectable : mDataSet) {
            if (selectable.isChecked() && !selectable.isDisable()) {
                selectable.setDisable(true);
            }
        }
    }

}
