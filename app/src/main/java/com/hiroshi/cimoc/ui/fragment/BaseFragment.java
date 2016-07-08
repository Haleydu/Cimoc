package com.hiroshi.cimoc.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiroshi.cimoc.presenter.BasePresenter;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutView(), container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        if (getPresenter() != null) {
            getPresenter().register();
        }
        initView();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getPresenter() != null) {
            getPresenter().unregister();
        }
    }

    protected abstract void initView();

    protected abstract @LayoutRes int getLayoutView();

    protected abstract BasePresenter getPresenter();

    protected abstract void initPresenter();

}
