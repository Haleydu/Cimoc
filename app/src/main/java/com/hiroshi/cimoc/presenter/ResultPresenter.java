package com.hiroshi.cimoc.presenter;

import android.content.Intent;

import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.model.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class ResultPresenter extends BasePresenter {

    private ResultActivity mResultActivity;

    private Manga mManga;
    private String keyword;
    private int page;
    private boolean isLoading;
    private boolean isInit;

    public ResultPresenter(ResultActivity activity, int source, String keyword) {
        this.mResultActivity = activity;
        this.mManga = Kami.getMangaById(source);
        this.keyword = keyword;
        this.page = 1;
        this.isLoading = false;
        this.isInit = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManga.search(keyword, page++);
    }

    public void onScrolled(int dy) {
        int lastItem = mResultActivity.findLastItemPosition();
        int itemCount = mResultActivity.getItemCount();
        if (lastItem >= itemCount - 4 && dy > 0) {
            if (!isLoading) {
                isLoading = true;
                mManga.search(keyword, page++);
            }
        }
    }

    public void onItemClick(int position) {
        Comic comic = mResultActivity.getItem(position);
        Intent intent = DetailActivity.createIntent(mResultActivity, comic);
        mResultActivity.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.SEARCH_SUCCESS:
                List list = (List<Comic>) msg.getData();
                mResultActivity.hideProgressBar();
                if (!list.isEmpty()) {
                    mResultActivity.addAll(list);
                    isLoading = false;
                    isInit = false;
                } else if (isInit) {
                    mResultActivity.showSnackbar("搜索结果为空 :)");
                }
                break;
            case EventMessage.NETWORK_ERROR:
                mResultActivity.hideProgressBar();
                mResultActivity.showSnackbar("网络错误 :(");
                break;
        }
    }

}
