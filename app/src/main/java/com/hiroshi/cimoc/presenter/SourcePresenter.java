package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SourceView;

import java.util.List;

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

    public void delete(Source source) {
        mSourceManager.delete(source.getId());
        mComicManager.deleteBySource(source.getSid());
        RxBus.getInstance().post(new RxEvent(RxEvent.COMIC_DELETE, source.getSid()));
    }

}
