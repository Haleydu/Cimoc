package com.hiroshi.cimoc.manager;

import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.model.Local;
import com.hiroshi.cimoc.model.LocalDao;
import com.hiroshi.cimoc.model.Tag;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public class LocalManager {

    private static LocalManager mInstance;

    private LocalDao mLocalDao;

    private LocalManager(AppGetter getter) {
        mLocalDao = getter.getAppInstance().getDaoSession().getLocalDao();
    }

    public Observable<List<Local>> listInRx() {
        return mLocalDao.queryBuilder()
                .rx()
                .list();
    }

    public void insert(Local local) {
        long id = mLocalDao.insert(local);
        local.setId(id);
    }

    public void delete(Local entity) {
        mLocalDao.delete(entity);
    }

    public static LocalManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (LocalManager.class) {
                if (mInstance == null) {
                    mInstance = new LocalManager(getter);
                }
            }
        }
        return mInstance;
    }

}
