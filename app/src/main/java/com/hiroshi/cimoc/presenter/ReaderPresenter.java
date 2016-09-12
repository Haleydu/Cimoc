package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.Picture;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;
import com.hiroshi.cimoc.ui.view.ReaderView;

import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter<ReaderView> {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_INIT = 1;
    private final static int LOAD_PREV = 2;
    private final static int LOAD_NEXT = 3;

    private PreloadAdapter mPreloadAdapter;

    private boolean isShowNext;
    private boolean isShowPrev;
    private int count;

    private int source;
    private String cid;
    private String comic;
    private int status;

    public ReaderPresenter(int source, String cid, String comic, Chapter[] chapter, int position) {
        this.mPreloadAdapter = new PreloadAdapter(chapter, position);
        this.source = source;
        this.cid = cid;
        this.comic = comic;
        this.isShowNext = true;
        this.isShowPrev = true;
        this.count = 0;
    }

    public void lazyLoad(final ImageUrl imageUrl) {
        Manga.load(source, imageUrl.getUrl())
                .subscribeOn(Schedulers.io())
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
                });
    }

    public void setPage(int progress) {
        if (status != LOAD_INIT) {
            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_PAGE_CHANGE, progress));
        }
    }

    public void loadInit(String last, int page) {
        status = LOAD_INIT;
        Chapter chapter = mPreloadAdapter.getNextChapter();
        images(chapter.isDownload() ?
                Download.images(source, comic, chapter.getTitle()) : Manga.images(source, cid, chapter.getPath()),
                last, page);
    }

    public void loadNext() {
        if (status == LOAD_NULL && isShowNext) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                images(chapter.isDownload() ?
                                Download.images(source, comic, chapter.getTitle()) : Manga.images(source, cid, chapter.getPath()));
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
                images(chapter.isDownload() ?
                        Download.images(source, comic, chapter.getTitle()) : Manga.images(source, cid, chapter.getPath()));
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
            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), 1));
        }
    }

    public void toPrevChapter() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter != null) {
            mBaseView.onChapterChange(chapter);
            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), chapter.getCount()));
        }
    }

    public void savePicture(InputStream inputStream, String suffix) {
        Picture.save(inputStream, suffix)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onPictureSaveSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onPictureSaveFail();
                    }
                });
    }

    private void images(Observable<List<ImageUrl>> observable) {
        images(observable, null, -1);
    }

    private void images(Observable<List<ImageUrl>> observable, final String last, final int page) {
        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ImageUrl>>() {
                    @Override
                    public void onCompleted() {
                        status = LOAD_NULL;
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof Manga.NetworkErrorException) {
                            mBaseView.onNetworkError();
                        } else {
                            mBaseView.onParseError();
                        }
                        if (status != LOAD_INIT && ++count < 2) {
                            status = LOAD_NULL;
                        }
                    }

                    @Override
                    public void onNext(List<ImageUrl> list) {
                        Chapter chapter;
                        switch (status) {
                            case LOAD_INIT:
                                chapter = mPreloadAdapter.moveNext();
                                chapter.setCount(list.size());
                                mBaseView.onChapterChange(chapter);
                                if (!chapter.getPath().equals(last) || page == -1) {
                                    mBaseView.onInitLoadSuccess(list, 1);
                                    RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), 1));
                                } else {
                                    mBaseView.onInitLoadSuccess(list, page);
                                    RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), page));
                                }
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
                    }
                });
    }

}
