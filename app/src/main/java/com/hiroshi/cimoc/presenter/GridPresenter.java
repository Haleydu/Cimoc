package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.GridView;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public abstract class GridPresenter<T extends GridView> extends BasePresenter<T> {

    private Map<Long, MiniComic> mComicMap;

    public GridPresenter() {
        mComicMap = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.COMIC_FILTER, new Action1<RxEvent>() {
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
                        mComicMap.put(ret.getId(), ret);
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
            result.add(mComicMap.get(id));
        }
        mBaseView.onComicLoadSuccess(result);
    }

    private void filter(int id) {
        Collection<MiniComic> collection = mComicMap.values();
        if (id != TagManager.TAG_ALL) {
            Iterator<MiniComic> iterator = collection.iterator();
            while (iterator.hasNext()) {
                MiniComic comic = iterator.next();
                if (id == TagManager.TAG_CONTINUE && comic.isFinish()
                        || id == TagManager.TAG_END && !comic.isFinish()) {
                    iterator.remove();
                }
            }
        }
        mBaseView.onComicFilterSuccess(collection);
    }

    protected abstract Observable<List<Comic>> getRawObservable();

}
