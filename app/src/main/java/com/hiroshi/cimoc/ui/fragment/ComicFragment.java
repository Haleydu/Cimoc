package com.hiroshi.cimoc.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ComicPresenter;
import com.hiroshi.cimoc.ui.activity.PartFavoriteActivity;
import com.hiroshi.cimoc.ui.activity.SearchActivity;
import com.hiroshi.cimoc.ui.adapter.TabPagerAdapter;
import com.hiroshi.cimoc.ui.fragment.coordinator.grid.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.coordinator.grid.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.coordinator.grid.GridFragment;
import com.hiroshi.cimoc.ui.fragment.coordinator.grid.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.ItemDialogFragment;
import com.hiroshi.cimoc.ui.view.ComicView;
import com.hiroshi.cimoc.ui.view.ThemeView;
import com.hiroshi.cimoc.utils.HintUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicFragment extends BaseFragment implements ComicView {

    private static final int DIALOG_REQUEST_FILTER = 0;

    @BindView(R.id.comic_tab_layout) TabLayout mTabLayout;
    @BindView(R.id.comic_view_pager) ViewPager mViewPager;

    private ComicPresenter mPresenter;
    private TabPagerAdapter mTabAdapter;
    private List<Tag> mTagList;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new ComicPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_history));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_favorite));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_download));
        mTabAdapter = new TabPagerAdapter(getFragmentManager(),
                new GridFragment[]{ new HistoryFragment(), new FavoriteFragment(), new DownloadFragment() },
                new String[]{ getString(R.string.comic_tab_history), getString(R.string.comic_tab_favorite), getString(R.string.comic_tab_download) });
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setCurrentItem(1);
        mTabLayout.setupWithViewPager(mViewPager);
        mTagList = new ArrayList<>();
        hideProgressBar();
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
                showProgressDialog();
                mTagList.clear();
                mPresenter.load();
                break;
            case R.id.comic_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_FILTER:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                Intent intent = PartFavoriteActivity.createIntent(getActivity(), mTagList.get(index).getId(), mTagList.get(index).getTitle());
                startActivity(intent);
                break;
        }
    }

    @Override
    public void showSnackbar(String msg) {
        HintUtils.showSnackbar(mTabAdapter.getItem(mViewPager.getCurrentItem()).getView(), msg);
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        hideProgressDialog();
        mTagList.add(new Tag(TagManager.TAG_FINISH, getString(R.string.comic_filter_finish)));
        mTagList.add(new Tag(TagManager.TAG_CONTINUE, getString(R.string.comic_filter_continue)));
        mTagList.addAll(list);
        int size = mTagList.size();
        String[] item = new String[size];
        for (int i = 0; i < size; ++i) {
            item[i] = mTagList.get(i).getTitle();
        }
        ItemDialogFragment fragment = ItemDialogFragment.newInstance(R.string.comic_tag_select, item, DIALOG_REQUEST_FILTER);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.comic_load_tag_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mTabLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), primary));
        for (int i = 0; i < 3; ++i) {
            ((ThemeView) mTabAdapter.getItem(i)).onThemeChange(primary, accent);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_comic;
    }

}
