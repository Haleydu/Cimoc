package com.hiroshi.cimoc.manager;

import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.ImageUrlDao;

import java.util.List;

import rx.Observable;

/**
 * Created by HaleyDu on 2020/8/27.
 */
public class ImageUrlManager {

    private static ImageUrlManager mInstance;

    private ImageUrlDao mImageUrlDao;

    private ImageUrlManager(AppGetter getter) {
        mImageUrlDao = getter.getAppInstance().getDaoSession().getImageUrlDao();
    }

    public static ImageUrlManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (ImageUrlManager.class) {
                if (mInstance == null) {
                    mInstance = new ImageUrlManager(getter);
                }
            }
        }
        return mInstance;
    }

    public void runInTx(Runnable runnable) {
        mImageUrlDao.getSession().runInTx(runnable);
    }

    public Observable<List<ImageUrl>> getListImageUrlRX(Long comicChapter) {
        return mImageUrlDao.queryBuilder()
                .where(ImageUrlDao.Properties.ComicChapter.eq(comicChapter))
                .rx()
                .list();
    }

    public List<ImageUrl> getListImageUrl(Long comicChapter) {
        return mImageUrlDao.queryBuilder()
                .where(ImageUrlDao.Properties.ComicChapter.eq(comicChapter))
                .list();
    }

    public ImageUrl load(long id) {
        return mImageUrlDao.load(id);
    }

    public void updateOrInsert(List<ImageUrl> imageUrlList) {
        for (ImageUrl imageurl : imageUrlList) {
            if (imageurl.getId() == null) {
                insert(imageurl);
            } else {
                update(imageurl);
            }
        }
    }

    public void insertOrReplace(List<ImageUrl> imageUrlList) {
        for (ImageUrl imageurl:imageUrlList) {
            if (imageurl.getId()!=null) {
                mImageUrlDao.insertOrReplace(imageurl);
            }
        }
    }

    public void update(ImageUrl imageurl) {
        mImageUrlDao.update(imageurl);
    }

    public void deleteByKey(long key) {
        mImageUrlDao.deleteByKey(key);
    }

    public void insert(ImageUrl imageurl) {
        long id = mImageUrlDao.insert(imageurl);
        imageurl.setId(id);
    }

}
