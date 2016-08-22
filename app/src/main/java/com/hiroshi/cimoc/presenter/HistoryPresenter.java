package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.HistoryView;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/18.
 */
public class HistoryPresenter extends BasePresenter<HistoryView> {

    private ComicManager mComicManager;

    public HistoryPresenter() {
        mComicManager = ComicManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.FAVORITE_COMIC, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemUpdate((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.DELETE_HISTORY, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemClear((int) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.COMIC_DELETE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onSourceRemove((int) rxEvent.getData());
            }
        });
    }

    public List<MiniComic> getComic() {
        return mComicManager.listHistory();
    }

    public void deleteHistory(MiniComic comic) {
        mComicManager.deleteHistory(comic.getId());
    }

    public void clearHistory() {
        mComicManager.cleanHistory();
    }

}
