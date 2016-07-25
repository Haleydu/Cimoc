package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.model.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter {

    private final static int PARSE_NULL = 0;
    private final static int PARSE_INIT = 1;
    private final static int PARSE_PREV = 2;
    private final static int PARSE_NEXT = 3;

    private ReaderActivity mReaderActivity;
    private ComicManager mComicManager;
    private Manga mManga;

    private List<Chapter> mChapterList;

    private int status;
    private int prev;
    private int next;
    private int index;

    public ReaderPresenter(ReaderActivity activity, int position) {
        mReaderActivity = activity;
        mComicManager = ComicManager.getInstance();
        mChapterList = mComicManager.getChapters();
        mManga = Kami.getMangaById(mComicManager.getSource());
        prev = position + 1;
        next = position;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        status = PARSE_INIT;
        mManga.browse(mComicManager.getCid(), mChapterList.get(next).getPath());
    }

    public void setPage(int progress) {
        mComicManager.setPage(progress);
    }

    public void onPageSelected(int position) {
        if (status == PARSE_NULL) {
            if (position == 0 && prev < mChapterList.size()) {
                status = PARSE_PREV;
                mManga.browse(mComicManager.getCid(), mChapterList.get(prev).getPath());
            } else if (position == mReaderActivity.getCount() - 1 && next >= 0) {
                status = PARSE_NEXT;
                mManga.browse(mComicManager.getCid(), mChapterList.get(next).getPath());
            }
        }
        onPositionChange(position);
    }

    public int getSource() {
        return mComicManager.getSource();
    }

    public int getOffset() {
        int offset = 0;
        for (int i = prev - 1; i > index; --i) {
            offset += mChapterList.get(i).getCount();
        }
        return offset;
    }

    private void onPositionChange(int position) {
        if (position == 0) {
            mReaderActivity.clearInformation();
            return;
        }
        for (int i = prev - 1, total = 1; i > next; --i) {
            Chapter chapter = mChapterList.get(i);
            if (total + chapter.getCount() > position) {
                int progress = position - total + 1;
                if (index != i || position == 1) {
                    switchChapter(i, progress);
                } else {
                    mReaderActivity.setReadProgress(progress);
                }
                break;
            }
            total += chapter.getCount();
        }
    }

    private void switchChapter(int which, int progress) {
        Chapter chapter = mChapterList.get(which);
        mReaderActivity.setReadMax(chapter.getCount(), progress);
        mReaderActivity.setTitle(chapter.getTitle());
        mComicManager.setLast(chapter.getPath());
        index = which;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        String[] array;
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_SUCCESS:
                array = (String[]) msg.getData();
                if (status == PARSE_INIT) {
                    mChapterList.get(next).setCount(array.length);
                    mReaderActivity.setInitImage(array, next == mChapterList.size() - 1);
                    switchChapter(next, 1);
                    next = next - 1;
                } else if (status == PARSE_PREV) {
                    mChapterList.get(prev).setCount(array.length);
                    mReaderActivity.setPrevImage(array, prev == mChapterList.size() - 1);
                    switchChapter(prev, array.length);
                    prev = prev + 1;
                } else if (status == PARSE_NEXT) {
                    mChapterList.get(next).setCount(array.length);
                    mReaderActivity.setNextImage(array);
                    next = next - 1;
                }
                status = PARSE_NULL;
                break;
            case EventMessage.PARSE_PIC_FAIL:
                break;
        }
    }

}
