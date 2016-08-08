package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class FavoriteAdapter extends ComicAdapter {

    public FavoriteAdapter(Context context, List<MiniComic> list) {
        super(context, list);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (mDataSet.get(position).getStatus()) {
            ((ViewHolder) holder).comicNew.setVisibility(View.VISIBLE);
        } else {
            ((ViewHolder) holder).comicNew.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void add(MiniComic data) {
        super.add(findFirstNormal(), data);
    }

    @Override
    public void addAll(List<MiniComic> data) {
        super.addAll(findFirstNormal(), data);
    }

    public void updateAll(List<MiniComic> list) {
        mDataSet.removeAll(list);
        mDataSet.addAll(0, list);
        notifyDataSetChanged();
    }

    public MiniComic cancelNew(int position) {
        MiniComic comic = mDataSet.get(position);
        if (comic.getStatus()) {
            comic.setStatus(false);
            mDataSet.remove(position);
            int temp = findFirstNormal();
            mDataSet.add(temp, comic);
            notifyItemMoved(position, temp);
            notifyItemChanged(temp);
        }
        return comic;
    }

    private int findFirstNormal() {
        int index = 0;
        for (MiniComic comic : mDataSet) {
            if (!comic.getStatus()) {
                break;
            }
            ++index;
        }
        return index;
    }

}
