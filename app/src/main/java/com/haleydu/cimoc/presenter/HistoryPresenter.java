package com.haleydu.cimoc.presenter;

import com.haleydu.cimoc.manager.ComicManager;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.MiniComic;
import com.haleydu.cimoc.rx.RxEvent;
import com.haleydu.cimoc.rx.ToAnotherList;
import com.haleydu.cimoc.ui.view.HistoryView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/18.
 */
public class HistoryPresenter extends BasePresenter<HistoryView> {

    private ComicManager mComicManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemUpdate((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_HISTORY_RESTORE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.OnComicRestore((List<Object>) rxEvent.getData());
            }
        });
    }

    public Comic load(long id) {
        return mComicManager.load(id);
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listHistoryInRx()
                .compose(new ToAnotherList<>(new Func1<Comic, Object>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> list) {
                        mBaseView.onComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
                    }
                }));
    }

    public void delete(long id) {
        Comic comic = mComicManager.load(id);
        comic.setHistory(null);
        mComicManager.updateOrDelete(comic);
        mBaseView.onHistoryDelete(id);
    }

    public void clear() {
        mCompositeSubscription.add(mComicManager.listHistoryInRx()
                .doOnNext(new Action1<List<Comic>>() {
                    @Override
                    public void call(final List<Comic> list) {
                        mComicManager.runInTx(new Runnable() {
                            @Override
                            public void run() {
                                for (Comic comic : list) {
                                    comic.setHistory(null);
                                    mComicManager.updateOrDelete(comic);
                                }
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        mBaseView.onHistoryClearSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }));
    }

}
