package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.DownloadTask;
import com.hiroshi.cimoc.model.DownloadTaskDao;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/4.
 */
public class DownloadManager {

    private static DownloadManager mDownloadManager;

    private DownloadTaskDao mDownloadDao;

    private DownloadManager() {
        mDownloadDao = CimocApplication.getDaoSession().getDownloadTaskDao();
    }

    public Observable<List<DownloadTask>> listSelectable(long id) {
        return mDownloadDao.queryBuilder()
                .where(DownloadTaskDao.Properties.Key.eq(id))
                .rx()
                .list();
    }

    public static DownloadManager getInstance() {
        if (mDownloadManager == null) {
            mDownloadManager = new DownloadManager();
        }
        return mDownloadManager;
    }

}
