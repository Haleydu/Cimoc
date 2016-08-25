package com.hiroshi.cimoc.ui.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.FavoriteAdapter;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.DialogFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends BaseFragment implements FavoriteView {

    @BindView(R.id.favorite_comic_list) RecyclerView mRecyclerView;

    private FavoriteAdapter mFavoriteAdapter;
    private FavoritePresenter mPresenter;
    private Notification.Builder mBuilder;
    private NotificationManager mManager;

    private int max;

    @Override
    protected void initView() {
        mManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(getActivity());
        mFavoriteAdapter = new FavoriteAdapter(getActivity(), new LinkedList<MiniComic>());
        mFavoriteAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MiniComic comic = mFavoriteAdapter.cancelHighlight(position);
                Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid());
                startActivity(intent);
            }
        });
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
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setAdapter(mFavoriteAdapter);
        mRecyclerView.addItemDecoration(mFavoriteAdapter.getItemDecoration());
    }

    @Override
    protected void initData() {
        max = -1;
        mPresenter.loadComic();
    }

    @OnClick(R.id.favorite_check_btn) void onCheckClick() {
        if (max == -1) {
            DialogFactory.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.favorite_update_confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            max = mFavoriteAdapter.getItemCount();
                            mPresenter.checkUpdate();
                            mBuilder.setSmallIcon(R.drawable.ic_sync_white_24dp)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(getString(R.string.favorite_update_doing))
                                    .setTicker(getString(R.string.favorite_update_doing))
                                    .setOngoing(true)
                                    .setProgress(0, 0, true);
                            notifyBuilder();
                        }
                    }).show();
        } else {
            showSnackbar(R.string.favorite_update_doing);
        }
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_favorite;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new FavoritePresenter();
        mPresenter.attachView(this);
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
    public void onProgressChange(int progress) {
        mBuilder.setProgress(max, progress, false);
        notifyBuilder();
    }

    @Override
    public void onComicUpdate(MiniComic comic) {
        mFavoriteAdapter.update(comic);
    }

    @Override
    public void onCheckComplete() {
        mBuilder.setOngoing(false)
                .setTicker(getString(R.string.favorite_update_finish))
                .setContentText(getString(R.string.favorite_update_finish))
                .setProgress(0, 0, false);
        notifyBuilder();
        max = -1;
    }

    private void notifyBuilder() {
        if (Build.VERSION.SDK_INT >= 16) {
            mManager.notify(1, mBuilder.build());
        } else {
            mManager.notify(1, mBuilder.getNotification());
        }
    }

}
