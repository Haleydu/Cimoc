package com.hiroshi.cimoc.presenter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.view.MenuItem;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.activity.MainActivity;
import com.hiroshi.cimoc.ui.fragment.AboutFragment;
import com.hiroshi.cimoc.ui.fragment.CimocFragment;
import com.hiroshi.cimoc.ui.fragment.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.SettingsFragment;
import com.hiroshi.cimoc.ui.fragment.SourceFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainPresenter extends BasePresenter {

    private MainActivity mMainActivity;

    private int mCheckedItem;
    private FragmentManager mFragmentManager;
    private CimocFragment mCimocFragment;
    private FavoriteFragment mFavoriteFragment;
    private HistoryFragment mHistoryFragment;
    private SourceFragment mSourceFragment;
    private SettingsFragment mSettingsFragment;
    private AboutFragment mAboutFragment;
    private Fragment mCurrentFragment;
    
    public MainPresenter(MainActivity activity, int item) {
        mMainActivity = activity;
        mCheckedItem = item;
        initFragment();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        switch (mCheckedItem) {
            case R.id.drawer_cimoc:
                mCurrentFragment = mCimocFragment;
                break;
            case R.id.drawer_favorite:
                mCurrentFragment = mFavoriteFragment;
                break;
            case R.id.drawer_history:
                mCurrentFragment = mHistoryFragment;
                break;
        }
        mFragmentManager.beginTransaction()
                .add(R.id.main_fragment_container, mCimocFragment)
                .add(R.id.main_fragment_container, mFavoriteFragment)
                .add(R.id.main_fragment_container, mHistoryFragment)
                .add(R.id.main_fragment_container, mSourceFragment)
                .add(R.id.main_fragment_container, mSettingsFragment)
                .add(R.id.main_fragment_container, mAboutFragment)
                .hide(mCimocFragment)
                .hide(mFavoriteFragment)
                .hide(mHistoryFragment)
                .hide(mSourceFragment)
                .hide(mSettingsFragment)
                .hide(mAboutFragment)
                .show(mCurrentFragment)
                .commit();
        mMainActivity.setCheckedItem(mCheckedItem);
    }

    private void initFragment() {
        mFragmentManager = mMainActivity.getFragmentManager();
        mCimocFragment = new CimocFragment();
        mFavoriteFragment = new FavoriteFragment();
        mHistoryFragment = new HistoryFragment();
        mSourceFragment = new SourceFragment();
        mSettingsFragment = new SettingsFragment();
        mAboutFragment = new AboutFragment();
    }

    public void transFragment() {
        switch (mCheckedItem) {
            default:
            case R.id.drawer_cimoc:
                mCurrentFragment = mCimocFragment;
                break;
            case R.id.drawer_favorite:
                mCurrentFragment = mFavoriteFragment;
                break;
            case R.id.drawer_history:
                mCurrentFragment = mHistoryFragment;
                break;
            case R.id.drawer_source:
                mCurrentFragment = mSourceFragment;
                break;
            case R.id.drawer_settings:
                mCurrentFragment = mSettingsFragment;
                break;
            case R.id.drawer_about:
                mCurrentFragment = mAboutFragment;
                break;
        }
        mFragmentManager.beginTransaction().show(mCurrentFragment).commit();
        mMainActivity.hideProgressBar();
    }

    public boolean switchItem(MenuItem menuItem) {
        if (menuItem.getItemId() == mCheckedItem) {
            mMainActivity.closeDrawer();
            return false;
        }
        mCheckedItem = menuItem.getItemId();
        mMainActivity.showProgressBar();
        mFragmentManager.beginTransaction().hide(mCurrentFragment).commit();
        mMainActivity.setToolbarTitle(menuItem.getTitle().toString());
        mMainActivity.setCheckedItem(mCheckedItem);
        mMainActivity.closeDrawer();
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
    }

}
