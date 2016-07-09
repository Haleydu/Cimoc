package com.hiroshi.cimoc.presenter;

import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter.OnItemClickListener;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.db.dao.FavoriteComicDao;
import com.hiroshi.db.dao.FavoriteComicDao.Properties;
import com.hiroshi.db.entity.FavoriteComic;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter {

    private DetailActivity mDetailActivity;
    private FavoriteComicDao mComicDao;

    private FavoriteComic mFavoriteComic;
    private String last_path;
    private String last_page;
    private String title;
    private String update;
    private String image;

    private int source;

    public DetailPresenter(DetailActivity activity, int source) {
        mDetailActivity = activity;
        mComicDao = CimocApplication.getDaoSession().getFavoriteComicDao();
        this.source = source;
    }

    public void loadComic(String path) {
        Manga manga = Kami.getMangaById(source);
        manga.into(path);
        loopDatabase(source, path);
    }

    private void loopDatabase(int source, String path) {
        List<FavoriteComic> list = mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Path.eq(path))
                .limit(1)
                .list();
        if (!list.isEmpty()) {
            mFavoriteComic = list.get(0);
            last_page = mFavoriteComic.getLast_page();
            last_path = mFavoriteComic.getLast_path();
        }
    }

    public OnItemClickListener getOnClickListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != 0) {
                    ArrayList<Chapter> chapters = new ArrayList<>(mDetailActivity.getChapter());
                    Intent intent = ReaderActivity.createIntent(mDetailActivity, chapters, position - 1, source);
                    mDetailActivity.startActivity(intent);
                }
            }
        };
    }

    public void onStarClick(int source, String path) {
        if (mFavoriteComic == null) {
            long create = System.currentTimeMillis();
            mFavoriteComic = new FavoriteComic(null, title, image, source, update, path, create, last_path, last_page);
            long id = mComicDao.insert(mFavoriteComic);
            mFavoriteComic.setId(id);
            EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, mFavoriteComic));
            mDetailActivity.setStarButtonRes(R.drawable.ic_favorite_white_24dp);
            mDetailActivity.showSnackbar("收藏成功 :)");
        } else {
            mComicDao.delete(mFavoriteComic);
            EventBus.getDefault().post(new EventMessage(EventMessage.UN_FAVORITE_COMIC, mFavoriteComic));
            mFavoriteComic = null;
            mDetailActivity.setStarButtonRes(R.drawable.ic_favorite_border_white_24dp);
            mDetailActivity.showSnackbar("取消收藏成功 :)");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.LOAD_COMIC_SUCCESS:
                Comic comic = (Comic) msg.getData();
                List<Chapter> list = (List<Chapter>) msg.getSecond();
                title = comic.getTitle();
                update = comic.getUpdate();
                image = comic.getImage();
                initView(comic, list);
                break;
        }
    }

    private void initView(Comic comic, List<Chapter> list) {
        mDetailActivity.setChapterList(comic, list);
        int resId = mFavoriteComic != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
        mDetailActivity.setStarButtonRes(resId);
        mDetailActivity.setStarButtonVisible();
        mDetailActivity.hideProgressBar();
        if (list.isEmpty()) {
            mDetailActivity.showSnackbar("此漫画已被屏蔽 :(");
        }
    }

}
