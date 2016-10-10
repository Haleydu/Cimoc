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

    public static final int TAG_TYPE_CONTINUE = 250;
    public static final int TAG_TYPE_END = 251;
    public static final int TAG_NORMAL = 300;

    private static TagManager mTagManager;

    private TagDao mTagDao;
    private TagRefDao mRefDao;

    private TagManager() {
        mTagDao = CimocApplication.getDaoSession().getTagDao();
        mRefDao = CimocApplication.getDaoSession().getTagRefDao();
    }

    public Observable<List<Tag>> list() {
        return mTagDao.queryBuilder()
                .rx()
                .list();
    }

    public Observable<List<TagRef>> listByTag(List<Integer> list) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.in(list))
                .rx()
                .list();
    }

    public Observable<List<TagRef>> listByComic(int cid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Cid.eq(cid))
                .rx()
                .list();
    }

    public long insert(Tag tag) {
        return mTagDao.insert(tag);
    }

    public long insert(TagRef ref) {
        return mRefDao.insert(ref);
    }

    public void update(Tag tag) {
        mTagDao.update(tag);
    }

    public static TagManager getInstance() {
        if (mTagManager == null) {
            mTagManager = new TagManager();
        }
        return mTagManager;
    }

}
