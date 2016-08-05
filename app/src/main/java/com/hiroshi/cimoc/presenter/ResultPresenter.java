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

    public ResultPresenter(ResultActivity activity, int source, String keyword) {
        this.mResultActivity = activity;
        this.mManga = Kami.getMangaById(source);
        this.keyword = keyword;
        this.page = 0;
        this.isLoading = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManga.search(keyword, ++page);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManga.cancel();
    }

    public void onScrolled(int dy, int last, int count) {
        if (last >= count - 4 && dy > 0 && !isLoading) {
            isLoading = true;
            mManga.search(keyword, ++page);
        }
    }

    public void onItemClick(Comic comic) {
        Intent intent = DetailActivity.createIntent(mResultActivity, comic.getId(), comic.getSource(), comic.getCid());
        mResultActivity.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.SEARCH_SUCCESS:
                mResultActivity.hideProgressBar();
                mResultActivity.addAll((List<Comic>) msg.getData());
                isLoading = false;
                break;
            case EventMessage.SEARCH_FAIL:
                if (page == 1) {
                    mResultActivity.hideProgressBar();
                    mResultActivity.showSnackbar("搜索结果为空");
                }
                break;
            case EventMessage.NETWORK_ERROR:
                mResultActivity.hideProgressBar();
                mResultActivity.showSnackbar("网络错误");
                isLoading = false;
                break;
        }
    }

}
