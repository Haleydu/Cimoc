package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/4.
 */
public class SelectAdapter extends BaseAdapter<String> {

    class BoxState {
        boolean disable;
        boolean checked;
    }

    private BoxState[] mStateArray;
    private List<Integer> mCheckList;

    public class ChapterHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.download_chapter_title) TextView chapterTitle;
        @BindView(R.id.download_chapter_checkbox) CheckBox chapterChoice;

        public ChapterHolder(View view) {
            super(view);
        }
    }

    public SelectAdapter(Context context, List<String> list) {
        super(context, list);
    }

    public void initState(boolean[] array) {
        mCheckList = new LinkedList<>();
        mStateArray = new BoxState[array.length];
        for (int i = 0; i != array.length; ++i) {
            mStateArray[i] = new BoxState();
            mStateArray[i].checked = array[i];
            mStateArray[i].disable = array[i];
        }
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
        viewHolder.chapterChoice.setEnabled(!mStateArray[position].disable);
        viewHolder.chapterChoice.setChecked(mStateArray[position].checked);
    }

    public void onClick(int position, boolean checked) {
        mStateArray[position].checked = checked;
        if (checked) {
            mCheckList.add(position);
        } else {
            mCheckList.remove(position);
        }
    }

    public List<Integer> getCheckedList() {
        return mCheckList;
    }

    public void clearCheckedList(boolean download) {
        if (download) {
            for (int position : mCheckList) {
                mStateArray[position].disable = true;
            }
        }
        mCheckList.clear();
    }

}
