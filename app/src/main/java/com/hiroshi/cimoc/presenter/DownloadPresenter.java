package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.DownloadView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadPresenter extends BasePresenter<DownloadView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;

    public DownloadPresenter() {
        mTaskManager = TaskManager.getInstance();
        mComicManager = ComicManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.TASK_ADD, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onTaskAdd((MiniComic) rxEvent.getData());
            }
        });
    }

    public void load() {
        mTaskManager.listKey()
                .flatMap(new Func1<List<Long>, Observable<List<MiniComic>>>() {
                    @Override
                    public Observable<List<MiniComic>> call(final List<Long> list) {
                        return mComicManager.callInTx(new Callable<List<MiniComic>>() {
                            @Override
                            public List<MiniComic> call() throws Exception {
                                List<MiniComic> result = new ArrayList<>(list.size());
                                for (long id : list) {
                                    result.add(new MiniComic(mComicManager.load(id)));
                                }
                                return result;
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onLoadSuccess(list);
                    }
                });
    }

}
