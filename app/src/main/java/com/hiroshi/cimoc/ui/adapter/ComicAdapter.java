package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Administrator;
import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class ComicAdapter extends BaseAdapter<MiniComic> {

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_comic_image) ImageView comicImage;
        @BindView(R.id.item_comic_title) TextView comicTitle;
        @BindView(R.id.item_comic_source) TextView comicSource;

        public ViewHolder(View view) {
            super(view);
        }

    }

    public ComicAdapter(Context context, List<MiniComic> list) {
        super(context, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_comic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MiniComic comic = mDataSet.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.comicImage.setImageResource(R.drawable.test1);
        viewHolder.comicTitle.setText(comic.getTitle());
        viewHolder.comicSource.setText(Administrator.getSourceById(comic.getSource()));
    }

}
