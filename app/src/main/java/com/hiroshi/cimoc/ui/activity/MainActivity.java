package com.hiroshi.cimoc.ui.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.fragment.AboutFragment;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.CimocFragment;
import com.hiroshi.cimoc.ui.fragment.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.SettingsFragment;
import com.hiroshi.cimoc.ui.fragment.SourceFragment;
import com.hiroshi.cimoc.utils.DialogFactory;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity {

    private static final int FRAGMENT_NUM = 7;

    @BindView(R.id.main_drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_navigation_view) NavigationView mNavigationView;
    @BindView(R.id.main_fragment_container) FrameLayout mFrameLayout;

    private AlertDialog mProgressDialog;
    private long mExitTime = 0;

    private int mCheckItem;
    private SparseArray<BaseFragment> mFragmentArray;
    private FragmentManager mFragmentManager;
    private BaseFragment mCurrentFragment;

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
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    @Override
    protected void initView() {
        mProgressDialog = DialogFactory.buildCancelableFalseDialog(this);
        mProgressDialog.setMessage(getString(R.string.dialog_doing));
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        mCurrentFragment = mFragmentArray.get(mCheckItem);
                        mFragmentManager.beginTransaction().show(mCurrentFragment).commit();
                        mProgressBar.setVisibility(View.GONE);
                        mFrameLayout.setVisibility(View.VISIBLE);
                    }
                };
        drawerToggle.syncState();
        mDrawerLayout.setDrawerListener(drawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == mCheckItem) {
                    return false;
                }
                mCheckItem = item.getItemId();
                mNavigationView.setCheckedItem(mCheckItem);
                mFrameLayout.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mFragmentManager.beginTransaction().hide(mCurrentFragment).commit();
                mToolbar.setTitle(item.getTitle().toString());
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        initFragment();
        mProgressBar.setVisibility(View.GONE);
    }

    private void initFragment() {
        initCheckItem();

        mFragmentArray = new SparseArray<>(FRAGMENT_NUM);
        mFragmentArray.put(R.id.drawer_cimoc, new CimocFragment());
        mFragmentArray.put(R.id.drawer_favorite, new FavoriteFragment());
        mFragmentArray.put(R.id.drawer_history, new HistoryFragment());
        mFragmentArray.put(R.id.drawer_source, new SourceFragment());
        mFragmentArray.put(R.id.drawer_download, new DownloadFragment());
        mFragmentArray.put(R.id.drawer_settings, new SettingsFragment());
        mFragmentArray.put(R.id.drawer_about, new AboutFragment());

        mCurrentFragment = mFragmentArray.get(mCheckItem);
        mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (int i = 0; i != FRAGMENT_NUM; ++i) {
            BaseFragment fragment = mFragmentArray.valueAt(i);
            transaction.add(R.id.main_fragment_container, fragment).hide(fragment);
        }
        transaction.show(mCurrentFragment).commit();
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
        }
        mNavigationView.setCheckedItem(mCheckItem);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected View getLayoutView() {
        return mDrawerLayout;
    }

    @Override
    protected String getDefaultTitle() {
        int home = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_CIMOC);
        return getResources().getStringArray(R.array.home_items)[home];
    }

    public void restart() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public void showProgressDialog() {
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

}
