package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilderSupplier;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.ui.widget.ChapterButton;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailAdapter extends BaseAdapter<Chapter> {

    private PipelineDraweeControllerBuilderSupplier mControllerSupplier;

    public String title;
    private String cover;
    private String update;
    private String author;
    public String intro;
    private Boolean finish;

    private String last;

    public DetailAdapter(Context context, List<Chapter> list) {
        super(context, list);
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
                    int offset = parent.getWidth() / 40;
                    outRect.set(offset, 0, offset, (int) (offset * 1.5));
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
        return new ChapterHolder(view);
    }

    public void setInfo(String cover, String title, String author, String intro, Boolean finish, String update, String last) {
        this.cover = cover;
        this.title = title;
        this.intro = intro;
        this.finish = finish;
        this.update = update;
        this.author = author;
        this.last = last;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (position == 0) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            AdRequest adRequest = new AdRequest.Builder().build();
            headerHolder.mAdView.loadAd(adRequest);
            if (title != null) {
                if (cover != null) {
                    headerHolder.mComicImage.setController(mControllerSupplier.get().setUri(cover).build());
                }
                headerHolder.mComicTitle.setText(title);
                headerHolder.mComicIntro.setText(intro);
                if (finish != null) {
                    headerHolder.mComicStatus.setText(finish ? "完结" : "连载中");
                }
                if (update != null) {
                    headerHolder.mComicUpdate.setText("最后更新：".concat(update));
                }
                headerHolder.mComicAuthor.setText(author);
            }
        } else {
            Chapter chapter = mDataSet.get(position - 1);
            ChapterHolder viewHolder = (ChapterHolder) holder;
            viewHolder.chapterButton.setText(chapter.getTitle());
            viewHolder.chapterButton.setDownload(chapter.isComplete());
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

    public void setControllerSupplier(PipelineDraweeControllerBuilderSupplier supplier) {
        this.mControllerSupplier = supplier;
    }

    public void setLast(String value) {
        if (value == null || value.equals(last)) {
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

    static class ChapterHolder extends BaseViewHolder {
        @BindView(R.id.item_chapter_button)
        ChapterButton chapterButton;

        ChapterHolder(View view) {
            super(view);
        }
    }

    class HeaderHolder extends BaseViewHolder {
        @BindView(R.id.item_header_comic_image)
        SimpleDraweeView mComicImage;
        @BindView(R.id.item_header_comic_title)
        TextView mComicTitle;
        @BindView(R.id.item_header_comic_intro)
        TextView mComicIntro;
        @BindView(R.id.item_header_comic_status)
        TextView mComicStatus;
        @BindView(R.id.item_header_comic_update)
        TextView mComicUpdate;
        @BindView(R.id.item_header_comic_author)
        TextView mComicAuthor;
        @BindView(R.id.adView_chapter)
        AdView mAdView;

        HeaderHolder(View view) {
            super(view);
        }
    }

}
