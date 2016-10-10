package com.hiroshi.cimoc.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.MainPresenter;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;
import com.hiroshi.cimoc.ui.fragment.DownloadFragment;
import com.hiroshi.cimoc.ui.fragment.FavoriteFragment;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;
import com.hiroshi.cimoc.ui.fragment.SourceFragment;
import com.hiroshi.cimoc.ui.fragment.TagFragment;
import com.hiroshi.cimoc.ui.view.MainView;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity implements MainView, NavigationView.OnNavigationItemSelectedListener,
        SearchView.OnQueryTextListener {

    private static final int FRAGMENT_NUM = 6;

    @BindView(R.id.main_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_navigation_view) NavigationView mNavigationView;
    @BindView(R.id.main_fragment_container) FrameLayout mFrameLayout;
    private TextView mLastText;
    private SimpleDraweeView mDraweeView;

    private MainPresenter mPresenter;
    private ActionBarDrawerToggle mDrawerToggle;
    private long mExitTime = 0;
    private int mLastSource = -1;
    private String mLastCid;

    private int mCheckItem;
    private SparseArray<BaseFragment> mFragmentArray;
    private BaseFragment mCurrentFragment;

    private List<Source> mSourceList;
    private Set<String> mFilterSet;

    private ControllerBuilderProvider mProvider;
    private boolean night;

    @Override
    protected void initPresenter() {
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        initDrawerToggle();
        initNavigation();
        initFragment();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mPresenter.loadLast();
        mPresenter.loadSource();
    }

    private void initDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (refreshCurrentFragment()) {
                    getFragmentManager().beginTransaction().show(mCurrentFragment).commit();
                } else {
                    getFragmentManager().beginTransaction().add(R.id.main_fragment_container, mCurrentFragment).commit();
                }
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        hideProgressBar();
    }

    private void initNavigation() {
        night = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_NIGHT, false);
        mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
        mNavigationView.setNavigationItemSelectedListener(this);
        View header = mNavigationView.getHeaderView(0);
        mLastText = ButterKnife.findById(header, R.id.drawer_last_read_text);
        mDraweeView = ButterKnife.findById(header, R.id.drawer_last_cover);
        mProvider = new ControllerBuilderProvider(this);
        ButterKnife.findById(header, R.id.drawer_last_read_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastSource != -1 && mLastCid != null) {
                    Intent intent = DetailActivity.createIntent(MainActivity.this, null, mLastSource, mLastCid);
                    startActivity(intent);
                }
            }
        });
    }

    private void initFragment() {
        int home = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_FAVORITE);
        switch (home) {
            default:
            case PreferenceManager.HOME_FAVORITE:
                mCheckItem = R.id.drawer_favorite;
                break;
            case PreferenceManager.HOME_HISTORY:
                mCheckItem = R.id.drawer_history;
                break;
            case PreferenceManager.HOME_DOWNLOAD:
                mCheckItem = R.id.drawer_download;
                break;
            case PreferenceManager.HOME_SOURCE:
                mCheckItem = R.id.drawer_source;
                break;
            case PreferenceManager.HOME_TAG:
                mCheckItem = R.id.drawer_tag;
                break;
        }
        mNavigationView.setCheckedItem(mCheckItem);
        mFragmentArray = new SparseArray<>(FRAGMENT_NUM);
        refreshCurrentFragment();
        getFragmentManager().beginTransaction().add(R.id.main_fragment_container, mCurrentFragment).commit();
    }

    private boolean refreshCurrentFragment() {
        mCurrentFragment = mFragmentArray.get(mCheckItem);
        if (mCurrentFragment == null) {
            switch (mCheckItem) {
                case R.id.drawer_favorite:
                    mCurrentFragment = new FavoriteFragment();
                    break;
                case R.id.drawer_history:
                    mCurrentFragment = new HistoryFragment();
                    break;
                case R.id.drawer_download:
                    mCurrentFragment = new DownloadFragment();
                    break;
                case R.id.drawer_source:
                    mCurrentFragment = new SourceFragment();
                    break;
                case R.id.drawer_tag:
                    mCurrentFragment = new TagFragment();
                    break;
            }
            mFragmentArray.put(mCheckItem, mCurrentFragment);
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
        if (mProvider != null) {
            mProvider.clear();
            mProvider = null;
        }
        mFragmentArray = null;
        mCurrentFragment = null;
        mDrawerToggle = null;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {}

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (System.currentTimeMillis() - mExitTime > 2000) {
            showSnackbar(R.string.main_double_click);
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.main_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("请输入关键字");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (StringUtils.isEmpty(query)) {
            showSnackbar(getString(R.string.main_search_keyword_empty));
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            for (Source source : mSourceList) {
                if (mFilterSet.contains(source.getTitle())) {
                    list.add(source.getType());
                }
            }
            if (list.isEmpty()) {
                showSnackbar(R.string.main_source_none);
            } else {
                startActivity(ResultActivity.createIntent(this, query, list));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_source:
                int size = mSourceList.size();
                boolean[] select = new boolean[size];
                String[] title = new String[size];
                for (int i = 0; i != size; ++i) {
                    title[i] = mSourceList.get(i).getTitle();
                    select[i] = mFilterSet.contains(title[i]);
                }
                DialogUtils.buildMultiChoiceDialog(this, R.string.main_source_select, title, select,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mFilterSet.add(mSourceList.get(which).getTitle());
                                } else {
                                    mFilterSet.remove(mSourceList.get(which).getTitle());
                                }
                            }
                        }, -1, null, null).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId != mCheckItem) {
            switch (itemId) {
                case R.id.drawer_favorite:
                case R.id.drawer_history:
                case R.id.drawer_download:
                case R.id.drawer_source:
                case R.id.drawer_tag:
                    mCheckItem = itemId;
                    showProgressBar();
                    getFragmentManager().beginTransaction().hide(mCurrentFragment).commit();
                    mToolbar.setTitle(item.getTitle().toString());
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    break;
                case R.id.drawer_night:
                    night = !night;
                    CimocApplication.getPreferences().putBoolean(PreferenceManager.PREF_NIGHT, night);
                    mNavigationView.getMenu().findItem(R.id.drawer_night).setTitle(night ? R.string.drawer_light : R.string.drawer_night);
                    mNightMask.setVisibility(night ? View.VISIBLE : View.INVISIBLE);
                    break;
                case R.id.drawer_settings:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    break;
                case R.id.drawer_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    break;
            }
        }
        return true;
    }

    @Override
    public void onLastLoadSuccess(int source, String cid, String title, String cover) {
        onLastChange(source, cid, title,cover);
    }

    @Override
    public void onLastLoadFail() {
        showSnackbar(R.string.main_last_read_fail);
    }

    @Override
    public void onLastChange(int source, String cid, String title, String cover) {
        mLastSource = source;
        mLastCid = cid;
        mLastText.setText(title);
        mDraweeView.setVisibility(View.VISIBLE);
        DraweeController controller = mProvider.get(source)
                .setOldController(mDraweeView.getController())
                .setUri(cover)
                .build();
        mDraweeView.setController(controller);
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        mFilterSet = new HashSet<>();
        for (Source source : list) {
            if (source.getType() < 100) {
                mFilterSet.add(source.getTitle());
            }
        }
        mSourceList = list;
    }

    @Override
    public void onSourceLoadFail() {
        showSnackbar(R.string.main_source_load_fail);
    }

    @Override
    protected String getDefaultTitle() {
        int home = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_FAVORITE);
        return getResources().getStringArray(R.array.home_items)[home];
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected View getLayoutView() {
        return mDrawerLayout;
    }

}
