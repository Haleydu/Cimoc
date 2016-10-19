package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagDao;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.TagRefDao;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagManager {

    public static final int TAG_ALL = 100;
    public static final int TAG_CONTINUE = 101;
    public static final int TAG_END = 102;
    public static final int TAG_NORMAL = 103;

    private static TagManager mTagManager;

    private TagDao mTagDao;
    private TagRefDao mRefDao;

    private TagManager() {
        mTagDao = CimocApplication.getDaoSession().getTagDao();
        mRefDao = CimocApplication.getDaoSession().getTagRefDao();
    }

    public Observable<Void> runInRx(Runnable runnable) {
        return mRefDao.getSession().rxTx().run(runnable);
    }

    public void runInTx(Runnable runnable) {
        mRefDao.getSession().runInTx(runnable);
    }

    public Observable<List<Tag>> list() {
        return mTagDao.queryBuilder()
                .rx()
                .list();
    }

    public Observable<List<TagRef>> listByTag(long id) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(id))
                .rx()
                .list();
    }

    public Observable<List<TagRef>> listByComic(long cid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Cid.eq(cid))
                .rx()
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

    public long insert(Tag tag) {
        return mTagDao.insert(tag);
    }

    public long insert(TagRef ref) {
        return mRefDao.insert(ref);
    }

    public void insert(Iterable<TagRef> entities) {
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
