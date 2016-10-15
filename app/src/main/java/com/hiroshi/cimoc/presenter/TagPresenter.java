package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TagView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class TagPresenter extends BasePresenter<TagView> {

    private TagManager mTagManager;

    public TagPresenter() {
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

    public void insert(Tag tag) {
        long id = mTagManager.insert(tag);
        tag.setId(id);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_INSERT, tag));
    }

    public void delete(final Tag tag) {
        mCompositeSubscription.add(mTagManager.runInTx(new Runnable() {
            @Override
            public void run() {
                mTagManager.deleteByTag(tag.getId());
                mTagManager.delete(tag);
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_DELETE, tag));
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onTagDeleteSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagDeleteFail();
                    }
                }));

    }

    public void update(Tag tag) {
        mTagManager.update(tag);
    }

}
