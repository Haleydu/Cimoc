package com.hiroshi.cimoc.ui.fragment.classical.grid;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.NotificationUtils;

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
        MiniComic comic = mGridAdapter.getItem(position);
        if (comic.isHighlight()) {
            comic.setFavorite(System.currentTimeMillis());
            comic.setHighlight(false);
            mGridAdapter.update(comic, false);
        }
        Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(),
                comic.getSource(), comic.getCid(), ComicFragment.TYPE_FAVORITE);
        startActivity(intent);
    }

    @Override
    public void OnComicFavorite(MiniComic comic) {
        mGridAdapter.addAfterHighlight(comic);
    }

    @Override
    public void OnComicRestore(List<MiniComic> list) {
        mGridAdapter.addAll(0, list);
    }

    @Override
    public void OnComicUnFavorite(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicRead(MiniComic comic) {
        mGridAdapter.update(comic, false);
    }

    @Override
    public void onComicFilterSuccess(List<MiniComic> list) {
        mGridAdapter.setData(list);
    }

    @Override
    public void onComicFilterFail() {
        showSnackbar(R.string.comic_filter_fail);
    }

    @Override
    public void onComicCheckSuccess(MiniComic comic, int progress, int max) {
        if (comic != null) {
            mGridAdapter.update(comic, false);
        }
        mBuilder.setProgress(max, progress, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
    }

    @Override
    public void onComicCheckFail() {
        NotificationUtils.setBuilder(getActivity(), mBuilder, R.string.favorite_check_update_fail, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        mBuilder = null;
    }

    @Override
    public void onComicCheckComplete() {
        NotificationUtils.setBuilder(getActivity(), mBuilder, R.string.favorite_check_update_done, false);
        NotificationUtils.notifyBuilder(0, mManager, mBuilder);
        mBuilder = null;
    }

    @Override
    public void onComicInsert(MiniComic comic) {
        if (!mGridAdapter.contains(comic)) {
            mGridAdapter.addAfterHighlight(comic);
        }
    }

    @Override
    public void onComicDelete(MiniComic comic) {
        mGridAdapter.remove(comic);
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_sync_white_24dp;
    }

}
