package com.hiroshi.cimoc.ui.adapter;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.hiroshi.cimoc.ui.fragment.BaseFragment;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {

    private BaseFragment[] fragment;
    private String[] title;

    public TabPagerAdapter(FragmentManager manager, BaseFragment[] fragment, String[] title) {
        super(manager);
        this.fragment = fragment;
        this.title = title;
    }

    @Override
    public Fragment getItem(int position) {
        return fragment[position];
    }

    @Override
    public int getCount() {
        return fragment.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }

}
