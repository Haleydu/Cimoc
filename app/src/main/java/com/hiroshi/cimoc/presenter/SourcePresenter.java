package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.ui.fragment.SourceFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourcePresenter extends BasePresenter {

    private SourceFragment mSourceFragment;
    private SourceManager mSourceManager;
    private ComicManager mComicManager;

    public SourcePresenter(SourceFragment fragment) {
        mSourceFragment = fragment;
        mSourceManager = SourceManager.getInstance();
        mComicManager = ComicManager.getInstance();
    }

    public List<Source> list() {
        return mSourceManager.list();
    }

    public void add(int sid) {
        if (mSourceManager.exist(sid)) {
            mSourceFragment.showSnackbar(R.string.source_add_exist);
        } else {
            long id = mSourceManager.insert(sid);
            mSourceFragment.addItem(new Source(id, sid, true));
            mSourceFragment.showSnackbar(R.string.source_add_success);
        }
    }

    public void update(Source source) {
        mSourceManager.update(source);
    }

    public void delete(Source source) {
        mSourceManager.delete(source.getId());
        mComicManager.deleteBySource(source.getSid());
        EventBus.getDefault().post(new EventMessage(EventMessage.COMIC_DELETE, source.getSid()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
    }

}
