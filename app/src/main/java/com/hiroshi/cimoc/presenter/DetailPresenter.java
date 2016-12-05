package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter<DetailView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private TagManager mTagManager;
    private Comic mComic;

    public DetailPresenter() {
        mComicManager = ComicManager.getInstance();
        mTaskManager = TaskManager.getInstance();
        mTagManager = TagManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComic = mComicManager.load(mComic.getId());
                mBaseView.onChapterChange(mComic.getLast());
            }
        });
        addSubscription(RxEvent.EVENT_TASK_INSERT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComic = mComicManager.load(((MiniComic) rxEvent.getData()).getId());
            }
        });
    }

    public void loadDetail(int source, String cid) {
        mComic = mComicManager.loadOrCreate(source, cid);
        cancelHighlight();
        loadDetail();
    }

    public void loadDetail(long id) {
        mComic = mComicManager.load(id);
        cancelHighlight();
        loadDetail();
    }

    private void updateChapterList(List<Chapter> list) {
        Map<String, Task> map = new HashMap<>();
        for (Task task : mTaskManager.list(mComic.getId())) {
            map.put(task.getPath(), task);
        }
        if (!map.isEmpty()) {
            for (Chapter chapter : list) {
                Task task = map.get(chapter.getPath());
                if (task != null) {
                    chapter.setDownload(true);
                    chapter.setComplete(task.isFinish());
                }
            }
        }
    }

    private void loadDetail() {
        mCompositeSubscription.add(Manga.info(mComic)
                .doOnNext(new Action1<List<Chapter>>() {
                    @Override
                    public void call(List<Chapter> list) {
                        if (mComic.getId() != null) {
                            updateChapterList(list);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Chapter>>() {
                    @Override
                    public void call(List<Chapter> list) {
                        mBaseView.onComicLoadSuccess(mComic);
                        mBaseView.onChapterLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadSuccess(mComic);
                        if (throwable instanceof Manga.NetworkErrorException) {
                            mBaseView.onNetworkError();
                        } else {
                            mBaseView.onParseError();
                        }
                    }
                }));
    }

    private void cancelHighlight() {
        if (mComic.getHighlight()) {
            mComic.setHighlight(false);
            mComic.setFavorite(System.currentTimeMillis());
            mComicManager.update(mComic);
        }
    }

    /**
     * 更新最后阅读
     * @param path 最后阅读
     * @param favorite 是否从收藏界面进入
     */
    public void updateLast(String path, boolean favorite) {
        if (favorite && mComic.getFavorite() != null) {     // 从收藏界面进入且没有取消收藏
            mComic.setFavorite(System.currentTimeMillis());
        }
        mComic.setHistory(System.currentTimeMillis());
        if (!path.equals(mComic.getLast())) {
            mComic.setLast(path);
            mComic.setPage(1);
        }
        mComicManager.updateOrInsert(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_READ, new MiniComic(mComic), favorite));
    }

    public long getId() {
        return mComic.getId() == null ? -1 : mComic.getId();
    }

    public void onFavoriteClick() {
        if (mComic.getFavorite() != null) {
            unfavoriteComic();
        } else {
            favoriteComic();
        }
    }

    public void onTagClick() {
        if (mComic.getFavorite() != null) {
            mBaseView.onTagOpenSuccess();
        } else {
            mBaseView.onTagOpenFail();
        }
    }

    public void onTitleClick(String path, boolean favorite) {
        if (mComic.getLast() != null) {
            path = mComic.getLast();
        }
        updateLast(path, favorite);
        mBaseView.onLastOpenSuccess(path);
    }

    private void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        mComicManager.updateOrInsert(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FAVORITE, new MiniComic(mComic)));
        mBaseView.onFavoriteSuccess();
    }

    private void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        mTagManager.deleteByComic(id);
        mComicManager.updateOrDelete(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UNFAVORITE, id));
        mBaseView.onUnfavoriteSuccess();
    }

}
