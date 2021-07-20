package com.haleydu.cimoc.ui.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.R;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.presenter.BasePresenter;
import com.haleydu.cimoc.ui.activity.BaseActivity;
import com.haleydu.cimoc.ui.view.BaseView;
import com.haleydu.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseFragment extends Fragment implements BaseView {

    protected PreferenceManager mPreference;
    @Nullable
    @BindView(R.id.custom_progress_bar)
    ProgressBar mProgressBar;
    private Unbinder unbinder;
    private BasePresenter mBasePresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        mPreference = App.getPreferenceManager();
        mBasePresenter = initPresenter();
        initProgressBar();
        initView();
        initData();
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mBasePresenter != null) {
            mBasePresenter.detachView();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public App getAppInstance() {
        return (App) requireActivity().getApplication();
    }

    @Override
    public void onNightSwitch() {
    }

    private void initProgressBar() {
        if (mProgressBar != null) {
            int resId = ThemeUtils.getResourceId(requireActivity(), R.attr.colorAccent);
            mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(requireActivity(), resId), PorterDuff.Mode.SRC_ATOP);
        }
    }

    protected void initView() {
    }

    protected void initData() {
    }

    protected BasePresenter initPresenter() {
        return null;
    }

    protected abstract @LayoutRes
    int getLayoutRes();

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
