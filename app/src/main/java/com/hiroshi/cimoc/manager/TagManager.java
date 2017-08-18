package com.hiroshi.cimoc.manager;

import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagDao;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagManager {

    public static final long TAG_CONTINUE = -101;
    public static final long TAG_FINISH = -100;

    private static TagManager mInstance;

    private TagDao mTagDao;

    private TagManager(AppGetter getter) {
        mTagDao = getter.getAppInstance().getDaoSession().getTagDao();
    }

    public List<Tag> list() {
        return mTagDao.queryBuilder().list();
    }

    public Observable<List<Tag>> listInRx() {
        return mTagDao.queryBuilder()
                .rx()
                .list();
    }

    public Tag load(String title) {
        return mTagDao.queryBuilder()
                .where(TagDao.Properties.Title.eq(title))
                .limit(1)
                .unique();
    }

    public void insert(Tag tag) {
        long id = mTagDao.insert(tag);
        tag.setId(id);
    }

    public void update(Tag tag) {
        mTagDao.update(tag);
    }

    public void delete(Tag entity) {
        mTagDao.delete(entity);
    }

    public static TagManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (TagManager.class) {
                if (mInstance == null) {
                    mInstance = new TagManager(getter);
                }
            }
        }
        return mInstance;
    }

}
