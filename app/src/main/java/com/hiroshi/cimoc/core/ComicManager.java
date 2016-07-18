package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.db.dao.ComicRecordDao;
import com.hiroshi.db.dao.ComicRecordDao.Properties;
import com.hiroshi.db.entity.ComicRecord;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private ComicRecordDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getComicRecordDao();
    }

    public List<ComicRecord> listFavorite() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
                .orderDesc(Properties.Favorite)
                .list();
    }

    public List<ComicRecord> listHistory() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .list();
    }

    private ComicRecord mComicRecord;

    public void initComic(int source, String path) {
        List<ComicRecord> list = mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Path.eq(path))
                .limit(1)
                .list();
        if (!list.isEmpty()) {
            mComicRecord = list.get(0);
        } else {
            mComicRecord = new ComicRecord(null);
            mComicRecord.setSource(source);
            mComicRecord.setPath(path);
        }
    }

    public void setBasicInfo(String title, String image, String update) {
        mComicRecord.setTitle(title);
        mComicRecord.setImage(image);
        mComicRecord.setUpdate(update);
    }

    public void setLastPath(String path) {
        mComicRecord.setLast_path(path);
        if (!isComicExist()) {
            long id = mComicDao.insert(mComicRecord);
            mComicRecord.setId(id);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.CHANGE_LAST_PATH, path));
    }

    public void setLastPage(int page) {
        mComicRecord.setHistory(System.currentTimeMillis());
        mComicRecord.setLast_page(page);
        EventBus.getDefault().post(new EventMessage(EventMessage.HISTORY_COMIC, mComicRecord));
    }

    public void favoriteComic() {
        mComicRecord.setFavorite(System.currentTimeMillis());
        if (!isComicHistory()) {
            long id = mComicDao.insert(mComicRecord);
            mComicRecord.setId(id);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, mComicRecord));
    }

    public void unfavoriteComic() {
        long id = mComicRecord.getId();
        mComicRecord.setFavorite(null);
        if (!isComicHistory()) {
            mComicDao.delete(mComicRecord);
        }
        EventBus.getDefault().post(new EventMessage(EventMessage.UN_FAVORITE_COMIC, id));
    }

    public boolean isComicStar() {
        return mComicRecord.getFavorite() != null;
    }

    public boolean isComicHistory() {
        return mComicRecord.getHistory() != null;
    }

    public boolean isComicExist() {
        return isComicStar() || isComicHistory();
    }

    public int getSource() {
        return mComicRecord.getSource();
    }

    public String getPath() {
        return mComicRecord.getPath();
    }

    public String getLastPath() {
        return mComicRecord.getLast_path();
    }

    public void saveAndClearComic() {
        if (isComicExist()) {
            mComicDao.update(mComicRecord);
        }
        mComicRecord = null;
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
