package com.haleydu.cimoc.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.component.ThemeResponsive;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.manager.TagManager;
import com.haleydu.cimoc.model.Tag;
import com.haleydu.cimoc.presenter.BasePresenter;
import com.haleydu.cimoc.presenter.ComicPresenter;
import com.haleydu.cimoc.ui.activity.PartFavoriteActivity;
import com.haleydu.cimoc.ui.activity.SearchActivity;
import com.haleydu.cimoc.ui.adapter.TabPagerAdapter;
import com.haleydu.cimoc.ui.fragment.dialog.ItemDialogFragment;
import com.haleydu.cimoc.ui.fragment.recyclerview.grid.DownloadFragment;
import com.haleydu.cimoc.ui.fragment.recyclerview.grid.FavoriteFragment;
import com.haleydu.cimoc.ui.fragment.recyclerview.grid.GridFragment;
import com.haleydu.cimoc.ui.fragment.recyclerview.grid.HistoryFragment;
import com.haleydu.cimoc.ui.fragment.recyclerview.grid.LocalFragment;
import com.haleydu.cimoc.ui.view.ComicView;
import com.haleydu.cimoc.utils.HintUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicFragment extends BaseFragment implements ComicView {

    private static final int DIALOG_REQUEST_FILTER = 0;

    @BindView(R.id.comic_tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.comic_view_pager)
    ViewPager mViewPager;

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
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_local));
        mTabAdapter = new TabPagerAdapter(requireActivity().getSupportFragmentManager(),
                new GridFragment[]{new HistoryFragment(), new FavoriteFragment(), new DownloadFragment(), new LocalFragment()},
                new String[]{getString(R.string.comic_tab_history), getString(R.string.comic_tab_favorite), getString(R.string.comic_tab_download), getString(R.string.comic_tab_local)});
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mTabAdapter);
        int home = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_FAVORITE);
        switch (home) {
            default:
            case PreferenceManager.HOME_FAVORITE:
                mViewPager.setCurrentItem(1);
                break;
            case PreferenceManager.HOME_HISTORY:
                mViewPager.setCurrentItem(0);
                break;
            case PreferenceManager.HOME_DOWNLOAD:
                mViewPager.setCurrentItem(2);
                break;
            case PreferenceManager.HOME_LOCAL:
                mViewPager.setCurrentItem(3);
                break;
        }
        mTabLayout.setupWithViewPager(mViewPager);
        mTagList = new ArrayList<>();
        hideProgressBar();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_comic, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comic_filter:
                showProgressDialog();
                mTagList.clear();
                mPresenter.loadTag();
                break;
            case R.id.comic_search:
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.comic_bbs:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.home_page_gitter_url)));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                }
                break;
            case R.id.comic_cancel_highlight:
                ((FavoriteFragment) mTabAdapter.getItem(1)).cancelAllHighlight();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (int i = 0; i < mTabAdapter.getCount(); ++i) {
            mTabAdapter.getItem(i).onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_FILTER:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                Intent intent = PartFavoriteActivity.createIntent(getActivity(),
                        mTagList.get(index).getId(), mTagList.get(index).getTitle());
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        hideProgressDialog();
        mTagList.add(new Tag(TagManager.TAG_FINISH, getString(R.string.comic_status_finish)));
        mTagList.add(new Tag(TagManager.TAG_CONTINUE, getString(R.string.comic_status_continue)));
        mTagList.addAll(list);
        int size = mTagList.size();
        String[] item = new String[size];
        for (int i = 0; i < size; ++i) {
            item[i] = mTagList.get(i).getTitle();
        }
        ItemDialogFragment fragment = ItemDialogFragment.newInstance(R.string.comic_tag_select, item, DIALOG_REQUEST_FILTER);
        fragment.setTargetFragment(this, 0);
        fragment.show(requireActivity().getSupportFragmentManager(), null);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressDialog();
        HintUtils.showToast(getActivity(), R.string.comic_load_tag_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mTabLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), primary));
        for (int i = 0; i < mTabAdapter.getCount(); ++i) {
            ((ThemeResponsive) mTabAdapter.getItem(i)).onThemeChange(primary, accent);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_comic;
    }

}
