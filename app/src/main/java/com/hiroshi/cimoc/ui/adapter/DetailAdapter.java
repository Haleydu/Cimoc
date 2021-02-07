package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface;
import android.text.TextPaint;
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
    //漫画源中倒序
    private Boolean isReverseOrder;
    //用户手动调整顺序
    private Boolean Reversed = false;

    private String last;
    Paint textPaint;

    Paint paint;

    public DetailAdapter(Context context, List<Chapter> list) {
        super(context, list);
        textPaint = new TextPaint();
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40);//文字大小
        textPaint.setColor(Color.BLACK);//背景颜色
        textPaint.setTextAlign(Paint.Align.LEFT);
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context, R.color.transparent));

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
                    if (isFirst(position)) {
                        int offset = parent.getWidth() / 40;

                        outRect.set(offset, 50, offset, (int) (offset * 1.5));

                    } else {
                        int offset = parent.getWidth() / 40;
                        outRect.set(offset, 0, offset, (int) (offset * 1.5));
                    }


                }
            }

            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.onDraw(c, parent, state);
                if (parent.getAdapter() != null) {
                    RecyclerView.Adapter adapter = parent.getAdapter();
                    int count = parent.getChildCount();
                    for (int i = 1; i < count; i++) {
                        View view = parent.getChildAt(i);
                        int position = parent.getChildLayoutPosition(view);
                        boolean isHeader = isFirst(position);

                        if (isHeader) {
                            float top = view.getTop() - 60;
                            float bottom = view.getTop() - 30;
                            Rect rect = new Rect(parent.getPaddingLeft(), (int) top, parent.getWidth() - parent.getPaddingRight(), (int) bottom);
                            c.drawRect(rect, paint);
                            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                            float baseline = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top) / 2;
                            textPaint.setTextAlign(Paint.Align.CENTER);
                            c.drawText(getGroupName(position), rect.centerX(), baseline, textPaint);


                        }  //                            c.drawRect(0, view.getTop() - 1, parent.getWidth(), view.getTop(), mLinePaint);

                    }
                }
            }
        };
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    @Override
    public void reverse() {
        this.Reversed = !this.Reversed;
        super.reverse();
    }

    //判断是否分组且当前是否为第一个元素
    public boolean isFirst(int position) {
        position = position - 1;
        if (mDataSet.get(position).getSourceGroup().isEmpty()) return false;
        if (position == 0) return true;
        if (position <= 0) {
            return false;
        }
        return !mDataSet.get(position - 1).getSourceGroup().equals(mDataSet.get(position).getSourceGroup());
    }

    public String getGroupName(int position) {
        return mDataSet.get(position - 1).getSourceGroup();

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

    public void setInfo(String cover, String title, String author, String intro, Boolean finish, String update, String last, Boolean isReverseOrder) {
        this.cover = cover;
        this.title = title;
        this.intro = intro;
        this.finish = finish;
        this.update = update;
        this.author = author;
        this.last = last;
        this.isReverseOrder = isReverseOrder;
    }

    public boolean isReverseOrder() {
        return isReverseOrder;
    }

    public boolean isReversed() {
        return Reversed;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        try {
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
                if (chapter.getPath() != null && chapter.getPath().equals(last)) {
                    viewHolder.chapterButton.setSelected(true);
                } else if (viewHolder.chapterButton.isSelected()) {
                    viewHolder.chapterButton.setSelected(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0 || isFirst(position)) return manager.getSpanCount();

                return 1;
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
