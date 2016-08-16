package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_PREV = 1;
    private final static int LOAD_NEXT = 2;

    private ReaderActivity mReaderActivity;
    private PreloadAdapter mPreloadAdapter;
    private Manga mManga;

    private boolean isShowNext;
    private boolean isShowPrev;
    private int count;

    private String cid;
    private String last;
    private int page;
    private int status;

    public ReaderPresenter(ReaderActivity activity, int source, String cid, String last, int page, Chapter[] array, int position) {
        mReaderActivity = activity;
        mPreloadAdapter = new PreloadAdapter(array, position);
        mManga = SourceManager.getManga(source);
        isShowNext = true;
        isShowPrev = true;
        count = 0;
        this.cid = cid;
        this.last = last;
        this.page = page;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        status = LOAD_NEXT;
        mManga.browse(cid, mPreloadAdapter.getNextChapter().getPath());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManga.cancel();
    }

    public void setPage(int progress) {
        if (mPreloadAdapter.isLoad()) {
            EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_PAGE_CHANGE, progress));
        }
    }

    public void loadNext() {
        if (status == LOAD_NULL && isShowNext) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                mManga.browse(cid, chapter.getPath());
                mReaderActivity.showToast(R.string.reader_load_next);
            } else {
                isShowNext = false;
                mReaderActivity.showToast(R.string.reader_next_none);
            }
        }
    }

    public void loadPrev() {
        if (status == LOAD_NULL && isShowPrev) {
            Chapter chapter = mPreloadAdapter.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                mManga.browse(cid, chapter.getPath());
                mReaderActivity.showToast(R.string.reader_load_prev);
            } else {
                isShowPrev = false;
                mReaderActivity.showToast(R.string.reader_prev_none);
            }
        }
    }

    public void toNextChapter() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        if (chapter == null) {
            return;
        }
        switchChapter(1, chapter.getCount(), chapter.getTitle(), chapter.getPath());
    }

    public void toPrevChapter() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter == null) {
            return;
        }
        switchChapter(chapter.getCount(), chapter.getCount(), chapter.getTitle(), chapter.getPath());
    }

    private void switchChapter(int progress, int count, String title, String path) {
        mReaderActivity.updateChapterInfo(count, title);
        mReaderActivity.setReadProgress(progress);
        EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_LAST_CHANGE, path, progress));
    }

    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_SUCCESS:
                List<String> list = (List<String>) msg.getData();
                Chapter chapter;
                if (!mPreloadAdapter.isLoad()) {
                    mReaderActivity.setNextImage(list);
                    chapter = mPreloadAdapter.moveNext();
                    if (!chapter.getPath().equals(last) || page == -1) {
                        page = 1;
                    }
                    mReaderActivity.initLoad(page, list.size(), chapter.getTitle());
                    EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_LAST_CHANGE, chapter.getPath(), page));
                } else {
                    if (status == LOAD_PREV) {
                        mReaderActivity.setPrevImage(list);
                        chapter = mPreloadAdapter.movePrev();
                    } else {
                        mReaderActivity.setNextImage(list);
                        chapter = mPreloadAdapter.moveNext();
                    }
                    mReaderActivity.loadSuccess();
                }
                chapter.setCount(list.size());
                status = LOAD_NULL;
                break;
            case EventMessage.PARSE_PIC_FAIL:
            case EventMessage.NETWORK_ERROR:
                mReaderActivity.showToast(R.string.reader_load_error);
                if (mPreloadAdapter.isLoad() && ++count < 2) {
                    status = LOAD_NULL;
                }
                break;
        }
    }

}
