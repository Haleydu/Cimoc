package com.hiroshi.cimoc.ui.fragment;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SettingsPresenter;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsFragment extends BaseFragment {

    private SettingsPresenter mPresenter;

    @Override
    protected void initPresenter() {
        mPresenter = new SettingsPresenter(this);
    }

    @Override
    protected void initView() {}

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_settings;
    }

}
