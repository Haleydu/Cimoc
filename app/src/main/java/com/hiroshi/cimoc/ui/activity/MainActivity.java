package com.hiroshi.cimoc.ui.activity;

import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.MainPresenter;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.main_drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_navigation_view) NavigationView mNavigationView;
    @BindView(R.id.main_fragment_container) FrameLayout mFrameLayout;
    @BindView(R.id.main_progress_bar) ProgressBar mProgressBar;

    private MainPresenter mPresenter;

    @Override
    protected void initPresenter() {
        mPresenter = new MainPresenter(this);
    }

    @Override
    protected void initView() {
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        mPresenter.transFragment();
                    }
                };
        drawerToggle.syncState();
        mDrawerLayout.setDrawerListener(drawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                return mPresenter.onNavigationItemSelected(item);
            }
        });
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    protected String getDefaultTitle() {
        return "Cimoc";
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onBackPressed() {
        mPresenter.onBackPressed();
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void setToolbarTitle(String title) {
        mToolbar.setTitle(title);
    }

    public void setCheckedItem(int id) {
        mNavigationView.setCheckedItem(id);
    }

    public void showProgressBar() {
        mFrameLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mFrameLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    public void showSnackbar(String msg) {
        Snackbar.make(mDrawerLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

}
