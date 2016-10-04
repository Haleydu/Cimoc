package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.collections.FilterList;
import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class FavoriteAdapter extends ComicAdapter {

    private FilterList<MiniComic, String> filterList;

    public FavoriteAdapter(Context context, FilterList<MiniComic, String> filterList) {
        super(context, filterList);
        this.filterList = filterList;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        int visibility = mDataSet.get(position).isHighlight() ? View.VISIBLE : View.INVISIBLE;
        ((ViewHolder) holder).comicNew.setVisibility(visibility);
    }

    @Override
    public void add(MiniComic data) {
        add(findFirstNotHighlight(mDataSet), data);
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
            int index1 = findFirstNotHighlight(mDataSet);
            int index2 = findFirstNotHighlight(filterList.getFullList());
            filterList.addDiff(index1, index2, comic);
            notifyItemInserted(index1);
        }
        return comic;
    }

    public void updateFilterSet() {
        filterList.filter();
        notifyDataSetChanged();
    }

    public Set<String> getFilterSet() {
        return filterList.getFilterSet();
    }

    @Override
    public void removeBySource(final int source) {
        Observable.from(filterList.getFullList())
                .filter(new Func1<MiniComic, Boolean>() {
                    @Override
                    public Boolean call(MiniComic comic) {
                        return source == comic.getSource();
                    }
                })
                .toList()
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        removeAll(list);
                    }
                });
    }

    public boolean isFull() {
        return filterList.isFull();
    }

    private int findFirstNotHighlight(List<MiniComic> list) {
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
