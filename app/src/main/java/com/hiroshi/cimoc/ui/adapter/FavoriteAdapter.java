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
        int visibility = mDataSet.get(position).isHighlight() ? View.VISIBLE : View.INVISIBLE;
        ((ViewHolder) holder).comicNew.setVisibility(visibility);
    }

    @Override
    public void add(MiniComic data) {
        add(findFirstNotHighlight(), data);
    }

    public void moveToFirst(MiniComic comic) {
        remove(comic);
        add(0, comic);
    }

    public MiniComic clickItem(int which) {
        final MiniComic comic = mDataSet.get(which);
        if (comic.isHighlight()) {
            comic.setHighlight(false);
            remove(which);
            int pos = findFirstNotHighlight();
            add(pos, comic);
            notifyItemInserted(pos);
        }
        return comic;
    }

    private int findFirstNotHighlight() {
        int count = 0;
        for (MiniComic comic : mDataSet) {
            if (!comic.isHighlight()) {
                break;
            }
            ++count;
        }
        return count;
    }

}
