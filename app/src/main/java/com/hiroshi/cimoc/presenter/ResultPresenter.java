package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.ResultActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
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
        this.mManga = SourceManager.getManga(source);
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

    public void loadNext() {
        if (!isLoading) {
            isLoading = true;
            mManga.search(keyword, ++page);
        }
    }

    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.SEARCH_SUCCESS:
                mResultActivity.addResultSet((List<Comic>) msg.getData());
                isLoading = false;
                break;
            case EventMessage.SEARCH_FAIL:
                mResultActivity.addResultSet(new LinkedList<Comic>());
                break;
            case EventMessage.NETWORK_ERROR:
                mResultActivity.addResultSet(null);
                isLoading = false;
                break;
        }
    }

}
