package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Local;
import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.TaskManager;
import com.hiroshi.cimoc.misc.Pair;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.saf.DocumentFile;
import com.hiroshi.cimoc.ui.view.LocalView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public class LocalPresenter extends BasePresenter<LocalView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listLocalInRx()
                .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
                    }
                }));
    }

    public void scan(DocumentFile doc) {
        mCompositeSubscription.add(Local.scan(doc)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Pair<Comic, ArrayList<Task>>>>() {
                    @Override
                    public void call(List<Pair<Comic, ArrayList<Task>>> list) {
                    //    ComicManager.insert(list);
                    //    mBaseView.onLocalAddSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
                    }
                }));
    }

    public void deleteComic(long id) {
        mCompositeSubscription.add(Observable.just(id)
                .doOnNext(new Action1<Long>() {
                    @Override
                    public void call(final Long id) {
                        mComicManager.runInTx(new Runnable() {
                            @Override
                            public void run() {
                                mTaskManager.deleteByComicId(id);
                                mComicManager.deleteByKey(id);
                            }
                        });
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long id) {
                        mBaseView.onLocalDeleteSuccess(id);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onLocalDeleteFail();
                    }
                }));
    }

}
