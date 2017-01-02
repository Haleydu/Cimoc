package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagDao;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.TagRefDao;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagManager {

    public static final long TAG_CONTINUE = -11;
    public static final long TAG_FINISH = -10;

    private static TagManager mTagManager;

    private TagDao mTagDao;
    private TagRefDao mRefDao;

    private TagManager() {
        mTagDao = CimocApplication.getDaoSession().getTagDao();
        mRefDao = CimocApplication.getDaoSession().getTagRefDao();
    }

    public <T> T callInTx(Callable<T> callable) {
        return mTagDao.getSession().callInTxNoException(callable);
    }

    public Observable<Void> runInRx(Runnable runnable) {
        return mRefDao.getSession().rxTx().run(runnable);
    }

    public void runInTx(Runnable runnable) {
        mRefDao.getSession().runInTx(runnable);
    }

    public Observable<List<Tag>> listInRx() {
        return mTagDao.queryBuilder()
                .rx()
                .list();
    }

    public Observable<List<TagRef>> listByTagInRx(long id) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(id))
                .rx()
                .list();
    }

    public List<TagRef> listByTag(long id) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(id))
                .list();
    }

    public List<TagRef> listByComic(long cid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Cid.eq(cid))
                .list();
    }

    public Tag load(String title) {
        return mTagDao.queryBuilder()
                .where(TagDao.Properties.Title.eq(title))
                .limit(1)
                .unique();
    }

    public TagRef load(long tid, long cid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid), TagRefDao.Properties.Cid.eq(cid))
                .limit(1)
                .unique();
    }

    public void insert(Tag tag) {
        long id = mTagDao.insert(tag);
        tag.setId(id);
    }

    public long insert(TagRef ref) {
        return mRefDao.insert(ref);
    }

    public void insert(Iterable<TagRef> entities) {
        mRefDao.insertInTx(entities);
    }

    public void insertInTx(Iterable<TagRef> entities) {
        mRefDao.insertInTx(entities);
    }

    public void update(Tag tag) {
        mTagDao.update(tag);
    }

    public void deleteByTag(long tid) {
        mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void deleteByComic(long cid) {
        mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Cid.eq(cid))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void delete(Tag entity) {
        mTagDao.delete(entity);
    }

    public void delete(long tid, long cid) {
        mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid), TagRefDao.Properties.Cid.eq(cid))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public static TagManager getInstance() {
        if (mTagManager == null) {
            mTagManager = new TagManager();
        }
        return mTagManager;
    }

}
