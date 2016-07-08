package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.custom_toolbar) protected Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutView());
        ButterKnife.bind(this);
        initToolbar();
        initPresenter();
        if (getPresenter() != null) {
            getPresenter().register();
        }
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getPresenter() != null) {
            getPresenter().unregister();
        }
    }

    protected void initToolbar() {
        mToolbar.setTitle(getDefaultTitle());
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract @LayoutRes int getLayoutView();

    protected abstract String getDefaultTitle();

    protected abstract BasePresenter getPresenter();

    protected abstract void initPresenter();

    protected abstract void initView();

}
