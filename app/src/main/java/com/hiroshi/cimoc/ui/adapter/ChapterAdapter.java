package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class ChapterAdapter extends BaseAdapter<Chapter> {

    private int source;
    private String title;
    private String image;
    private String update;
    private String author;
    private String intro;
    private boolean status;

    private String last;

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

    public ChapterAdapter(Context context, List<Chapter> list, int source, String image, String title, String author, String intro, boolean status, String update, String last) {
        super(context, list);
        this.source = source;
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
            PipelineDraweeControllerBuilder builder = ControllerBuilderFactory.getControllerBuilder(source, mContext);
            headerHolder.mComicImage.setController(builder.setUri(image).build());
            headerHolder.mComicTitle.setText(title);
            headerHolder.mComicIntro.setText(intro);
            headerHolder.mComicStatus.setText(status ? "完结" : "连载中");
            headerHolder.mComicUpdate.setText(update);
            headerHolder.mComicAuthor.setText(author);
        } else {
            Chapter chapter = mDataSet.get(position - 1);
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.chapterButton.setText(chapter.getTitle());
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

}
