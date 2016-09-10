package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.MiniComic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class FavoriteAdapter extends ComicAdapter {

    private Set<String> mFilterSet;
    private List<MiniComic> mFullList;

    public FavoriteAdapter(Context context) {
        super(context, new LinkedList<MiniComic>());
        mFullList = new LinkedList<>();
        mFilterSet = new HashSet<>();
        mFilterSet.add("已完结");
        mFilterSet.add("连载中");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (mDataSet.get(position).isHighlight()) {
            ((ViewHolder) holder).comicNew.setVisibility(View.VISIBLE);
        } else {
            ((ViewHolder) holder).comicNew.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void add(MiniComic comic) {
        mFullList.add(findFirstNormal(mFullList), comic);
        if (isFilter(comic)) {
            super.add(findFirstNormal(mDataSet), comic);
        }
    }

    @Override
    public void addAll(List<MiniComic> list) {
        mFullList.addAll(findFirstNormal(mFullList), list);
        List<MiniComic> temp = new LinkedList<>();
        for (MiniComic comic : list) {
            if (isFilter(comic)) {
                temp.add(comic);
            }
        }
        if (!temp.isEmpty()) {
            super.addAll(findFirstNormal(mDataSet), temp);
        }
    }

    public void moveToFirst(MiniComic comic) {
        mFullList.remove(comic);
        mFullList.add(0, comic);

        int position = mDataSet.indexOf(comic);
        if (position != -1) {
            mDataSet.remove(position);
            mDataSet.add(0, comic);
            notifyItemMoved(position, 0);
        }
    }

    public MiniComic cancelHighlight(int position) {
        MiniComic comic = mDataSet.get(position);
        if (comic.isHighlight()) {
            comic.setHighlight(false);

            mFullList.remove(comic);
            int temp = findFirstNormal(mFullList);
            mFullList.add(temp, comic);

            mDataSet.remove(position);
            temp = findFirstNormal(mDataSet);
            mDataSet.add(temp, comic);
            notifyItemMoved(position, temp);
            notifyItemChanged(temp);
        }
        return comic;
    }

    public void updateFilter(String[] filter, boolean[] checked) {
        for (int i = 0; i != checked.length; ++i) {
            if (checked[i]) {
                mFilterSet.add(filter[i]);
            } else {
                mFilterSet.remove(filter[i]);
            }
        }

        List<MiniComic> list = new LinkedList<>();
        for (MiniComic comic : mFullList) {
            if (isFilter(comic)) {
                list.add(comic);
            }
        }
        mDataSet = list;
        notifyDataSetChanged();
    }

    @Override
    public void removeById(long id) {
        for (MiniComic comic : mFullList) {
            if (id == comic.getId()) {
                mFullList.remove(comic);
                break;
            }
        }
        super.removeById(id);
    }

    @Override
    public void removeBySource(int source) {
        Iterator<MiniComic> iterator = mFullList.iterator();
        while (iterator.hasNext()) {
            MiniComic comic = iterator.next();
            if (source == comic.getSource()) {
                iterator.remove();
            }
        }
        super.removeBySource(source);
    }

    public boolean isFull() {
        return mDataSet.size() == mFullList.size();
    }

    public int getFullSize() {
        return mFullList.size();
    }

    private boolean isFilter(MiniComic comic) {
        return mFilterSet.contains(comic.isFinish() ? "已完结" : "连载中") ||
                mFilterSet.contains(SourceManager.getTitle(comic.getSource()));
    }

    private int findFirstNormal(List<MiniComic> list) {
        int index = 0;
        for (MiniComic comic : list) {
            if (!comic.isHighlight()) {
                break;
            }
            ++index;
        }
        return index;
    }

}
