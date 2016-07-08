package com.hiroshi.cimoc.presenter;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.fragment.FavoriteFragment;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.db.dao.FavoriteComicDao;
import com.hiroshi.db.entity.FavoriteComic;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/6.
 */
public class FavoritePresenter extends BasePresenter {

    private FavoriteFragment mFavoriteFragment;
    private FavoriteComicDao mComicDao;


    public FavoritePresenter(FavoriteFragment fragment) {
        mFavoriteFragment = fragment;
        mComicDao = CimocApplication.getDaoSession().getFavoriteComicDao();
    }

    public List<FavoriteComic> getFavoriteComic() {
        List<FavoriteComic> list = mComicDao.queryBuilder().list();
        return list;
    }

    public BaseAdapter.OnItemClickListener getItemClickListener() {
        return new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = DetailActivity.createIntent(mFavoriteFragment.getActivity(), mFavoriteFragment.getItem(position));
                mFavoriteFragment.startActivity(intent);
            }
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        FavoriteComic comic;
        switch (msg.getType()) {
            case EventMessage.FAVORITE_COMIC:
                comic = (FavoriteComic) msg.getData();
                mFavoriteFragment.addItem(comic);
                break;
            case EventMessage.UN_FAVORITE_COMIC:
                comic = (FavoriteComic) msg.getData();
                mFavoriteFragment.removeItem(comic.getId());
                break;
        }
    }

}
