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

    private ReaderActivity mReaderActivity;
    private ComicManager mComicManager;
    private Manga mManga;

    private List<Chapter> mChapterList;

    private boolean isLoading;
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
        isLoading = true;
        mManga.browse(mChapterList.get(next).getPath(), Manga.MODE_INIT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mComicManager.setLast(mReaderActivity.getReadProgress());
    }

    public void onPageSelected(int position) {
        if (!isLoading) {
            if (position == 0 && prev < mChapterList.size()) {
                String path = mChapterList.get(prev).getPath();
                isLoading = true;
                mManga.browse(path, Manga.MODE_PREV);
            } else if (position == mReaderActivity.getCount() - 1 && next >= 0) {
                String path = mChapterList.get(next).getPath();
                isLoading = true;
                mManga.browse(path, Manga.MODE_NEXT);
            }
        }
        onPositionChange(position);
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
        mComicManager.setLastPath(chapter.getPath());
        index = which;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        String[] array;
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_INIT:
                array = (String[]) msg.getData();
                mReaderActivity.setInitImage(array, next == mChapterList.size() - 1);
                mChapterList.get(next).setCount(array.length);
                switchChapter(next, 1);
                next = next - 1;
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_PREV:
                array = (String[]) msg.getData();
                mReaderActivity.setPrevImage(array, prev == mChapterList.size() - 1);
                mChapterList.get(prev).setCount(array.length);
                switchChapter(prev, array.length);
                prev = prev + 1;
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_NEXT:
                array = (String[]) msg.getData();
                mReaderActivity.setNextImage(array);
                mChapterList.get(next).setCount(array.length);
                next = next - 1;
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_FAIL:
                break;
        }
    }

}
