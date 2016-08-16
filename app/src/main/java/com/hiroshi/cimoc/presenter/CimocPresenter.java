package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.ui.fragment.CimocFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/12.
 */
public class CimocPresenter extends BasePresenter {

    private CimocFragment mCimocFragment;
    private SourceManager mSourceManager;
    private List<Source> mSourceList;

    public CimocPresenter(CimocFragment fragment) {
        mCimocFragment = fragment;
        mSourceManager = SourceManager.getInstance();
    }

    public String[] getItems() {
        mSourceList = mSourceManager.listEnable();
        String[] items = new String[mSourceList.size()];
        for (int i = 0; i != items.length; ++i) {
            items[i] = mCimocFragment.getString(SourceManager.getTitle(mSourceList.get(i).getSid()));
        }
        return items;
    }

    public int getSid(int location) {
        if (mSourceList == null) {
            getItems();
        }
        if (mSourceList.isEmpty()) {
            return -1;
        }
        return mSourceList.get(location).getSid();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
    }

}
