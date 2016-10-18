package com.hiroshi.cimoc.ui.fragment;

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
import com.hiroshi.cimoc.presenter.ComicPresenter;
import com.hiroshi.cimoc.ui.adapter.TabPagerAdapter;
import com.hiroshi.cimoc.ui.fragment.classical.grid.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.classical.grid.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.classical.grid.GridFragment;
import com.hiroshi.cimoc.ui.fragment.classical.grid.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.view.ComicView;
import com.hiroshi.cimoc.utils.HintUtils;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicFragment extends BaseFragment implements ComicView, ChoiceDialogFragment.ChoiceDialogListener {

    public static final int TYPE_HISTORY = 1;
    public static final int TYPE_FAVORITE = 2;
    public static final int TYPE_DOWNLOAD = 3;

    @BindView(R.id.comic_tab_layout) TabLayout mTabLayout;
    @BindView(R.id.comic_view_pager) ViewPager mViewPager;

    private ComicPresenter mPresenter;
    private TabPagerAdapter mTabAdapter;
    private List<Tag> mTagList;
    private int mFilterChoice;

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
        mTabAdapter = new TabPagerAdapter(getFragmentManager(),
                new GridFragment[]{ new HistoryFragment(), new FavoriteFragment(), new DownloadFragment() },
                new String[]{ getString(R.string.comic_tab_history), getString(R.string.comic_tab_favorite), getString(R.string.comic_tab_download) });
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mTabAdapter);
        mViewPager.setCurrentItem(1);
        mTabLayout.setupWithViewPager(mViewPager);
        hideProgressBar();
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
                if (mTagList == null) {
                    showProgressDialog();
                    mPresenter.load();
                } else {
                    showTagList();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTagList() {
        String[] item = new String[mTagList.size() + 3];
        item[0] = "全部漫画";
        item[1] = "连载中";
        item[2] = "已完结";
        for (int i = 0; i != mTagList.size(); ++i) {
            item[i + 3] = mTagList.get(i).getTitle();
        }
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.comic_filter, item, mFilterChoice, -1);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onChoicePositiveClick(int type, int choice, String value) {
        mFilterChoice = choice;
        switch (mFilterChoice) {
            case 0:
                mPresenter.filter(TagManager.TAG_ALL, 0);
                break;
            case 1:
                mPresenter.filter(TagManager.TAG_CONTINUE, 0);
                break;
            case 2:
                mPresenter.filter(TagManager.TAG_END, 0);
                break;
            default:
                mPresenter.filter(TagManager.TAG_NORMAL, mTagList.get(mFilterChoice - 3).getId());
                break;
        }
    }

    @Override
    public void showSnackbar(String msg) {
        HintUtils.showSnackbar(mTabAdapter.getItem(mViewPager.getCurrentItem()).getView(), msg);
    }

    @Override
    public void onTagInsert(Tag tag) {
        if (mTagList != null) {
            mTagList.add(tag);
        }
    }

    @Override
    public void onTagDelete(Tag tag) {
        if (mTagList != null) {
            int index = mTagList.indexOf(tag);
            mTagList.remove(tag);
            if (index != -1 && index == mFilterChoice - 3) {
                mFilterChoice = 0;
                mPresenter.filter(TagManager.TAG_ALL, 0);
            }
        }
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        hideProgressDialog();
        mFilterChoice = 0;
        mTagList = list;
        showTagList();
    }

    @Override
    public void onTagLoadFail() {
        showSnackbar(R.string.comic_load_filter_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mTabLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), primary));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_comic;
    }

}
