package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class ChapterAdapter extends BaseAdapter<Chapter> {

    private String image;
    private String title;
    private String intro;
    private String status;
    private String update;
    private String author;

    private String last;

    public class ViewHolder extends BaseViewHolder {

        @BindView(R.id.item_chapter_button) Button chapterButton;

        public ViewHolder(View view) {
            super(view);
        }

        public void select() {
            chapterButton.setBackgroundResource(R.drawable.button_chapter_checked);
            chapterButton.setTextColor(Color.WHITE);
        }

        public void clear() {
            chapterButton.setBackgroundResource(R.drawable.button_chapter);
            chapterButton.setTextColor(Color.parseColor("#7F7F80"));
        }

    }

    public class HeaderHolder extends BaseViewHolder {

        @BindView(R.id.item_header_comic_image) SimpleDraweeView mComicImage;
        @BindView(R.id.item_header_comic_title) TextView mComicTitle;
        @BindView(R.id.item_header_comic_intro) TextView mComicIntro;
        @BindView(R.id.item_header_comic_status) TextView mComicStatus;
        @BindView(R.id.item_header_comic_update) TextView mComicUpdate;
        @BindView(R.id.item_header_comic_author) TextView mComicAuthor;

        public HeaderHolder(View view) {
            super(view);
        }

    }

    public ChapterAdapter(Context context, List<Chapter> list, String image, String title, String author, String intro, String status, String update, String last) {
        super(context, list);
        this.image = image;
        this.title = title;
        this.intro = intro;
        this.status = status;
        this.update = update;
        this.author = author;
        this.last = last;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildLayoutPosition(view);
                if (position == 0) {
                    outRect.set(0, 0, 0, 10);
                } else {
                    outRect.set(30, 0, 30, 40);
                }
            }
        };
    }

    public int getPositionByPath(String path) {
        if (path == null) {
            return -1;
        }
        for (int i = 0; i != mDataSet.size(); ++i) {
            if (mDataSet.get(i).getPath().equals(path)) {
                return i + 1;
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size() + 1;
    }

    @Override
    public Chapter getItem(int position) {
        return super.getItem(position - 1);
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
            headerHolder.mComicImage.setImageURI(image);
            headerHolder.mComicTitle.setText(title);
            headerHolder.mComicIntro.setText(intro);
            headerHolder.mComicStatus.setText(status);
            headerHolder.mComicUpdate.setText(update);
            headerHolder.mComicAuthor.setText(author);
        } else {
            Chapter chapter = mDataSet.get(position - 1);
            ViewHolder viewHolder = (ViewHolder) holder;
            if (last != null && last.equals(chapter.getPath())) {
                viewHolder.select();
            }
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
