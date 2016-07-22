package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.fragment.SettingsFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter {

    private SettingsFragment mSettingsFragment;

    public SettingsPresenter(SettingsFragment fragment) {
        mSettingsFragment = fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {

    }

}
