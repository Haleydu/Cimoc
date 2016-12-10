package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
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
    private ComicManager mComicManager;
    private Comic mComic;

    private boolean isShowNext = true;
    private boolean isShowPrev = true;
    private int count = 0;
    private int status = LOAD_INIT;

    public ReaderPresenter() {
        mComicManager = ComicManager.getInstance();
    }

    public void lazyLoad(final ImageUrl imageUrl) {
        mCompositeSubscription.add(Manga.loadLazyUrl(mComic.getSource(), imageUrl.getFirstUrl())
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

    public void loadInit(long id, Chapter[] array) {
        mComic = mComicManager.load(id);
        for (int i = 0; i != array.length; ++i) {
            if (array[i].getPath().equals(mComic.getLast())) {
                this.mPreloadAdapter = new PreloadAdapter(array, i);
                images(getObservable(array[i]));
            }
        }
    }

    public void loadNext() {
        if (status == LOAD_NULL && isShowNext) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                images(getObservable(chapter));
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
                images(getObservable(chapter));
                mBaseView.onPrevLoading();
            } else {
                isShowPrev = false;
                mBaseView.onPrevLoadNone();
            }
        }
    }

    private Observable<List<ImageUrl>> getObservable(Chapter chapter) {
        return chapter.isComplete() ? Download.images(mComic, chapter) :
                Manga.getChapterImage(mComic.getSource(), mComic.getCid(), chapter.getPath());
    }

    public void toNextChapter() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        if (chapter != null) {
            updateChapter(chapter, true);
        }
    }

    public void toPrevChapter() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter != null) {
            updateChapter(chapter, false);
        }
    }

    private void updateChapter(Chapter chapter, boolean isNext) {
        mBaseView.onChapterChange(chapter);
        mComic.setLast(chapter.getPath());
        mComic.setPage(isNext ? 1 : chapter.getCount());
        mComicManager.update(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_CHAPTER_CHANGE, chapter.getPath()));
    }

    public void savePicture(InputStream inputStream, String url) {
        mCompositeSubscription.add(Storage.savePicture(inputStream, url)
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
            mComic.setPage(page);
            mComicManager.update(mComic);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_PAGE_CHANGE, page));
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
                                mBaseView.onInitLoadSuccess(list, mComic.getPage(), mComic.getSource());
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
