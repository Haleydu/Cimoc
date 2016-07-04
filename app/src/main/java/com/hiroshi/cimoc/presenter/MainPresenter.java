package com.hiroshi.cimoc.presenter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.MenuItem;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.activity.MainActivity;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.MainFragment;
import com.hiroshi.cimoc.ui.fragment.PlugFragment;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainPresenter extends BasePresenter {

    private MainActivity mMainActivity;
    private long mExitTime;

    private int mCheckedItem;
    private FragmentManager mFragmentManager;
    private MainFragment mMainFragment;
    private ComicFragment mComicFragment;
    private HistoryFragment mHistoryFragment;
    private PlugFragment mPlugFragment;
    private Fragment mCurrentFragment;
    
    public MainPresenter(MainActivity activity) {
        mMainActivity = activity;
        mExitTime = 0;
        initFragment();
    }

    private void initFragment() {
        mFragmentManager = mMainActivity.getFragmentManager();
        mMainFragment = new MainFragment();
        mFragmentManager.beginTransaction().add(R.id.container_fragment, mMainFragment).commit();
        mCurrentFragment = mMainFragment;
        mCheckedItem = R.id.drawer_main;
        mMainActivity.setCheckedItem(mCheckedItem);
    }

    public void onBackPressed() {
        if (mMainActivity.isDrawerOpen()) {
            mMainActivity.closeDrawer();
        } else if (System.currentTimeMillis() - mExitTime > 2000) {
            mMainActivity.showSnackbar("再按一次退出程序");
            mExitTime = System.currentTimeMillis();
        } else {
            mMainActivity.finish();
        }
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mMainActivity.closeDrawer();
        if (menuItem.getItemId() == mCheckedItem) {
            return false;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction().hide(mCurrentFragment);
        mCheckedItem = menuItem.getItemId();
        switch (mCheckedItem) {
            case R.id.drawer_main:
                if (mMainFragment == null) {
                    mMainFragment = new MainFragment();
                    transaction.add(R.id.container_fragment, mMainFragment);
                }
                mCurrentFragment = mMainFragment;
                break;
            case R.id.drawer_comic:
                if (mComicFragment == null) {
                    mComicFragment = new ComicFragment();
                    transaction.add(R.id.container_fragment, mComicFragment);
                }
                mCurrentFragment = mComicFragment;
                break;
        }
        transaction.show(mCurrentFragment).commit();
        mMainActivity.setToolbarTitle(menuItem.getTitle().toString());
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(EventMessage msg) {

    }

}
