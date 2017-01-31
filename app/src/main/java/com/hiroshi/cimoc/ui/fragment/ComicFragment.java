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
import com.hiroshi.cimoc.manager.TagManager;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ComicPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.activity.PartFavoriteActivity;
import com.hiroshi.cimoc.ui.activity.SearchActivity;
import com.hiroshi.cimoc.ui.adapter.TabPagerAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.ItemDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.grid.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.grid.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.grid.GridFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.grid.HistoryFragment;
import com.hiroshi.cimoc.ui.view.ComicView;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicFragment extends BaseFragment implements ComicView {

    private static final int DIALOG_REQUEST_FILTER = 0;
    private static final int DIALOG_REQUEST_CLEAR = 1;
    private static final int DIALOG_REQUEST_UPDATE = 2;
    private static final int DIALOG_REQUEST_SWITCH = 3;

    @BindView(R.id.comic_tab_layout) TabLayout mTabLayout;
    @BindView(R.id.comic_view_pager) ViewPager mViewPager;

    private ComicPresenter mPresenter;
    private TabPagerAdapter mTabAdapter;
    private List<Tag> mTagList;

    private HistoryFragment mHistoryFragment;
    private FavoriteFragment mFavoriteFragment;
    private DownloadFragment mDownloadFragment;

    private boolean isDownload;
    private MenuItem mDownloadItem;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new ComicPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        mHistoryFragment = new HistoryFragment();
        mFavoriteFragment = new FavoriteFragment();
        mDownloadFragment = new DownloadFragment();
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_history));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_favorite));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.comic_tab_download));
        mTabAdapter = new TabPagerAdapter(getFragmentManager(),
                new GridFragment[]{ mHistoryFragment, mFavoriteFragment, mDownloadFragment },
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
        inflater.inflate(R.menu.menu_comic, menu);
        mDownloadItem = menu.findItem(R.id.comic_switch_download);
        if (ServiceUtils.isServiceRunning(getActivity(), DownloadService.class)) {
            onDownloadStart();
        } else {
            onDownloadStop();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MessageDialogFragment fragment;
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
            case R.id.comic_clear_history:
                fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                        R.string.history_clear_confirm, true, DIALOG_REQUEST_CLEAR);
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), null);
                break;
            case R.id.comic_check_update:
                fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                        R.string.favorite_check_update_confirm, true, DIALOG_REQUEST_UPDATE);
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), null);
                break;
            case R.id.comic_switch_download:
                fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                        R.string.download_action_confirm, true, DIALOG_REQUEST_SWITCH);
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), null);
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
            case DIALOG_REQUEST_CLEAR:
                mHistoryFragment.clearHistory();
                break;
            case DIALOG_REQUEST_UPDATE:
                mFavoriteFragment.checkUpdate();
                break;
            case DIALOG_REQUEST_SWITCH:
                if (isDownload) {
                    ServiceUtils.stopService(getActivity(), DownloadService.class);
                    HintUtils.showToast(getActivity(), R.string.download_stop_success);
                } else {
                    showProgressDialog();
                    mPresenter.loadTask();
                }
                break;
        }
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
        HintUtils.showToast(getActivity(), R.string.comic_load_tag_fail);
    }

    @Override
    public void onDownloadStart() {
        if (!isDownload) {
            isDownload = true;
            mDownloadItem.setTitle(R.string.comic_stop_download);
        }
    }

    @Override
    public void onDownloadStop() {
        if (isDownload) {
            isDownload = false;
            mDownloadItem.setTitle(R.string.comic_start_download);
        }
    }

    @Override
    public void onTaskLoadFail() {
        hideProgressDialog();
        HintUtils.showToast(getActivity(), R.string.download_task_fail);
    }

    @Override
    public void onTaskLoadSuccess(ArrayList<Task> list) {
        if (list.isEmpty()) {
            HintUtils.showToast(getActivity(), R.string.download_task_empty);
        } else {
            Intent intent = DownloadService.createIntent(getActivity(), list);
            getActivity().startService(intent);
            HintUtils.showToast(getActivity(), R.string.download_start_success);
        }
        hideProgressDialog();
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
