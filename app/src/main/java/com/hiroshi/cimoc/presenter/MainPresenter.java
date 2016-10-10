package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.MainView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private ComicManager mComicManager;
    private SourceManager mSourceManager;

    public MainPresenter() {
        mComicManager = ComicManager.getInstance();
        mSourceManager = SourceManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.HISTORY_COMIC, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                MiniComic comic = (MiniComic) rxEvent.getData();
                mBaseView.onLastChange(comic.getSource(), comic.getCid(), comic.getTitle(), comic.getCover());
            }
        });
    }

    public void loadLast() {
        mCompositeSubscription.add(mComicManager.loadLast()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Comic>() {
                    @Override
                    public void call(Comic comic) {
                        if (comic != null) {
                            mBaseView.onLastLoadSuccess(comic.getSource(), comic.getCid(), comic.getTitle(), comic.getCover());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onLastLoadFail();
                    }
                }));
    }

    public void loadSource() {
        mCompositeSubscription.add(mSourceManager.listEnable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Source>>() {
                    @Override
                    public void call(List<Source> list) {
                        mBaseView.onSourceLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onSourceLoadFail();
                    }
                }));
    }

}
