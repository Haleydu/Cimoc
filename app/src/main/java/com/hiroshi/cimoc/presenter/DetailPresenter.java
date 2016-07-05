package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.BaseManga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.db.dao.StarComicDao;
import com.hiroshi.db.dao.StarComicDao.Properties;
import com.hiroshi.db.entity.StarComic;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter {

    private DetailActivity mDetailActivity;
    private StarComicDao mComicDao;

    private StarComic mStarComic;
    private String last_path;
    private String last_page;
    private String title;
    private String update;

    public DetailPresenter(DetailActivity activity) {
        mDetailActivity = activity;
        mComicDao = CimocApplication.getDaoSession().getStarComicDao();
    }

    public void loadComic(String path, int source) {
        BaseManga manga = Kami.getMangaById(source);
        String url = Kami.getHostById(source) + path;
        manga.get(url);
        loopDatabase(source, path);
    }

    private void loopDatabase(int source, String path) {
        List<StarComic> list = mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Path.eq(path))
                .limit(1)
                .list();
        if (!list.isEmpty()) {
            mStarComic = list.get(0);
            last_page = mStarComic.getLast_page();
            last_path = mStarComic.getLast_path();
        }
    }

    public void onStarClick(int source, String path) {
        if (mStarComic == null) {
            long create = System.currentTimeMillis();
            mStarComic = new StarComic(null, title, "", source, update, path, create, last_path, last_page);
            long id = mComicDao.insert(mStarComic);
            mStarComic.setId(id);
            mDetailActivity.setStarButtonRes(R.drawable.ic_favorite_white_24dp);
            mDetailActivity.showSnackbar("收藏成功 :)");
        } else {
            mComicDao.delete(mStarComic);
            mStarComic = null;
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
                initView(comic, list);
                break;
        }
    }

    private void initView(Comic comic, List<Chapter> list) {
        mDetailActivity.setChapterList(comic, list);
        int resId = mStarComic != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
        mDetailActivity.setStarButtonRes(resId);
        mDetailActivity.setStarButtonVisible();
        if (list.isEmpty()) {
            mDetailActivity.showSnackbar("此漫画已被屏蔽 :(");
        }
    }

}
