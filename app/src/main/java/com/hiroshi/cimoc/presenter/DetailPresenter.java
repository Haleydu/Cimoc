package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter<DetailView> {

    private ComicManager mComicManager;
    private Comic mComic;
    private int source;

    public DetailPresenter(Long id, int source, String cid) {
        this.source = source;
        this.mComicManager = ComicManager.getInstance();
        this.mComic = mComicManager.getComic(id, source, cid);
    }

    @Override
    public void attachView(DetailView mBaseView) {
        super.attachView(mBaseView);
        initSubscription();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                String last = (String) rxEvent.getData();
                int page = (int) rxEvent.getData(1);
                mComic.setHistory(System.currentTimeMillis());
                mComic.setLast(last);
                mComic.setPage(page);
                if (mComic.getId() == null) {
                    long id = mComicManager.insertComic(mComic);
                    mComic.setId(id);
                } else {
                    mComicManager.updateComic(mComic);
                }
                RxBus.getInstance().post(new RxEvent(RxEvent.HISTORY_COMIC, new MiniComic(mComic)));
                mBaseView.onChapterChange(last);
            }
        });
        addSubscription(RxEvent.COMIC_PAGE_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComic.setPage((Integer) rxEvent.getData());
                if (mComic.getId() != null) {
                    mComicManager.updateComic(mComic);
                }
            }
        });
    }

    public void load() {
        Manga.info(SourceManager.getParser(source), mComic)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Chapter>>() {
                    @Override
                    public void onCompleted() {
                        mBaseView.showLayout();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBaseView.showLayout();
                        if (e instanceof Manga.NetworkErrorException) {
                            mBaseView.onNetworkError();
                        } else {
                            mBaseView.onParseError();
                        }
                    }

                    @Override
                    public void onNext(List<Chapter> list) {
                        mBaseView.onLoadSuccess(mComic, list);
                    }
                });
    }

    public void updateComic() {
        if (mComic.getId() != null) {
            mComicManager.updateComic(mComic);
        }
    }

    public Comic getComic() {
        return mComic;
    }

    public boolean isComicFavorite() {
        return mComic.getFavorite() != null;
    }

    public void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        if (mComic.getId() == null) {
            long id = mComicManager.insertComic(mComic);
            mComic.setId(id);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.FAVORITE_COMIC, new MiniComic(mComic)));
    }

    public void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        if (mComic.getHistory() == null) {
            mComicManager.deleteComic(id);
            mComic.setId(null);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.UN_FAVORITE_COMIC, id));
    }

}
