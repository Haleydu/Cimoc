package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.ComicView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicPresenter extends BasePresenter<ComicView> {

    private TagManager mTagManager;

    public ComicPresenter() {
        mTagManager = TagManager.getInstance();
    }

    public void load() {
        mCompositeSubscription.add(mTagManager.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Tag>>() {
                    @Override
                    public void call(List<Tag> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagLoadFail();
                    }
                }));
    }

    public void filter(int type, long id) {
        if (type != TagManager.TAG_NORMAL) {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FILTER, type));
        } else {
            mCompositeSubscription.add(mTagManager.listByTag(id)
                    .flatMap(new Func1<List<TagRef>, Observable<TagRef>>() {
                        @Override
                        public Observable<TagRef> call(List<TagRef> tagRefs) {
                            return Observable.from(tagRefs);
                        }
                    })
                    .map(new Func1<TagRef, Long>() {
                        @Override
                        public Long call(TagRef ref) {
                            return ref.getCid();
                        }
                    })
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Long>>() {
                        @Override
                        public void call(List<Long> list) {
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FILTER, TagManager.TAG_NORMAL, list));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mBaseView.onFilterFail();
                        }
                    }));
        }
    }

}
