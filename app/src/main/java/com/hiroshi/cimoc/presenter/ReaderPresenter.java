package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Parser;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;
import com.hiroshi.cimoc.ui.view.ReaderView;

import java.util.List;

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

    private Parser parser;
    private String cid;
    private String last;
    private int page;
    private int status;

    public ReaderPresenter(int source, String cid, String last, int page, Chapter[] array, int position) {
        this.mPreloadAdapter = new PreloadAdapter(array, position);
        this.isShowNext = true;
        this.isShowPrev = true;
        this.count = 0;
        this.cid = cid;
        this.last = last;
        this.page = page;
        this.parser = SourceManager.getParser(source);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.IMAGE_LAZY_LOAD, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                ImageUrl imageUrl = (ImageUrl) rxEvent.getData();
                final int id = imageUrl.getId();
                Manga.load(parser, imageUrl.getUrl())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String url) {
                                mBaseView.onImageLoadSuccess(id, url);
                            }
                        });
            }
        });
    }

    public void setPage(int progress) {
        if (status != LOAD_INIT) {
            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_PAGE_CHANGE, progress));
        }
    }

    public void loadInit() {
        status = LOAD_INIT;
        images(mPreloadAdapter.getNextChapter().getPath());
    }

    public void loadNext() {
        if (status == LOAD_NULL && isShowNext) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                images(chapter.getPath());
                mBaseView.showMessage(R.string.reader_load_next);
            } else {
                isShowNext = false;
                mBaseView.showMessage(R.string.reader_next_none);
            }
        }
    }

    public void loadPrev() {
        if (status == LOAD_NULL && isShowPrev) {
            Chapter chapter = mPreloadAdapter.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                images(chapter.getPath());
                mBaseView.showMessage(R.string.reader_load_prev);
            } else {
                isShowPrev = false;
                mBaseView.showMessage(R.string.reader_prev_none);
            }
        }
    }

    public void toNextChapter() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        if (chapter != null) {
            mBaseView.onChapterChange(chapter, true);
            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), 1));
        }
    }

    public void toPrevChapter() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter != null) {
            mBaseView.onChapterChange(chapter, false);
            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), chapter.getCount()));
        }
    }

    private void images(final String path) {
        Manga.images(parser, cid, path)
                .subscribeOn(Schedulers.io())
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
                        if (status == LOAD_PREV) {
                            mBaseView.onPrevLoadSuccess(list);
                            chapter = mPreloadAdapter.movePrev();
                        } else {
                            mBaseView.onNextLoadSuccess(list);
                            chapter = mPreloadAdapter.moveNext();
                        }
                        if (status == LOAD_INIT) {
                            if (!chapter.getPath().equals(last) || page == -1) {
                                page = 1;
                            }
                            mBaseView.onFirstLoadSuccess(page, list.size(), chapter.getTitle());
                            RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_CHAPTER_CHANGE, chapter.getPath(), page));
                        }
                        chapter.setCount(list.size());
                    }
                });
    }

}
