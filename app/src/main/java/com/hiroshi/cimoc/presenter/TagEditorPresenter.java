package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.manager.TagManager;
import com.hiroshi.cimoc.manager.TagRefManager;
import com.hiroshi.cimoc.misc.Switcher;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.TagEditorView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorPresenter extends BasePresenter<TagEditorView> {

    private TagManager mTagManager;
    private TagRefManager mTagRefManager;
    private long mComicId;
    private Set<Long> mTagSet;

    @Override
    protected void onViewAttach() {
        mTagManager = TagManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
        mTagSet = new HashSet<>();
    }

    public void load(long id) {
        mComicId = id;
        mCompositeSubscription.add(mTagManager.listInRx()
                .doOnNext(new Action1<List<Tag>>() {
                    @Override
                    public void call(List<Tag> list) {
                        for (TagRef ref : mTagRefManager.listByComic(mComicId)) {
                            mTagSet.add(ref.getTid());
                        }
                    }
                })
                .compose(new ToAnotherList<>(new Func1<Tag, Switcher<Tag>>() {
                    @Override
                    public Switcher<Tag> call(Tag tag) {
                        return new Switcher<>(tag, mTagSet.contains(tag.getId()));
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Switcher<Tag>>>() {
                    @Override
                    public void call(List<Switcher<Tag>> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagLoadFail();
                    }
                }));
    }

    private void updateInTx(final List<Long> list) {
        mTagRefManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (long id : list) {
                    if (!mTagSet.contains(id)) {
                        mTagRefManager.insert(new TagRef(null, id, mComicId));
                    }
                }
                mTagSet.removeAll(list);
                for (long id : mTagSet) {
                    mTagRefManager.delete(id, mComicId);
                }
            }
        });
    }

    public void updateRef(List<Long> list) {
        mCompositeSubscription.add(Observable.just(list)
                .doOnNext(new Action1<List<Long>>() {
                    @Override
                    public void call(List<Long> list) {
                        updateInTx(list);
                        mTagSet.clear();
                        mTagSet.addAll(list);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Long>>() {
                    @Override
                    public void call(List<Long> list) {
                        mBaseView.onTagUpdateSuccess();
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_UPDATE, mComicId, list));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagUpdateFail();
                    }
                }));
    }

}
