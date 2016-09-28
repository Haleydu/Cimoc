package com.hiroshi.cimoc.ui.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.MainPresenter;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.CimocFragment;
import com.hiroshi.cimoc.ui.fragment.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.SourceFragment;
import com.hiroshi.cimoc.ui.view.MainView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity implements MainView, NavigationView.OnNavigationItemSelectedListener {

    private static final int FRAGMENT_NUM = 5;

    @BindView(R.id.main_drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_navigation_view) NavigationView mNavigationView;
    @BindView(R.id.main_fragment_container) FrameLayout mFrameLayout;
    TextView mLastText;

    private MainPresenter mPresenter;
    private long mExitTime = 0;
    private int mLastSource = -1;
    private String mLastCid;

    private int mCheckItem;
    private SparseArray<BaseFragment> mFragmentArray;
    private FragmentManager mFragmentManager;
    private BaseFragment mCurrentFragment;

    @Override
    protected void initPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        mCurrentFragment = mFragmentArray.get(mCheckItem);
                        mFragmentManager.beginTransaction().show(mCurrentFragment).commit();
                        mProgressBar.setVisibility(View.GONE);
                    }
                };
        drawerToggle.syncState();
        mDrawerLayout.setDrawerListener(drawerToggle);
        boolean night = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_NIGHT, false);
        mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
        mNavigationView.setNavigationItemSelectedListener(this);
        initHeader(mNavigationView.getHeaderView(0));
        initFragment();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    private void initHeader(View header) {
        mLastText = ButterKnife.findById(header, R.id.drawer_last_read_text);
        ButterKnife.findById(header, R.id.drawer_last_read_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastSource != -1 && mLastCid != null) {
                    Intent intent = DetailActivity.createIntent(MainActivity.this, null, mLastSource, mLastCid);
                    startActivity(intent);
                }
            }
        });
    }

    private void initFragment() {
        initCheckItem();

        mFragmentArray = new SparseArray<>(FRAGMENT_NUM);
        mFragmentArray.put(R.id.drawer_cimoc, new CimocFragment());
        mFragmentArray.put(R.id.drawer_favorite, new FavoriteFragment());
        mFragmentArray.put(R.id.drawer_history, new HistoryFragment());
        mFragmentArray.put(R.id.drawer_download, new DownloadFragment());
        mFragmentArray.put(R.id.drawer_source, new SourceFragment());

        mCurrentFragment = mFragmentArray.get(mCheckItem);
        mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (int i = 0; i != FRAGMENT_NUM; ++i) {
            BaseFragment fragment = mFragmentArray.valueAt(i);
            transaction.add(R.id.main_fragment_container, fragment);
            if (fragment != mCurrentFragment) {
                transaction.hide(fragment);
            }
        }
        transaction.commit();
    }

    private void initCheckItem() {
        int home = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_CIMOC);
        switch (home) {
            default:
            case PreferenceManager.HOME_CIMOC:
                mCheckItem = R.id.drawer_cimoc;
                break;
            case PreferenceManager.HOME_FAVORITE:
                mCheckItem = R.id.drawer_favorite;
                break;
            case PreferenceManager.HOME_HISTORY:
                mCheckItem = R.id.drawer_history;
                break;
            case PreferenceManager.HOME_DOWNLOAD:
                mCheckItem = R.id.drawer_download;
                break;
        }
        mNavigationView.setCheckedItem(mCheckItem);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {}

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (System.currentTimeMillis() - mExitTime > 2000) {
            showSnackbar(R.string.main_double_click);
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId != mCheckItem) {
            if (mFragmentArray.get(itemId) != null) {
                mCheckItem = itemId;
                mProgressBar.setVisibility(View.VISIBLE);
                mFragmentManager.beginTransaction().hide(mCurrentFragment).commit();
                mToolbar.setTitle(item.getTitle().toString());
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                switch (itemId) {
                    case R.id.drawer_night:
                        boolean night = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_NIGHT, false);
                        CimocApplication.getPreferences().putBoolean(PreferenceManager.PREF_NIGHT, !night);
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                        break;
                    case R.id.drawer_settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                    case R.id.drawer_about:
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        break;
                }
            }
        }
        return true;
    }

    @Override
    public void onLastLoadSuccess(int source, String cid, String title) {
        onLastChange(source, cid, title);
    }

    @Override
    public void onLastLoadFail() {
        showSnackbar(R.string.main_last_read_fail);
    }

    @Override
    public void onLastChange(int source, String cid, String title) {
        mLastSource = source;
        mLastCid = cid;
        mLastText.setText(title);
    }

    @Override
    protected String getDefaultTitle() {
        int home = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_CIMOC);
        return getResources().getStringArray(R.array.home_items)[home];
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected View getLayoutView() {
        return mDrawerLayout;
    }

}
