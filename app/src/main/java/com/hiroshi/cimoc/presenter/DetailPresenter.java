package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter {

    private DetailActivity mDetailActivity;
    private ComicManager mComicManager;
    private Manga mManga;
    private Comic mComic;


    public DetailPresenter(DetailActivity activity, Long id, int source, String cid) {
        mDetailActivity = activity;
        mComicManager = ComicManager.getInstance();
        mManga = SourceManager.getManga(source);
        mComic = mComicManager.getComic(id, source, cid);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManga.into(mComic);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManga.cancel();
        if (mComic.getId() != null) {
            mComicManager.updateComic(mComic);
        }
    }

    public Comic getComic() {
        return mComic;
    }

    public boolean isComicFavorite() {
        return mComic.getFavorite() != null;
    }

    public void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        if (mComic.getId() == null) {
            long id = mComicManager.insertComic(mComic);
            mComic.setId(id);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, new MiniComic(mComic)));
    }

    public void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        if (mComic.getHistory() == null) {
            mComicManager.deleteComic(id);
            mComic.setId(null);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.UN_FAVORITE_COMIC, id));
    }

    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.LOAD_COMIC_SUCCESS:
                mDetailActivity.setView(mComic, (List<Chapter>) msg.getData());
                break;
            case EventMessage.LOAD_COMIC_FAIL:
                mDetailActivity.setView(mComic, new LinkedList<Chapter>());
                break;
            case EventMessage.NETWORK_ERROR:
                mDetailActivity.setView(mComic, null);
                break;
            case EventMessage.COMIC_LAST_CHANGE:
                String last = (String) msg.getData();
                int page = (int) msg.getSecond();
                mComic.setHistory(System.currentTimeMillis());
                mComic.setLast(last);
                mComic.setPage(page);
                if (mComic.getId() == null) {
                    long id = mComicManager.insertComic(mComic);
                    mComic.setId(id);
                } else {
                    mComicManager.updateComic(mComic);
                }
                EventBus.getDefault().post(new EventMessage(EventMessage.HISTORY_COMIC, new MiniComic(mComic)));
                mDetailActivity.setLastChapter(last);
                break;
            case EventMessage.COMIC_PAGE_CHANGE:
                mComic.setPage((Integer) msg.getData());
                if (mComic.getId() != null) {
                    mComicManager.updateComic(mComic);
                }
                break;
        }
    }

}
