package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.db.entity.FavoriteComic;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class ComicAdapter extends BaseAdapter<FavoriteComic> {

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_comic_image) ImageView comicImage;
        @BindView(R.id.item_comic_title) TextView comicTitle;
        @BindView(R.id.item_comic_source) TextView comicSource;

        public ViewHolder(View view) {
            super(view);
        }

    }

    public ComicAdapter(Context context, List<FavoriteComic> list) {
        super(context, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_comic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FavoriteComic comic = mDataSet.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.comicTitle.setText(comic.getTitle());
        viewHolder.comicSource.setText(Kami.getSourceById(comic.getSource()));
        Picasso.with(mContext).load(comic.getImage()).into(viewHolder.comicImage);
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(10, 0, 10, 30);
            }
        };
    }

    public void removeById(long id) {
        for (FavoriteComic comic : mDataSet) {
            if (comic.getId() == id) {
                remove(comic);
            }
        }
    }

}
