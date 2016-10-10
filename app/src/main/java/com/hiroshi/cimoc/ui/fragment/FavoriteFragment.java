package com.hiroshi.cimoc.ui.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.FavoriteAdapter;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.NotificationUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        mFavoriteAdapter = new FavoriteAdapter(getActivity(), new LinkedList<MiniComic>());
        mComicAdapter = mFavoriteAdapter;

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int flag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
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
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
        if (mBuilder != null) {
            NotificationUtils.cancelNotification(0, mManager);
            mBuilder = null;
        }
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
        mFavoriteAdapter.add(comic);
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
