package com.hiroshi.cimoc.ui.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.collections.FilterList;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.FavoriteAdapter;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.NotificationUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends GridFragment implements FavoriteView {

    private FavoriteAdapter mFavoriteAdapter;
    private FavoritePresenter mPresenter;
    private Notification.Builder mBuilder;
    private NotificationManager mManager;

    @Override
    protected void initPresenter() {
        mPresenter = new FavoritePresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        super.initView();
        mActionButton.setImageResource(R.drawable.ic_sync_white_24dp);
        mManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void initAdapter() {
        Set<String> filterSet = new HashSet<>();
        filterSet.add("已完结");
        filterSet.add("连载中");
        FilterList<MiniComic, String> list = new FilterList<MiniComic, String>(filterSet) {
            @Override
            protected boolean isFilter(Set<String> filter, MiniComic data) {
                return filter.contains(data.isFinish() != null && data.isFinish() ? "已完结" : "连载中") ||
                        filter.contains(SourceManager.getTitle(data.getSource()));
            }
        };
        mFavoriteAdapter = new FavoriteAdapter(getActivity(), list);
        mComicAdapter = mFavoriteAdapter;

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int flag = mFavoriteAdapter.isFull() ?
                        (ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) : 0;
                return makeMovementFlags(flag, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (fromPosition == toPosition) {
                    return false;
                }

                List<MiniComic> list = mFavoriteAdapter.getDateSet();
                long fromId = list.get(fromPosition).getId();
                long toId = list.get(toPosition).getId();
                boolean isBack = fromPosition < toPosition;
                if (isBack) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(list, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(list, i, i - 1);
                    }
                }
                mPresenter.updateComic(fromId, toId, isBack);
                mFavoriteAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void initData() {
        mPresenter.loadComic();
    }

    @Override
    public void onDestroy() {
        if (mBuilder != null) {
            NotificationUtils.cancelNotification(0, mManager);
        }
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.favorite_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite_filter:
                mPresenter.loadFilter();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActionConfirm() {
        if (mBuilder == null) {
            mPresenter.checkUpdate();
            mBuilder = NotificationUtils.getBuilder(getActivity(), R.drawable.ic_sync_white_24dp,
                    R.string.favorite_update_doing, true, 0, 0, true);
            NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        } else {
            showSnackbar(R.string.favorite_update_doing);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mFavoriteAdapter.clickItem(position);
        Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onItemAdd(MiniComic comic) {
        mFavoriteAdapter.add(0, comic);
    }

    @Override
    public void onItemAdd(List<MiniComic> list) {
        mFavoriteAdapter.addAll(0, list);
    }

    @Override
    public void onItemRemove(long id) {
        mFavoriteAdapter.removeById(id);
    }

    @Override
    public void onSourceRemove(int source) {
        mFavoriteAdapter.removeBySource(source);
    }

    @Override
    public void onFilterLoad(final String[] filter) {
        final Set<String> filterSet = mFavoriteAdapter.getFilterSet();
        final boolean[] checked = new boolean[filter.length];
        for (int i = 0; i != filter.length; ++i) {
            checked[i] = filterSet.contains(filter[i]);
        }
        DialogUtils.buildMultiChoiceDialog(getActivity(), R.string.favorite_filter_select, filter, checked,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checked[which] = isChecked;
                    }
                }, -1, null, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i != filter.length; ++i) {
                            if (checked[i]) {
                                filterSet.add(filter[i]);
                            } else {
                                filterSet.remove(filter[i]);
                            }
                        }
                        mFavoriteAdapter.updateFilterSet();
                    }
                }).show();
    }

    @Override
    public void onComicUpdate(Comic comic, int progress, int max) {
        if (comic != null) {
            mFavoriteAdapter.moveToFirst(new MiniComic(comic));
        }
        mBuilder.setProgress(max, progress, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
    }

    @Override
    public void onCheckComplete() {
        NotificationUtils.setBuilder(getActivity(), mBuilder, R.string.favorite_update_finish, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        mBuilder = null;
    }

    @Override
    protected int getActionRes() {
        return R.string.favorite_update_confirm;
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_sync_white_24dp;
    }

}
