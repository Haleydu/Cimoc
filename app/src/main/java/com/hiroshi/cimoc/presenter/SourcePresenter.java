package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SourceView;

import java.util.List;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

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

    public List<Source> list() {
        return mSourceManager.list();
    }

    public void add(int sid) {
        Source source = null;
        if (!mSourceManager.exist(sid)) {
            long id = mSourceManager.insert(sid);
            source = new Source(id, sid, true);
        }
        mBaseView.onSourceAdd(source);
    }

    public void update(Source source) {
        mSourceManager.update(source);
    }

    public void delete(final Source source) {
        mComicManager.listSource(source.getSid())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        mSourceManager.delete(source.getId());
                        mComicManager.deleteInTx(list);
                    }
                });
        RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_DELETE, source.getSid()));
    }

}
