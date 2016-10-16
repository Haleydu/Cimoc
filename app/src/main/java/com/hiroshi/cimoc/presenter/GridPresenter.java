package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.GridView;

import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public abstract class GridPresenter<T extends GridView> extends BasePresenter<T> {

    protected LongSparseArray<MiniComic> mComicArray;

    public GridPresenter() {
        mComicArray = new LongSparseArray<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_FILTER, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                int type = (int) rxEvent.getData();
                if (type == TagManager.TAG_NORMAL) {
                    filter((List<Long>) rxEvent.getData(1));
                } else {
                    filter(type);
                }
            }
        });
        addSubscription(RxEvent.EVENT_THEME_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onThemeChange((int) rxEvent.getData(1), (int) rxEvent.getData(2));
            }
        });
    }

    public void loadComic() {
        mCompositeSubscription.add(getRawObservable()
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
                        return Observable.from(list);
                    }
                })
                .map(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        MiniComic ret = new MiniComic(comic);
                        mComicArray.put(ret.getId(), ret);
                        return ret;
                    }
                })
                .toList()
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

    private void filter(List<Long> list) {
        List<MiniComic> result = new LinkedList<>();
        for (Long id : list) {
            MiniComic comic = mComicArray.get(id);
            if (comic != null) {
                result.add(comic);
            }
        }
        mBaseView.onComicFilterSuccess(result);
    }

    private void filter(int type) {
        List<MiniComic> list = new LinkedList<>();
        for (int i = 0; i != mComicArray.size(); ++i) {
            if (type != TagManager.TAG_ALL) {
                Boolean finish = mComicArray.valueAt(i).isFinish();
                if (type == TagManager.TAG_CONTINUE && (finish == null || !finish) ||
                        type == TagManager.TAG_END && finish != null && finish) {
                    list.add(mComicArray.valueAt(i));
                }
            } else {
                list.add(mComicArray.valueAt(i));
            }
        }
        mBaseView.onComicFilterSuccess(list);
    }

    protected abstract Observable<List<Comic>> getRawObservable();

}
