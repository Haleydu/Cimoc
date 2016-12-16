package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class GridAdapter extends BaseAdapter<MiniComic> {

    public static int GRID_ITEM_TYPE = 2016101213;

    private ControllerBuilderProvider mProvider;
    private boolean symbol = false;

    static class GridHolder extends BaseViewHolder {
        @BindView(R.id.item_grid_image) SimpleDraweeView comicImage;
        @BindView(R.id.item_grid_title) TextView comicTitle;
        @BindView(R.id.item_grid_subtitle) TextView comicSource;
        @BindView(R.id.item_grid_symbol) View comicHighlight;

        GridHolder(View view) {
            super(view);
        }
    }

    public GridAdapter(Context context, List<MiniComic> list) {
        super(context, list);
    }

    @Override
    public int getItemViewType(int position) {
        return GRID_ITEM_TYPE;
    }

    @Override
    public GridHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_grid, parent, false);
        return new GridHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        MiniComic comic = mDataSet.get(position);
        GridHolder gridHolder = (GridHolder) holder;
        gridHolder.comicTitle.setText(comic.getTitle());
        gridHolder.comicSource.setText(SourceManager.getTitle(comic.getSource()));
        if (mProvider != null) {
            DraweeController controller = mProvider.get(comic.getSource())
                    .setOldController(gridHolder.comicImage.getController())
                    .setUri(comic.getCover())
                    .build();
            gridHolder.comicImage.setController(controller);
        }
        gridHolder.comicHighlight.setVisibility(symbol && comic.isHighlight() ? View.VISIBLE : View.INVISIBLE);
    }

    public void setProvider(ControllerBuilderProvider provider) {
        mProvider = provider;
    }

    public void setSymbol(boolean symbol) {
        this.symbol = symbol;
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, 0, offset, (int) (2.8 * offset));
            }
        };
    }

    public void removeItemById(long id) {
        for (MiniComic comic : mDataSet) {
            if (id == comic.getId()) {
                remove(comic);
                break;
            }
        }
    }

    public MiniComic getItemById(long id) {
        for (MiniComic comic : mDataSet) {
            if (comic.getId() == id) {
                return comic;
            }
        }
        return null;
    }

    public int findFirstNotHighlight() {
        int count = 0;
        if (symbol) {
            for (MiniComic comic : mDataSet) {
                if (!comic.isHighlight()) {
                    break;
                }
                ++count;
            }
        }
        return count;
    }

}
