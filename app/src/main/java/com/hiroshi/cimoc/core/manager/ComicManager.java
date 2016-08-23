package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private ComicDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getComicDao();
    }

    public void restoreFavorite(final List<Comic> list) {
        mComicDao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                long favorite = System.currentTimeMillis();
                List<MiniComic> result = new LinkedList<>();
                for (Comic comic : list) {
                    Comic temp = mComicDao.queryBuilder()
                            .where(Properties.Source.eq(comic.getSource()), Properties.Cid.eq(comic.getCid()))
                            .unique();
                    if (temp == null) {
                        comic.setFavorite(favorite);
                        long id = mComicDao.insert(comic);
                        comic.setId(id);
                        result.add(0, new MiniComic(comic));
                    } else if (temp.getFavorite() == null) {
                        temp.setFavorite(System.currentTimeMillis());
                        mComicDao.update(temp);
                        result.add(0, new MiniComic(temp));
                    }
                    favorite += 10;
                }
                RxBus.getInstance().post(new RxEvent(RxEvent.RESTORE_FAVORITE, result));
            }
        });
    }

    public void deleteHistory(long id) {
        Comic comic = mComicDao.load(id);
        if (comic.getFavorite() == null) {
            mComicDao.delete(comic);
        } else {
            comic.setHistory(null);
            mComicDao.update(comic);
        }
    }

    public Observable<List<Comic>> listSource(int source) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source))
                .rx()
                .list();
    }

    public Observable<List<Comic>> listFavorite() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listHistory() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .rx()
                .list();
    }

    public Observable<Comic> load(int source, String cid) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                .rx()
                .unique();
    }

    public Observable<Comic> load(long id) {
        return mComicDao.rx().load(id);
    }

    public void update(Comic comic) {
        mComicDao.update(comic);
    }

    public void deleteByKey(long id) {
        mComicDao.deleteByKey(id);
    }

    public void deleteInTx(List<Comic> list) {
        mComicDao.deleteInTx(list);
    }

    public void updateInTx(List<Comic> list) {
        mComicDao.updateInTx(list);
    }

    public long insert(Comic comic) {
        return mComicDao.insert(comic);
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
