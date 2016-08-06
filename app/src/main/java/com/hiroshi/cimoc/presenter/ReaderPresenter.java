package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.PageReaderActivity;
import com.hiroshi.cimoc.ui.adapter.PreloadAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class ReaderPresenter extends BasePresenter {

    private final static int LOAD_NULL = 0;
    private final static int LOAD_PREV = 1;
    private final static int LOAD_NEXT = 2;

    private PageReaderActivity mPageReaderActivity;
    private PreloadAdapter mPreloadAdapter;
    private Manga mManga;

    private String cid;
    private String last;
    private Integer page;
    private int status;

    public ReaderPresenter(PageReaderActivity activity, int source, String cid, String last, Integer page, Chapter[] array, int position) {
        mPageReaderActivity = activity;
        mPreloadAdapter = new PreloadAdapter(array, position);
        mManga = Kami.getMangaById(source);
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
        if (status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getNextChapter();
            if (chapter != null) {
                status = LOAD_NEXT;
                mManga.browse(cid, chapter.getPath());
                mPageReaderActivity.showToast("正在加载下一话");
            } else {
                mPageReaderActivity.showToast("后面没有了");
            }
        }
    }

    public void loadPrev() {
        if (status == LOAD_NULL) {
            Chapter chapter = mPreloadAdapter.getPrevChapter();
            if (chapter != null) {
                status = LOAD_PREV;
                mManga.browse(cid, chapter.getPath());
                mPageReaderActivity.showToast("正在加载上一话");
            } else {
                mPageReaderActivity.showToast("前面没有了");
            }
        }
    }

    public void onChapterToNext() {
        Chapter chapter = mPreloadAdapter.nextChapter();
        switchChapter(1, chapter.getCount(), chapter.getTitle(), chapter.getPath());
    }

    public void onChapterToPrev() {
        Chapter chapter = mPreloadAdapter.prevChapter();
        switchChapter(chapter.getCount(), chapter.getCount(), chapter.getTitle(), chapter.getPath());
    }

    private void switchChapter(int progress, int count, String title, String path) {
        mPageReaderActivity.updateChapterInfo(count, title);
        mPageReaderActivity.setReadProgress(progress);
        EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_LAST_CHANGE, path));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.PARSE_PIC_SUCCESS:
                String[] array = (String[]) msg.getData();
                Chapter chapter;
                if (!mPreloadAdapter.isLoad()) {
                    mPageReaderActivity.setNextImage(array);
                    chapter = mPreloadAdapter.moveNext();
                    if (!chapter.getPath().equals(last) || page == null) {
                        page = 1;
                    }
                    mPageReaderActivity.initLoad(page, array.length, chapter.getTitle());
                    EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_LAST_CHANGE, chapter.getPath()));
                } else {
                    if (status == LOAD_PREV) {
                        mPageReaderActivity.setPrevImage(array);
                        chapter = mPreloadAdapter.movePrev();
                    } else {
                        mPageReaderActivity.setNextImage(array);
                        chapter = mPreloadAdapter.moveNext();
                    }
                    mPageReaderActivity.loadSuccess(status == LOAD_NEXT);
                    mPageReaderActivity.showToast("加载成功");
                }
                chapter.setCount(array.length);
                status = LOAD_NULL;
                break;
            case EventMessage.PARSE_PIC_FAIL:
            case EventMessage.NETWORK_ERROR:
                mPageReaderActivity.showToast("加载错误");
                break;
        }
    }

}
