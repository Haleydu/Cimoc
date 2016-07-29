package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.adapter.PicturePagerAdapter;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
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
    private ComicManager mComicManager;
    private Manga mManga;

    private int status;
    private boolean load;

    public ReaderPresenter(ReaderActivity activity, int position) {
        mReaderActivity = activity;
        mComicManager = ComicManager.getInstance();
        List<Chapter> list = new ArrayList<>(mComicManager.getChapters());
        Collections.reverse(list);
        mPreloadAdapter = new PreloadAdapter(list.toArray(new Chapter[list.size()]), list.size() - position - 1, PicturePagerAdapter.MAX_COUNT / 2 + 1);
        mManga = Kami.getMangaById(mComicManager.getSource());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        load = false;
        status = LOAD_NEXT;
        mManga.browse(mComicManager.getCid(), mPreloadAdapter.getNextChapter().getPath());
    }

    public void afterRead() {
        if (load) {
            mComicManager.afterRead(mPreloadAdapter.getProgress());
        }
    }

    public void onPageStateIdle(boolean isFirst) {
        if (status == LOAD_NULL) {
            if (isFirst) {
                Chapter chapter = mPreloadAdapter.getPrevChapter();
                if (chapter != null) {
                    status = LOAD_PREV;
                    mManga.browse(mComicManager.getCid(), mPreloadAdapter.getPrevChapter().getPath());
                } else {
                    mReaderActivity.notifyPrevPage(PicturePagerAdapter.STATUS_NULL);
                }
            } else {
                Chapter chapter = mPreloadAdapter.getNextChapter();
                if (chapter != null) {
                    status = LOAD_NEXT;
                    mManga.browse(mComicManager.getCid(), mPreloadAdapter.getNextChapter().getPath());
                } else {
                    mReaderActivity.notifyNextPage(PicturePagerAdapter.STATUS_NULL);
                }
            }
        }
    }

    public void onPageSelected(int position) {
        boolean flag = mPreloadAdapter.moveToPosition(position);
        Chapter chapter = mPreloadAdapter.getValidChapter();
        if (chapter == null) {
            mReaderActivity.hideChapterInfo();
        } else if (flag) {
            switchChapter();
        } else {
            mReaderActivity.setReadProgress(mPreloadAdapter.getProgress());
        }
    }

    public int getSource() {
        return mComicManager.getSource();
    }

    public int getOffset() {
        return mPreloadAdapter.getCurrentOffset();
    }

    private void switchChapter() {
        mReaderActivity.updateChapterInfo(mPreloadAdapter.getProgress(), mPreloadAdapter.getMax(), mPreloadAdapter.getValidChapter().getTitle());
        mComicManager.setLast(mPreloadAdapter.getValidChapter().getPath());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        String[] array;
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_SUCCESS:
                array = (String[]) msg.getData();
                if (status == LOAD_PREV) {
                    mReaderActivity.setPrevImage(array);
                    mPreloadAdapter.movePrev(array.length);
                } else {
                    mReaderActivity.setNextImage(array);
                    mPreloadAdapter.moveNext(array.length);
                }
                switchChapter();
                mReaderActivity.setNoneLimit();
                load = true;
                status = LOAD_NULL;
                break;
            case EventMessage.PARSE_PIC_FAIL:
            case EventMessage.NETWORK_ERROR:
                if (status == LOAD_PREV) {
                    mReaderActivity.notifyPrevPage(PicturePagerAdapter.STATUS_ERROR);
                } else {
                    mReaderActivity.notifyNextPage(PicturePagerAdapter.STATUS_ERROR);
                }
                break;
        }
    }

}
