package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Tag;
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

    public void insert(String title) {
        Tag tag = new Tag(null, title);
        long id = mTagManager.insert(tag);
        tag.setId(id);
        mBaseView.onTagAddSuccess(tag);
    }

    public void update(Tag tag) {
        mTagManager.update(tag);
    }

}
