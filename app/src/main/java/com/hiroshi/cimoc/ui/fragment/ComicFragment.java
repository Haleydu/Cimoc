package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
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
import com.hiroshi.cimoc.ui.view.ComicView;
import com.hiroshi.cimoc.utils.DialogUtils;
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
    private TabPagerAdapter mTabAdapter;
    private List<Tag> mTagList;
    private int mFilterChoice;
    private int mTempChoice;

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
        String[] title = new String[mTagList.size() + 3];
        title[0] = "全部漫画";
        title[1] = "连载中";
        title[2] = "已完结";
        for (int i = 0; i != mTagList.size(); ++i) {
            title[i + 3] = mTagList.get(i).getTitle();
        }
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.comic_filter, title, mFilterChoice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTempChoice = which;
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFilterChoice = mTempChoice;
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
                }).show();
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
    public void onTagInsert(Tag tag) {
        mTagList.add(tag);
    }

    @Override
    public void onTagDelete(Tag tag) {
        int index = mTagList.indexOf(tag);
        mTagList.remove(tag);
        if (index != -1 && index == mFilterChoice - 3) {
            mFilterChoice = 0;
            mPresenter.filter(TagManager.TAG_ALL, 0);
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
    protected int getLayoutRes() {
        return R.layout.fragment_comic;
    }

}
