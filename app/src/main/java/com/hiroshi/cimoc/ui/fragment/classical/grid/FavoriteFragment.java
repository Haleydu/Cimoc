package com.hiroshi.cimoc.ui.fragment.classical.grid;

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
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.NotificationUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends GridFragment implements FavoriteView {

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
        super.initView();
        mManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mGridAdapter.setSymbol(true);
        mGridAdapter.setOnItemLongClickListener(null);
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

                List<MiniComic> list = mGridAdapter.getDateSet();
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
                mGridAdapter.notifyItemMoved(fromPosition, toPosition);
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
    public void onMessagePositiveClick(int type) {
        if (mBuilder == null) {
            mPresenter.checkUpdate();
            mBuilder = NotificationUtils.getBuilder(getActivity(), R.drawable.ic_sync_white_24dp,
                    R.string.favorite_check_update_doing, true, 0, 0, true);
            NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        } else {
            showSnackbar(R.string.favorite_check_update_doing);
        }
    }

    @Override
    protected void onActionButtonClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.favorite_check_update_confirm, true);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.clickItem(position);
        Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onItemAdd(MiniComic comic) {
        mGridAdapter.addAfterHighlight(comic);
    }

    @Override
    public void onItemAdd(List<MiniComic> list) {
        mGridAdapter.addAll(0, list);
    }

    @Override
    public void onItemRemove(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicUpdate(Comic comic, int progress, int max) {
        if (comic != null) {
            mGridAdapter.update(new MiniComic(comic));
        }
        mBuilder.setProgress(max, progress, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
    }

    @Override
    public void onCheckComplete() {
        NotificationUtils.setBuilder(getActivity(), mBuilder, R.string.favorite_check_update_done, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        mBuilder = null;
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_sync_white_24dp;
    }

}
