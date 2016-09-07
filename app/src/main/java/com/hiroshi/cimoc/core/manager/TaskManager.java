package com.hiroshi.cimoc.core.manager;

import android.database.Cursor;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.model.TaskDao;
import com.hiroshi.cimoc.model.TaskDao.Properties;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Hiroshi on 2016/9/4.
 */
public class TaskManager {

    private static TaskManager mTaskManager;

    private TaskDao mTaskDao;

    private TaskManager() {
        mTaskDao = CimocApplication.getDaoSession().getTaskDao();
    }

    public Observable<List<Task>> listSelectable(long id) {
        return mTaskDao.queryBuilder()
                .where(Properties.Key.eq(id))
                .rx()
                .list();
    }

    public long insert(Task task) {
        return mTaskDao.insert(task);
    }

    public Observable<List<Long>> listKey() {
        return Observable.create(new Observable.OnSubscribe<List<Long>>() {
            @Override
            public void call(Subscriber<? super List<Long>> subscriber) {
                String sql = "SELECT DISTINCT ".concat(Properties.Key.columnName).concat(" FROM ").concat(TaskDao.TABLENAME);
                Cursor cursor = mTaskDao.getDatabase().rawQuery(sql, null);
                List<Long> list = new LinkedList<>();
                while (cursor.moveToNext()) {
                    list.add(cursor.getLong(0));
                }
                subscriber.onNext(list);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<Task>> list(long key) {
        return mTaskDao.queryBuilder()
                .where(Properties.Key.eq(key))
                .rx()
                .list();
    }

    public static TaskManager getInstance() {
        if (mTaskManager == null) {
            mTaskManager = new TaskManager();
        }
        return mTaskManager;
    }

}
