package com.hiroshi.cimoc.presenter;

import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter.OnItemClickListener;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter {

    private DetailActivity mDetailActivity;
    private ComicManager mComicManager;

    public DetailPresenter(DetailActivity activity) {
        mDetailActivity = activity;
        mComicManager = ComicManager.getInstance();
    }

    public void loadComic() {
        int source = mComicManager.getSource();
        String path = mComicManager.getPath();
        Kami.getMangaById(source).into(path);
    }

    public void saveComic() {
        mComicManager.saveAndClearComic();
    }

    public int getSource() {
        return mComicManager.getSource();
    }

    public OnItemClickListener getOnClickListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != 0) {
                    ArrayList<Chapter> chapters = new ArrayList<>(mDetailActivity.getChapter());
                    Intent intent = ReaderActivity.createIntent(mDetailActivity, chapters, position - 1);
                    mDetailActivity.startActivity(intent);
                }
            }
        };
    }

    public void onStarClick() {
        if (mComicManager.isComicStar()) {
            mComicManager.unfavoriteComic();
            mDetailActivity.setStarButtonRes(R.drawable.ic_favorite_border_white_24dp);
            mDetailActivity.showSnackbar("取消收藏成功 :)");
        } else {
            mComicManager.favoriteComic();
            mDetailActivity.setStarButtonRes(R.drawable.ic_favorite_white_24dp);
            mDetailActivity.showSnackbar("收藏成功 :)");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.LOAD_COMIC_SUCCESS:
                Comic comic = (Comic) msg.getData();
                List<Chapter> list = (List<Chapter>) msg.getSecond();
                mComicManager.setBasicInfo(comic.getTitle(), comic.getImage(), comic.getUpdate());
                initView(comic, list);
                break;
            case EventMessage.CHANGE_LAST_PATH:
                String path = (String) msg.getData();
                mDetailActivity.setLastChapter(path);
                break;
            case EventMessage.NETWORK_ERROR:
                mDetailActivity.hideProgressBar();
                mDetailActivity.showSnackbar("网络错误 :(");
                break;
        }
    }

    private void initView(Comic comic, List<Chapter> list) {
        String last = mComicManager.getLastPath();
        mDetailActivity.setChapterList(comic, list, last);
        int resId = mComicManager.isComicStar() ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
        mDetailActivity.setStarButtonRes(resId);
        mDetailActivity.setStarButtonVisible();
        mDetailActivity.hideProgressBar();
        if (list.isEmpty()) {
            mDetailActivity.showSnackbar("此漫画已被屏蔽 :(");
        }
    }

}
