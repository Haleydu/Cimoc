package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class ChapterAdapter extends BaseAdapter<Chapter> {

    private String image;
    private String title;
    private String intro;

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_chapter_button) Button chapterButton;

        public ViewHolder(View view) {
            super(view);
        }

    }

    public class HeaderHolder extends BaseViewHolder {

        @BindView(R.id.item_header_comic_image) ImageView mComicImage;
        @BindView(R.id.item_header_comic_title) TextView mComicTitle;
        @BindView(R.id.item_header_comic_intro) TextView mComicIntro;

        public HeaderHolder(View view) {
            super(view);
        }

    }

    public ChapterAdapter(Context context, List<Chapter> list, String image, String title, String intro) {
        super(context, list);
        this.image = image;
        this.title = title;
        this.intro = intro;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = mInflater.inflate(R.layout.item_chapter_header, parent, false);
            return new HeaderHolder(view);
        }
        View view = mInflater.inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            Picasso.with(mContext).load(image).into(headerHolder.mComicImage);
            headerHolder.mComicTitle.setText(title);
            headerHolder.mComicIntro.setText(intro);
        } else {
            Chapter chapter = mDataSet.get(position - 1);
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.chapterButton.setText(chapter.getTitle());
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? manager.getSpanCount() : 1;
            }
        });
    }
}
