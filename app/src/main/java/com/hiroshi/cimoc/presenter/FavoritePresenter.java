package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.fragment.FavoriteFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/6.
 */
public class FavoritePresenter extends BasePresenter {

    private FavoriteFragment mFavoriteFragment;
    private ComicManager mComicManager;

    public FavoritePresenter(FavoriteFragment fragment) {
        mFavoriteFragment = fragment;
        mComicManager = ComicManager.getInstance();
    }

    public List<MiniComic> getComicList() {
        return mComicManager.listFavorite();
    }

    public MiniComic[] getComicArray() {
        return mComicManager.arrayFavorite();
    }

    public void updateComic(List<MiniComic> list) {
        mComicManager.updateFavorite(list);
    }

    public void deleteComic(MiniComic comic) {
        mComicManager.deleteFavorite(comic.getId());
    }

    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.FAVORITE_COMIC:
                mFavoriteFragment.addItem((MiniComic) msg.getData());
                break;
            case EventMessage.UN_FAVORITE_COMIC:
                mFavoriteFragment.removeItem((long) msg.getData());
                break;
            case EventMessage.RESTORE_FAVORITE:
                mFavoriteFragment.addItems((List<MiniComic>) msg.getData());
                break;
            case EventMessage.COMIC_DELETE:
                mFavoriteFragment.removeItems((int) msg.getData());
                break;
        }
    }

}
