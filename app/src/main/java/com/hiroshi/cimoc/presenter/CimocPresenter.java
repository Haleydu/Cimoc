package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.ui.view.BaseView;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/12.
 */
public class CimocPresenter extends BasePresenter<BaseView> {

    private SourceManager mSourceManager;
    private List<Source> mSourceList;

    public CimocPresenter() {
        mSourceManager = SourceManager.getInstance();
    }

    public String[] load() {
        mSourceList = mSourceManager.listEnable();
        String[] array = new String[mSourceList.size()];
        for (int i = 0; i != array.length; ++i) {
            array[i] = SourceManager.getTitle(mSourceList.get(i).getSid());
        }
        return array;
    }

    public int getSid(int location) {
        if (mSourceList.isEmpty()) {
            return -1;
        }
        return mSourceList.get(location).getSid();
    }

}
