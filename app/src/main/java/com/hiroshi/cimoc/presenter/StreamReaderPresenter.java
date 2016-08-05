package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.StreamReaderActivity;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderPresenter extends BasePresenter {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_PREV = 1;
    private final static int LOAD_NEXT = 2;

    private StreamReaderActivity mStreamReaderActivity;
    private PreloadAdapter mPreloadAdapter;
    private ComicManager mComicManager;
    private Manga mManga;

    private int status;

    public StreamReaderPresenter(StreamReaderActivity activity, int position) {
        mStreamReaderActivity = activity;
        mComicManager = ComicManager.getInstance();
        mPreloadAdapter = new PreloadAdapter(mComicManager.getChapters(), position);
        mManga = Kami.getMangaById(mComicManager.getSource());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        status = LOAD_NEXT;
        mManga.browse(mComicManager.getCid(), mPreloadAdapter.getNextChapter().getPath());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mManga.cancel();
    }

    public void onScrolled(int dy, int last, int count) {
        if (last >= count - 1 && dy > 0 && status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                mManga.browse(mComicManager.getCid(), chapter.getPath());
            }
        } else if (last <= 1 && dy < 0 && status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                mManga.browse(mComicManager.getCid(), chapter.getPath());
            }
        }
    }

    public void afterRead(int progress) {
            mComicManager.afterRead(progress);
    }

    public void onChapterToNext() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        if (chapter != null) {
            switchChapter(1, chapter.getCount(), chapter.getTitle(), chapter.getPath());
        }
    }

    public void onChapterToPrev() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        if (chapter != null) {
            switchChapter(chapter.getCount(), chapter.getCount(), chapter.getTitle(), chapter.getPath());
        }
    }

    public void onProgressChanged(int value, boolean fromUser) {
    }

    public int getSource() {
        return mComicManager.getSource();
    }

    private void switchChapter(int progress, int max, String title, String path) {
        mStreamReaderActivity.updateChapterInfo(max, title);
        if (progress != -1) {
            mStreamReaderActivity.setReadProgress(progress);
        }
        mComicManager.setLast(path);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_SUCCESS:
                String[] array = (String[]) msg.getData();
                Chapter chapter;
                if (status == LOAD_PREV) {
                    mStreamReaderActivity.setPrevImage(array);
                    chapter = mPreloadAdapter.movePrev();
                } else {
                    mStreamReaderActivity.setNextImage(array);
                    chapter = mPreloadAdapter.moveNext();
                }
                chapter.setCount(array.length);
                switchChapter(1, array.length, chapter.getTitle(), chapter.getPath());
                status = LOAD_NULL;
                break;
            case EventMessage.PARSE_PIC_FAIL:
            case EventMessage.NETWORK_ERROR:
                break;
        }
    }

}
