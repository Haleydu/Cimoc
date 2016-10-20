package com.hiroshi.cimoc.ui.fragment;

import android.app.Fragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.BaseActivity;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment {

    private Unbinder unbinder;
    protected PreferenceManager mPreference;
    @Nullable @BindView(R.id.custom_progress_bar) ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        mPreference = ((CimocApplication) getActivity().getApplication()).getPreferenceManager();
        initPresenter();
        initProgressBar();
        initView();
        initData();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initProgressBar() {
        if (mProgressBar != null) {
            int resId = ThemeUtils.getResourceId(getActivity(), R.attr.colorAccent);
            mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), resId), PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void showSnackbar(int resId) {
        showSnackbar(getString(resId));
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

    protected void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

}
