package com.hiroshi.cimoc.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hiroshi.cimoc.ui.activity.MainActivity;

import java.util.Locale;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutView(), container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initView();
        initData();
        return view;
    }

    public void showSnackbar(int resId) {
        showSnackbar(getString(resId));
    }

    public void showSnackbar(int resId, Object... args) {
        showSnackbar(String.format(Locale.CHINA, getString(resId), args));
    }

    public void showSnackbar(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    protected void showProgressDialog() {
        ((MainActivity) getActivity()).showProgressDialog();
    }

    protected void hideProgressDialog() {
        ((MainActivity) getActivity()).hideProgressDialog();
    }

    protected void initView() {}

    protected void initData() {}

    protected void initPresenter() {}

    protected abstract int getLayoutView();

}
