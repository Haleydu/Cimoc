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
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultAdapter extends BaseAdapter<MiniComic> {

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.result_comic_image) ImageView comicImage;
        @BindView(R.id.result_comic_title) TextView comicTitle;
        @BindView(R.id.result_comic_author) TextView comicAuthor;
        @BindView(R.id.result_comic_update) TextView comicUpdate;
        @BindView(R.id.result_comic_source) TextView comicSource;

        public ViewHolder(View view) {
            super(view);
        }

    }

    public ResultAdapter(Context context, List<MiniComic> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MiniComic comic = mDataSet.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.comicTitle.setText(comic.getTitle());
        viewHolder.comicAuthor.setText(comic.getAuthor());
        String source = "来源于：" + Administrator.getSourceById(comic.getSource());
        viewHolder.comicSource.setText(source);
        String update = "最后更新：" + comic.getUpdate();
        viewHolder.comicUpdate.setText(update);
        viewHolder.comicImage.setImageResource(R.drawable.test1);
    }

}
