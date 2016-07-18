package com.hiroshi.cimoc.presenter;

import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter {

    private ReaderActivity mReaderActivity;
    private ComicManager mComicManager;
    private Manga mManga;

    private ArrayList<Chapter> mChapterList;
    private int[] mPageArray;

    private boolean isLoading;
    private int prev;
    private int next;

    private int current;
    private int index;

    public ReaderPresenter(ReaderActivity activity, ArrayList<Chapter> list, int position) {
        mReaderActivity = activity;
        mChapterList = list;
        mPageArray = new int[list.size()];
        mComicManager = ComicManager.getInstance();
        mManga = Kami.getMangaById(mComicManager.getSource());
        prev = position + 1;
        next = position;
        index = position;
        current = 1;
    }

    public void initPicture() {
        isLoading = true;
        mManga.browse(mChapterList.get(next).getPath(), Manga.MODE_INIT);
    }


    public OnPageChangeListener getPageChangeListener() {
        return new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
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
                setCurrent(position);
                if (position == 0) {
                    mReaderActivity.clearInformation();
                } else {
                    mReaderActivity.setInformation(mChapterList.get(index).getTitle(), current, mPageArray[index]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        };
    }

    public void setLastPage() {
        mComicManager.setLastPage(current);
    }

    private void setCurrent(int position) {
        int count = 1;
        for (int i = prev - 1; i > next; --i) {
            if (count + mPageArray[i] > position) {
                current = position - count + 1;
                if (index != i) {
                    mComicManager.setLastPath(mChapterList.get(i).getPath());
                    index = i;
                }
                break;
            }
            count += mPageArray[i];
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        String[] array;
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_INIT:
                array = (String[]) msg.getData();
                mReaderActivity.setInitImage(array, next == mChapterList.size() - 1);
                mReaderActivity.setInformation(mChapterList.get(next).getTitle(), 1, array.length);
                mComicManager.setLastPath(mChapterList.get(next).getPath());
                mPageArray[next] = array.length;
                current = 1;
                index = next;
                next = next - 1;
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_PREV:
                array = (String[]) msg.getData();
                mReaderActivity.setPrevImage(array, prev == mChapterList.size() - 1);
                mReaderActivity.setInformation(mChapterList.get(prev).getTitle(), array.length, array.length);
                mPageArray[prev] = array.length;
                prev = prev + 1;
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_NEXT:
                array = (String[]) msg.getData();
                mReaderActivity.setNextImage(array);
                mPageArray[next] = array.length;
                next = next - 1;
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_FAIL:
                break;
        }
    }

}
