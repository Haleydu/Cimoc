package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.collections.FilterList;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.utils.CollectionUtils;

import java.util.List;
import java.util.Set;

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

    public void moveToFirst(MiniComic comic) {
        remove(comic);
        add(0, comic);
    }

    public MiniComic clickItem(int which) {
        MiniComic comic = mDataSet.get(which);
        if (comic.isHighlight()) {
            comic.setHighlight(false);
            remove(which);
            CollectionUtils.Condition<MiniComic> condition = new CollectionUtils.Condition<MiniComic>() {
                @Override
                public boolean call(int position, MiniComic element) {
                    return !element.isHighlight();
                }
            };
            int position = CollectionUtils.findFirstFromList(mDataSet, condition);
            int[] index = {CollectionUtils.findFirstFromList(filterList.getFullList(), condition), position};
            filterList.addDiff(index, comic);
            notifyItemInserted(position);
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
        List<MiniComic> list = CollectionUtils.findAllToList(filterList.getFullList(), new CollectionUtils.Condition<MiniComic>() {
            @Override
            public boolean call(int position, MiniComic element) {
                return source == element.getSource();
            }
        });
        removeAll(list);
    }

    public boolean isFull() {
        return filterList.isFull();
    }

}
