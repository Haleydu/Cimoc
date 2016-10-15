package com.hiroshi.cimoc.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.BaseActivity;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment {

    private Unbinder unbinder;
    protected PreferenceManager mPreference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        mPreference = ((CimocApplication) getActivity().getApplication()).getPreferenceManager();
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
        showSnackbar(getString(resId));
    }

    public void showSnackbar(int resId, Object... args) {
        showSnackbar(StringUtils.format(getString(resId), args));
    }

    public void showSnackbar(String msg) {
        HintUtils.showSnackbar(getView(), msg);
    }

    protected void initView() {}

    protected void initData() {}

    protected void initPresenter() {}

    protected abstract @LayoutRes int getLayoutRes();

    protected void showProgressDialog() {
        ((BaseActivity) getActivity()).showProgressDialog();
    }

    protected void hideProgressDialog() {
        ((BaseActivity) getActivity()).hideProgressDialog();
    }

}
