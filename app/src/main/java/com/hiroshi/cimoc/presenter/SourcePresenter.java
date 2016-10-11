package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SourceView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourcePresenter extends BasePresenter<SourceView> {

    private SourceManager mSourceManager;
    private ComicManager mComicManager;

    public SourcePresenter() {
        mSourceManager = SourceManager.getInstance();
        mComicManager = ComicManager.getInstance();
    }

    public void load() {
        mCompositeSubscription.add(mSourceManager.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Source>>() {
                    @Override
                    public void call(List<Source> list) {
                        mBaseView.onSourceLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onSourceLoadFail();
                    }
                }));
    }

    public void insert(Source source) {
        long id = mSourceManager.insert(source);
        source.setId(id);
        mBaseView.onSourceAdd(source);
    }

    public void update(Source source) {
        mSourceManager.update(source);
    }

    public void delete(final long id, final int sid, final int position) {
        mCompositeSubscription.add(mComicManager.listSource(sid)
                .doOnNext(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        mSourceManager.deleteByKey(id);
                        mComicManager.deleteInTx(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_DELETE, sid));
                        mBaseView.onSourceDeleteSuccess(position);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onSourceDeleteFail();
                    }
                }));
    }

}
