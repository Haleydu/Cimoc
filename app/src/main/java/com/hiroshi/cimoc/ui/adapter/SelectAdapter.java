package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;

import java.util.ArrayList;
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
    }

    public List<Integer> getCheckedList() {
        List<Integer> list = new ArrayList<>(mStateArray.length);
        for (int i = 0; i != mStateArray.length; ++i) {
            if (mStateArray[i].checked && !mStateArray[i].disable) {
                list.add(i);
            }
        }
        return list;
    }

    public void checkAll() {
        for (BoxState state : mStateArray) {
            state.checked = true;
        }
    }

    public void refreshChecked() {
        for (BoxState state : mStateArray) {
            if (state.checked && !state.disable) {
                state.disable = true;
            }
        }
    }

}
