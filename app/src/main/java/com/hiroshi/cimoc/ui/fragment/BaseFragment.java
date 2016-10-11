package com.hiroshi.cimoc.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiroshi.cimoc.ui.activity.MainActivity;
import com.hiroshi.cimoc.utils.HintUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment {

    private Unbinder unbinder;
    protected boolean isInit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutView(), container, false);
        unbinder = ButterKnife.bind(this, view);
        isInit = false;
        initPresenter();
        initView();
        initData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void showSnackbar(int resId) {
        HintUtils.showSnackBar(getView(), getString(resId));
    }

    public void showSnackbar(int resId, Object... args) {
        HintUtils.showSnackBar(getView(), getString(resId), args);
    }

    public void showSnackbar(String msg) {
        HintUtils.showSnackBar(getView(), msg);
    }

    protected void initView() {}

    protected void initData() {}

    protected void initPresenter() {}

    protected abstract @LayoutRes int getLayoutView();

}
