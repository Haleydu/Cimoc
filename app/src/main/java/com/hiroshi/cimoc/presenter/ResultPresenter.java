package com.hiroshi.cimoc.presenter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.BaseSearch;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class ResultPresenter extends BasePresenter {

    private ResultActivity mResultActivity;
    private BaseSearch mMangaSearch;
    private boolean isLoading;

    public ResultPresenter(ResultActivity activity) {
        mResultActivity = activity;
        isLoading = false;
    }

    public RecyclerView.OnScrollListener getScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem = mResultActivity.findLastItemPosition();
                int itemCount = mResultActivity.getItemCount();
                if (lastItem >= itemCount - 4 && dy > 0) {
                    if (!isLoading) {
                        isLoading = true;
                        mMangaSearch.next();
                    }
                }
            }
        };
    }

    public BaseAdapter.OnItemClickListener getItemClickListener() {
        return new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Comic comic = mResultActivity.getItem(position);
                Intent intent = DetailActivity.createIntent(mResultActivity, comic);
                mResultActivity.startActivity(intent);
            }
        };
    }

    public void initManga(String keyword, int source) {
        mMangaSearch = Kami.getSearchById(source);
        mMangaSearch.first(keyword);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.SEARCH_SUCCESS:
                mResultActivity.addAll((List<Comic>) msg.getData());
                isLoading = false;
                break;
            case EventMessage.SEARCH_EMPTY:
                break;
        }
    }

}
