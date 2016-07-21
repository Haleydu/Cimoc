package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultAdapter extends BaseAdapter<Comic> {

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.result_comic_image) SimpleDraweeView comicImage;
        @BindView(R.id.result_comic_title) TextView comicTitle;
        @BindView(R.id.result_comic_author) TextView comicAuthor;
        @BindView(R.id.result_comic_update) TextView comicUpdate;
        @BindView(R.id.result_comic_source) TextView comicSource;

        public ViewHolder(View view) {
            super(view);
        }
    }

    public ResultAdapter(Context context, List<Comic> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Comic comic = mDataSet.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.comicTitle.setText(comic.getTitle());
        viewHolder.comicAuthor.setText(comic.getAuthor());
        viewHolder.comicSource.setText(Kami.getSourceById(comic.getSource()));
        viewHolder.comicUpdate.setText(comic.getUpdate());
        PipelineDraweeControllerBuilder builder = ControllerBuilderFactory.getControllerBuilder(comic.getSource());
        viewHolder.comicImage.setController(builder.setUri(comic.getImage()).build());
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 10);
            }
        };
    }
}
