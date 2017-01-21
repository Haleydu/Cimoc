package com.hiroshi.cimoc.ui.activity.settings;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.activity.BackActivity;
import com.hiroshi.cimoc.ui.adapter.TabPagerAdapter;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.config.PageConfigFragment;
import com.hiroshi.cimoc.ui.fragment.config.StreamConfigFragment;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/14.
 */

public class ReaderConfigActivity extends BackActivity {

    @BindView(R.id.reader_config_tab_layout) TabLayout mTabLayout;
    @BindView(R.id.reader_config_view_pager) ViewPager mViewPager;

    @Override
    protected void initView() {
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.reader_config_page));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.reader_config_stream));
        TabPagerAdapter tabAdapter = new TabPagerAdapter(getFragmentManager(),
                new BaseFragment[]{ new PageConfigFragment(), new StreamConfigFragment() },
                new String[]{ getString(R.string.reader_config_page), getString(R.string.reader_config_stream)});
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(tabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.reader_config_title);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_reader_config;
    }

}
