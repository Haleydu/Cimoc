package com.hiroshi.cimoc.core;

import android.util.Log;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.db.dao.FavoriteComicDao;
import com.hiroshi.db.dao.FavoriteComicDao.Properties;
import com.hiroshi.db.entity.FavoriteComic;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private FavoriteComicDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getFavoriteComicDao();
    }

    public List<FavoriteComic> list() {
        return mComicDao.loadAll();
    }

    private FavoriteComic mCurrentComic;

    public void initCurrentComic(int source, String path) {
        List<FavoriteComic> list = mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Path.eq(path))
                .limit(1)
                .list();
        if (!list.isEmpty()) {
            mCurrentComic = list.get(0);
        } else {
            mCurrentComic = new FavoriteComic(null);
            mCurrentComic.setSource(source);
            mCurrentComic.setPath(path);
        }
    }

    public void setBasicInfo(String title, String update, String image) {
        mCurrentComic.setTitle(title);
        mCurrentComic.setUpdate(update);
        mCurrentComic.setImage(image);
    }

    public void setLastRead(String path, int page) {
        mCurrentComic.setLast_path(path);
        mCurrentComic.setLast_page(page);
        EventBus.getDefault().post(new EventMessage(EventMessage.LAST_READ, path));
    }

    public void starCurrentComic() {
        mCurrentComic.setCreate(System.currentTimeMillis());
        long id = mComicDao.insert(mCurrentComic);
        mCurrentComic.setId(id);
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, mCurrentComic));
    }

    public void unstarCurrentComic() {
        mComicDao.delete(mCurrentComic);
        EventBus.getDefault().post(new EventMessage(EventMessage.UN_FAVORITE_COMIC, mCurrentComic));
        mCurrentComic.setId(null);
    }

    public boolean isComicStar() {
        return mCurrentComic.getId() != null;
    }

    public int getSource() {
        return mCurrentComic.getSource();
    }

    public String getPath() {
        return mCurrentComic.getPath();
    }

    public String getLastPath() {
        return mCurrentComic.getLast_path();
    }

    public void saveAndClearComic() {
        if (isComicStar()) {
            mComicDao.update(mCurrentComic);
        }
        mCurrentComic = null;
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }


}
