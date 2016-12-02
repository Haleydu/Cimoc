package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TagEditorView;
import com.hiroshi.cimoc.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class TagEditorPresenter extends BasePresenter<TagEditorView> {

    private TagManager mTagManager;
    private ComicManager mComicManager;
    private MiniComic mMiniComic;
    private Set<Long> mTagSet;

    public TagEditorPresenter() {
        mTagManager = TagManager.getInstance();
        mComicManager = ComicManager.getInstance();
    }

    public void load(long id) {
        mMiniComic = new MiniComic(mComicManager.load(id));
        mCompositeSubscription.add(mTagManager.listInRx()
                .map(new Func1<List<Tag>, List<Pair<Tag, Boolean>>>() {
                    @Override
                    public List<Pair<Tag, Boolean>> call(List<Tag> list) {
                        initTagSet();
                        List<Pair<Tag, Boolean>> result = new ArrayList<>();
                        for (Tag tag : list) {
                            result.add(Pair.create(tag, mTagSet.contains(tag.getId())));
                        }
                        return result;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Pair<Tag, Boolean>>>() {
                    @Override
                    public void call(List<Pair<Tag, Boolean>> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagLoadFail();
                    }
                }));
    }

    private void initTagSet() {
        mTagSet = new HashSet<>();
        for (TagRef ref : mTagManager.listByComic(mMiniComic.getId())) {
            mTagSet.add(ref.getTid());
        }
    }

    private void updateInTx(final List<Long> dList, final List<Long> iList) {
        mTagManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Long id : dList) {
                    mTagManager.delete(id, mMiniComic.getId());
                }
                for (Long id : iList) {
                    mTagManager.insert(new TagRef(null, id, mMiniComic.getId()));
                }
            }
        });
    }

    private void updateTagSet(final List<Long> dList, final List<Long> iList) {
        mTagSet.removeAll(dList);
        mTagSet.addAll(iList);
    }

    public void updateRef(final List<Long> list) {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                List<Long> dList = new ArrayList<>(CollectionUtils.minus(mTagSet, list));
                List<Long> iList = new ArrayList<>(CollectionUtils.minus(list, mTagSet));
                if (!dList.isEmpty() || !iList.isEmpty()) {
                    updateInTx(dList, iList);
                    updateTagSet(dList, iList);
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_UPDATE, mMiniComic));
                    subscriber.onNext(true);
                } else {
                    subscriber.onNext(false);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean flag) {
                        if (flag) {
                            mBaseView.onTagUpdateSuccess();
                        } else {
                            mBaseView.onTagUpdateInvalid();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagUpdateFail();
                    }
                }));
    }

}
