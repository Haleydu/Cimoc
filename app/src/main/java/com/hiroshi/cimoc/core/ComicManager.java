package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.utils.ExLog;

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

    public boolean isExist(int source, String cid) {
        List<Comic> list = mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                .limit(1)
                .list();
        return !list.isEmpty();
    }

    public void restore(int source, String cid, String title, String cover, String update) {
        Comic comic = new Comic(null, source, cid, title, cover, update, System.currentTimeMillis(), null, null, null);
        long id = mComicDao.insert(comic);
        comic.setId(id);
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, comic));
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

    public void setComic(Comic comic, boolean isResult) {
        this.comic = comic;
        ExLog.d("ComicManager", "init" + " [" + this.comic.getId() + "] " + this.comic.getTitle());
        if (isResult) {
            ExLog.d("ComicManager", "comic from result");
            List<Comic> list = mComicDao.queryBuilder()
                    .where(Properties.Source.eq(comic.getSource()), Properties.Cid.eq(comic.getCid()))
                    .limit(1)
                    .list();
            if (!list.isEmpty()) {
                this.comic = list.get(0);
                ExLog.d("ComicManager", "find" + " [" + this.comic.getId() + "] " + this.comic.getTitle());
            }
        }
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public void setLast(String path) {
        comic.setLast(path);
        comic.setHistory(System.currentTimeMillis());
        if (!isComicExist()) {
            long id = mComicDao.insert(comic);
            comic.setId(id);
            ExLog.d("ComicManager", "insert " + comic.getTitle() + " to " + comic.getId());
        }
        ExLog.d("ComicManager", "set the last path of" + " [" + comic.getId() + "] " + comic.getTitle() + " to " + path);
        EventBus.getDefault().post(new EventMessage(EventMessage.CHANGE_LAST_PATH, path));
    }

    public void setPage(int page) {
        comic.setPage(page);
        ExLog.d("ComicManager", "set the last page of" + " [" + comic.getId() + "] " + comic.getTitle() + " to " + page);
        EventBus.getDefault().post(new EventMessage(EventMessage.HISTORY_COMIC, comic));
    }

    public void favoriteComic() {
        comic.setFavorite(System.currentTimeMillis());
        if (!isComicExist()) {
            long id = mComicDao.insert(comic);
            comic.setId(id);
            ExLog.d("ComicManager", "insert " + comic.getTitle() + " to " + comic.getId());
        }
        ExLog.d("ComicManager", "favorite" + " [" + comic.getId() + "] " + comic.getTitle());
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, comic));
    }

    public void unfavoriteComic() {
        long id = comic.getId();
        comic.setFavorite(null);
        if (!isComicHistory()) {
            mComicDao.delete(comic);
            ExLog.d("ComicManager", "delete" + " [" + comic.getId() + "] " + comic.getTitle());
        }
        ExLog.d("ComicManager", "unfavorite" + " [" + comic.getId() + "] " + comic.getTitle());
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

    public String getCid() {
        return comic.getCid();
    }

    public String getLast() {
        return comic.getLast();
    }

    public void saveAndClearComic() {
        if (isComicExist()) {
            ExLog.d("ComicManager", "save" + " [" + comic.getId() + "] " + comic.getTitle());
            mComicDao.update(comic);
        }
        comic = null;
        ExLog.d("ComicManager", "clear comic");
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
