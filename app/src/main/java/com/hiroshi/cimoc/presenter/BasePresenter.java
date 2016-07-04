package com.hiroshi.cimoc.presenter;

import android.app.Activity;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public abstract class BasePresenter {

    public BasePresenter() {
        EventBus.getDefault().register(this);
    }

    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

}
