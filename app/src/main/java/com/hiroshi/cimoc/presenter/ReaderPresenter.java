package com.hiroshi.cimoc.presenter;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.ui.activity.ReaderActivity;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter {

    private ReaderActivity mReaderActivity;
    private Manga mManga;
    private boolean isLoading;

    public ReaderPresenter(ReaderActivity activity, int source) {
        mReaderActivity = activity;
        mManga = Kami.getMangaById(source);
    }

    public void initPicture(String path) {
        isLoading = true;
        mManga.browse(path, true);
    }

    public OnPageChangeListener getPageChangeListener() {
        return new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if (!isLoading) {
                    if (position == 0) {
                        String path = mReaderActivity.getPrevPath();
                        if (path != null) {
                            isLoading = true;
                            mManga.browse(path, false);
                        }
                    } else if (position == mReaderActivity.getCount() - 1) {
                        String path = mReaderActivity.getNextPath();
                        if (path != null) {
                            isLoading = true;
                            mManga.browse(path, true);
                        }
                    }
                }
                mReaderActivity.setInformation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        String[] array;
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_PREV:
                array = (String[]) msg.getData();
                mReaderActivity.setPrevImage(array);
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_NEXT:
                array = (String[]) msg.getData();
                mReaderActivity.setNextImage(array);
                isLoading = false;
                break;
            case EventMessage.PARSE_PIC_FAIL:
                break;
        }
    }

}
