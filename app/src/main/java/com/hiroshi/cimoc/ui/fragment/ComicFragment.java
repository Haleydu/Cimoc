package com.hiroshi.cimoc.ui.fragment;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.ComicPresenter;
import com.hiroshi.cimoc.ui.adapter.ComicTabAdapter;
import com.hiroshi.cimoc.ui.fragment.classical.grid.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.classical.grid.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.classical.grid.GridFragment;
import com.hiroshi.cimoc.ui.fragment.classical.grid.HistoryFragment;
import com.hiroshi.cimoc.ui.view.ComicView;
import com.hiroshi.cimoc.utils.HintUtils;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicFragment extends BaseFragment implements ComicView {

    @BindView(R.id.comic_tab_layout) TabLayout mTabLayout;
    @BindView(R.id.comic_view_pager) ViewPager mViewPager;

    private ComicPresenter mPresenter;
    private ComicTabAdapter mTabAdapter;
    private long id;

    @Override
    protected void initPresenter() {
        mPresenter = new ComicPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_history));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_favorite));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_download));
        mTabAdapter = new ComicTabAdapter(getFragmentManager(),
                new GridFragment[]{ new HistoryFragment(), new FavoriteFragment(), new DownloadFragment() },
                new String[]{ getString(R.string.comic_tab_history), getString(R.string.comic_tab_favorite), getString(R.string.comic_tab_download) });
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setCurrentItem(1);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.comic_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comic_filter:
                mPresenter.load();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSnackbar(String msg) {
        HintUtils.showSnackbar(mTabAdapter.getItem(mViewPager.getCurrentItem()).getView(), msg);
    }

    @Override
    public void onFilterFail() {
        showSnackbar(R.string.comic_filter_fail);
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {

    }

    @Override
    public void onTagLoadFail() {
        showSnackbar(R.string.comic_load_filter_fail);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_comic;
    }

}
