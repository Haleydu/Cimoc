package com.hiroshi.cimoc.presenter;

import android.content.Intent;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
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

    public List<Comic> getComic() {
        return mComicManager.listFavorite();
    }

    public void onItemClick(int position) {
        Comic comic = mFavoriteFragment.getItem(position);
        Intent intent = DetailActivity.createIntent(mFavoriteFragment.getActivity(), comic, false);
        mFavoriteFragment.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.FAVORITE_COMIC:
                Comic comic = (Comic) msg.getData();
                mFavoriteFragment.addItem(comic);
                break;
            case EventMessage.UN_FAVORITE_COMIC:
                long id = (Long) msg.getData();
                mFavoriteFragment.removeItem(id);
                break;
            case EventMessage.RESTORE_FAVORITE:
                List<Comic> list = (List<Comic>) msg.getData();
                mFavoriteFragment.addItems(list);
                break;
        }
    }

}
