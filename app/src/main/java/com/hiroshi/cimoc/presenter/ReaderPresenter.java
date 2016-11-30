package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.global.SharedComic;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;
import com.hiroshi.cimoc.ui.view.ReaderView;

import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter<ReaderView> {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_INIT = 1;
    private final static int LOAD_PREV = 2;
    private final static int LOAD_NEXT = 3;

    private PreloadAdapter mPreloadAdapter;
    private SharedComic mSharedComic;

    private boolean isShowNext = true;
    private boolean isShowPrev = true;
    private int count = 0;
    private int status = LOAD_INIT;

    public ReaderPresenter() {
        mSharedComic = SharedComic.getInstance();
    }

    public void lazyLoad(final ImageUrl imageUrl) {
        mCompositeSubscription.add(Manga.load(mSharedComic.source(), imageUrl.getFirstUrl())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String url) {
                        if (url == null) {
                            mBaseView.onImageLoadFail(imageUrl.getId());
                        } else {
                            mBaseView.onImageLoadSuccess(imageUrl.getId(), url);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onImageLoadFail(imageUrl.getId());
                    }
                }));
    }

    public void loadInit(Chapter[] array) {
        for (int i = 0; i != array.length; ++i) {
            if (array[i].getPath().equals(mSharedComic.last())) {
                this.mPreloadAdapter = new PreloadAdapter(array, i);
                Chapter chapter = array[i];
                images(chapter.isComplete() ?
                        Download.images(mSharedComic.source(), mSharedComic.title(), chapter.getTitle()) :
                        Manga.images(mSharedComic.source(), mSharedComic.cid(), chapter.getPath()));
            }
        }
    }

    public void loadNext() {
        if (status == LOAD_NULL && isShowNext) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                images(chapter.isComplete() ?
                        Download.images(mSharedComic.source(), mSharedComic.title(), chapter.getTitle()) :
                        Manga.images(mSharedComic.source(), mSharedComic.cid(), chapter.getPath()));
                mBaseView.onNextLoading();
            } else {
                isShowNext = false;
                mBaseView.onNextLoadNone();
            }
        }
    }

    public void loadPrev() {
        if (status == LOAD_NULL && isShowPrev) {
            Chapter chapter = mPreloadAdapter.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                images(chapter.isComplete() ?
                        Download.images(mSharedComic.source(), mSharedComic.title(), chapter.getTitle()) :
                        Manga.images(mSharedComic.source(), mSharedComic.cid(), chapter.getPath()));
                mBaseView.onPrevLoading();
            } else {
                isShowPrev = false;
                mBaseView.onPrevLoadNone();
            }
        }
    }

    public void toNextChapter() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        if (chapter != null) {
            mBaseView.onChapterChange(chapter);
            mSharedComic.get().setLast(chapter.getPath());
            mSharedComic.get().setPage(1);
            mSharedComic.update();
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_CHAPTER_CHANGE));
        }
    }

    public void toPrevChapter() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter != null) {
            mBaseView.onChapterChange(chapter);
            mSharedComic.get().setLast(chapter.getPath());
            mSharedComic.get().setPage(chapter.getCount());
            mSharedComic.update();
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_CHAPTER_CHANGE));
        }
    }

    public void savePicture(InputStream inputStream, String suffix) {
        mCompositeSubscription.add(Storage.savePicture(inputStream, suffix)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String path) {
                        mBaseView.onPictureSaveSuccess(path);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onPictureSaveFail();
                    }
                }));
    }

    public void updateComic(int page) {
        if (status != LOAD_INIT) {
            mSharedComic.get().setPage(page);
            mSharedComic.update();
        }
    }

    private void images(Observable<List<ImageUrl>> observable) {
        mCompositeSubscription.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ImageUrl>>() {
                    @Override
                    public void call(List<ImageUrl> list) {
                        Chapter chapter;
                        switch (status) {
                            case LOAD_INIT:
                                chapter = mPreloadAdapter.moveNext();
                                chapter.setCount(list.size());
                                mBaseView.onChapterChange(chapter);
                                mBaseView.onInitLoadSuccess(list, mSharedComic.page(), mSharedComic.source());
                                break;
                            case LOAD_PREV:
                                chapter = mPreloadAdapter.movePrev();
                                chapter.setCount(list.size());
                                mBaseView.onPrevLoadSuccess(list);
                                break;
                            case LOAD_NEXT:
                                chapter = mPreloadAdapter.moveNext();
                                chapter.setCount(list.size());
                                mBaseView.onNextLoadSuccess(list);
                                break;
                        }
                        status = LOAD_NULL;
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof Manga.NetworkErrorException) {
                            mBaseView.onNetworkError();
                        } else {
                            mBaseView.onParseError();
                        }
                        if (status != LOAD_INIT && ++count < 2) {
                            status = LOAD_NULL;
                        }
                    }
                }));
    }

}
