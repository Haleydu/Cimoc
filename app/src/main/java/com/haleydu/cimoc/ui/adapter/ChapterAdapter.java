package com.haleydu.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.misc.Switcher;
import com.haleydu.cimoc.model.Chapter;
import com.haleydu.cimoc.ui.widget.ChapterButton;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/11/15.
 */

public class ChapterAdapter extends BaseAdapter<Switcher<Chapter>> {

    private static final int TYPE_ITEM = 2017030222;
    private static final int TYPE_BUTTON = 2017030223;

    private boolean isButtonMode = false;

    public ChapterAdapter(Context context, List<Switcher<Chapter>> list) {
        super(context, list);
    }

    @Override
    public int getItemViewType(int position) {
        return isButtonMode ? TYPE_BUTTON : TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = mInflater.inflate(R.layout.item_select, parent, false);
            return new ItemHolder(view);
        }
        View view = mInflater.inflate(R.layout.item_chapter, parent, false);
        return new ButtonHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Switcher<Chapter> switcher = mDataSet.get(position);
        if (isButtonMode) {
            final ButtonHolder viewHolder = (ButtonHolder) holder;
            viewHolder.chapterButton.setText(switcher.getElement().getTitle());
            if (switcher.getElement().isDownload()) {
                viewHolder.chapterButton.setDownload(true);
                viewHolder.chapterButton.setSelected(false);
            } else {
                viewHolder.chapterButton.setDownload(false);
                viewHolder.chapterButton.setSelected(switcher.isEnable());
            }
        } else {
            ItemHolder viewHolder = (ItemHolder) holder;
            viewHolder.chapterTitle.setText(switcher.getElement().getTitle());
            viewHolder.chapterChoice.setEnabled(!switcher.getElement().isDownload());
            viewHolder.chapterChoice.setChecked(switcher.isEnable());
        }
    }

    public void setButtonMode(boolean enable) {
        isButtonMode = enable;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 40;
                outRect.set(offset, 0, offset, (int) (offset * 1.5));
            }
        };
    }

    @Override
    protected boolean isClickValid() {
        return true;
    }

    static class ItemHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_select_title)
        TextView chapterTitle;
        @BindView(R.id.item_select_checkbox)
        CheckBox chapterChoice;

        ItemHolder(View view) {
            super(view);
        }
    }

    static class ButtonHolder extends BaseAdapter.BaseViewHolder {
        @BindView(R.id.item_chapter_button)
        ChapterButton chapterButton;

        ButtonHolder(View view) {
            super(view);
        }
    }

}
