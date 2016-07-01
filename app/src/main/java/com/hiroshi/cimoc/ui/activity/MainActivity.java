package com.hiroshi.cimoc.ui.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainActivity extends BaseActivity {

    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @BindView(R.id.navigation_view) NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDrawer();
        FragmentManager manager = getFragmentManager();
        ComicFragment fragment = new ComicFragment();
        manager.beginTransaction().replace(R.id.container_fragment, fragment).commit();
    }

    private void initDrawer() {
        ActionBarDrawerToggle drawerToggle =
                new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0) {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                    }
                };
        drawerToggle.syncState();
        mDrawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_main;
    }

    @Override
    protected String getDefaultTitle() {
        return "Cimoc";
    }
}
