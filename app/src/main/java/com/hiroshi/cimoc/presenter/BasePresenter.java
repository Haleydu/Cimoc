package com.hiroshi.cimoc.presenter;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public abstract class BasePresenter {

    public void onStart() {
        EventBus.getDefault().register(this);
    }

    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

}
