package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;
import com.hiroshi.cimoc.model.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private ComicDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getComicDao();
    }

    public List<Comic> listFavorite() {
        return mComicDao.queryBuilder()
                .where(ComicDao.Properties.Favorite.isNotNull())
                .orderDesc(Properties.Favorite)
                .list();
    }

    public List<Comic> listHistory() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .list();
    }

    private Comic comic;
    private List<Chapter> chapters;

    public void setComic(Comic comic) {
        this.comic = comic;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public void setLastPath(String path) {
        comic.setLast(path);
        comic.setHistory(System.currentTimeMillis());
        if (!isComicExist()) {
            long id = mComicDao.insert(comic);
            comic.setId(id);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.CHANGE_LAST_PATH, path));
    }

    public void setLast(int page) {
        comic.setPage(page);
        EventBus.getDefault().post(new EventMessage(EventMessage.HISTORY_COMIC, comic));
    }

    public void favoriteComic() {
        comic.setFavorite(System.currentTimeMillis());
        if (!isComicExist()) {
            long id = mComicDao.insert(comic);
            comic.setId(id);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, comic));
    }

    public void unfavoriteComic() {
        long id = comic.getId();
        comic.setFavorite(null);
        if (!isComicHistory()) {
            mComicDao.delete(comic);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.UN_FAVORITE_COMIC, id));
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public Comic getComic() {
        return comic;
    }

    public boolean isComicStar() {
        return comic.getFavorite() != null;
    }

    public boolean isComicHistory() {
        return comic.getHistory() != null;
    }

    public boolean isComicExist() {
        return comic.getId() != null;
    }

    public int getSource() {
        return comic.getSource();
    }

    public String getPath() {
        return comic.getPath();
    }

    public String getLast() {
        return comic.getLast();
    }

    public void saveAndClearComic() {
        if (isComicExist()) {
            mComicDao.update(comic);
        }
        comic = null;
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
