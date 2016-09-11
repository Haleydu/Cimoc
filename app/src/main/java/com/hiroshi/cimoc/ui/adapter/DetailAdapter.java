package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderFactory;
import com.hiroshi.cimoc.model.Chapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailAdapter extends BaseAdapter<Chapter> {

    private int source;
    private String title;
    private String image;
    private String update;
    private String author;
    private String intro;
    private Boolean status;

    private String last;
    private int backgroundId;
    private int colorId;

    public class ViewHolder extends BaseViewHolder {
        @BindView(R.id.item_chapter_button) TextView chapterButton;

        public ViewHolder(View view) {
            super(view);
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

    public DetailAdapter(Context context, List<Chapter> list) {
        super(context, list);
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(R.attr.backgroundChapter, typedValue, true);
        backgroundId = typedValue.resourceId;
        mContext.getTheme().resolveAttribute(R.attr.colorChapterText, typedValue, true);
        colorId = typedValue.resourceId;
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

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        return mDataSet.size() + 1;
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

    public void setInfo(int source, String image, String title, String author, String intro, Boolean status, String update, String last) {
        this.source = source;
        this.image = image;
        this.title = title;
        this.intro = intro;
        this.status = status;
        this.update = update;
        this.author = author;
        this.last = last;
    }

    public void setDownload(boolean[] download) {
        for (int i = 0; i != download.length; ++i) {
            mDataSet.get(i).setDownload(download[i]);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            PipelineDraweeControllerBuilder builder = ControllerBuilderFactory.get(mContext, source);
            if (title != null) {
                headerHolder.mComicImage.setController(builder.setUri(image).build());
                headerHolder.mComicTitle.setText(title);
                headerHolder.mComicIntro.setText(intro);
                if (status != null) {
                    headerHolder.mComicStatus.setText(status ? "完结" : "连载中");
                }
                headerHolder.mComicUpdate.setText(update);
                headerHolder.mComicAuthor.setText(author);
            }
        } else {
            Chapter chapter = mDataSet.get(position - 1);
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.chapterButton.setText(chapter.getTitle());
            if (chapter.isDownload()) {
                viewHolder.chapterButton.setBackgroundResource(R.drawable.button_chapter_download);
                viewHolder.chapterButton.setTextColor(mContext.getResources().getColorStateList(R.color.button_chapter_color_download));
            } else {
                viewHolder.chapterButton.setBackgroundResource(backgroundId);
                viewHolder.chapterButton.setTextColor(mContext.getResources().getColorStateList(colorId));
            }
            if (chapter.getPath().equals(last)) {
                viewHolder.chapterButton.setSelected(true);
            } else if (viewHolder.chapterButton.isSelected()) {
                viewHolder.chapterButton.setSelected(false);
            }
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

    public void setLast(String value) {
        if (value.equals(last)) {
            last = value;
            return;
        }
        String temp = last;
        last = value;
        for (int i = 0; i != mDataSet.size(); ++i) {
            String path = mDataSet.get(i).getPath();
            if (path.equals(last)) {
                notifyItemChanged(i + 1);
            } else if (path.equals(temp)) {
                notifyItemChanged(i + 1);
            }
        }
    }

    public List<String> getTitles() {
        List<String> list = new ArrayList<>(mDataSet.size());
        for (Chapter chapter : mDataSet) {
            list.add(chapter.getTitle());
        }
        return list;
    }

    public String[] getPaths() {
        int size = mDataSet.size();
        String[] paths = new String[size];
        for (int i = 0; i != size; ++i) {
            paths[i] = mDataSet.get(i).getPath();
        }
        return paths;
    }

}
