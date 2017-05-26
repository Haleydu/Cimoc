package com.hiroshi.cimoc.ui.fragment.recyclerview.grid;

import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.NotificationUtils;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends GridFragment implements FavoriteView {

    private static final int DIALOG_REQUEST_UPDATE = 1;
    private static final int DIALOG_REQUEST_INFO = 2;
    private static final int DIALOG_REQUEST_DELETE = 3;

    private static final int OPERATION_INFO = 0;
    private static final int OPERATION_DELETE = 1;

    private FavoritePresenter mPresenter;
    private Notification.Builder mBuilder;
    private NotificationManager mManager;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new FavoritePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mManager = NotificationUtils.getManager(getActivity());
        mGridAdapter.setSymbol(true);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBuilder != null) {
            NotificationUtils.cancelNotification(0, mManager);
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_OPERATION:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                switch (index) {
                    case OPERATION_INFO:
                        showComicInfo(mPresenter.load(mSavedId), DIALOG_REQUEST_INFO);
                        break;
                    case OPERATION_DELETE:
                        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                                R.string.favorite_delete_confirm, true, DIALOG_REQUEST_DELETE);
                        fragment.setTargetFragment(this, 0);
                        fragment.show(getFragmentManager(), null);
                        break;
                }
                break;
            case DIALOG_REQUEST_UPDATE:
                if (mBuilder == null) {
                    mPresenter.checkUpdate();
                    mBuilder = NotificationUtils.getBuilder(getActivity(), R.drawable.ic_sync_white_24dp,
                            R.string.favorite_check_update_doing, true, 0, 0, true);
                    NotificationUtils.notifyBuilder(0, mManager, mBuilder);
                } else {
                    HintUtils.showToast(getActivity(), R.string.favorite_check_update_doing);
                }
                break;
            case DIALOG_REQUEST_DELETE:
                mPresenter.unfavoriteComic(mSavedId);
                HintUtils.showToast(getActivity(), R.string.common_execute_success);
                break;
        }
    }

    @Override
    protected void performActionButtonClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.favorite_check_update_confirm, true, DIALOG_REQUEST_UPDATE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void OnComicFavorite(MiniComic comic) {
        mGridAdapter.add(mGridAdapter.findFirstNotHighlight(), comic);
    }

    @Override
    public void OnComicRestore(List<MiniComic> list) {
        mGridAdapter.addAll(mGridAdapter.findFirstNotHighlight(), list);
    }

    @Override
    public void OnComicUnFavorite(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onComicCheckSuccess(MiniComic comic, int progress, int max) {
        if (comic != null) {
            mGridAdapter.remove(comic);
            mGridAdapter.add(0, comic);
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
    public void onHighlightCancel(MiniComic comic) {
        mGridAdapter.moveItemTop(comic);
    }

    @Override
    public void onComicRead(MiniComic comic) {
        mGridAdapter.moveItemTop(comic);
    }

    @Override
    protected int getActionButtonRes() {
        return R.drawable.ic_sync_white_24dp;
    }

    @Override
    protected String[] getOperationItems() {
        return new String[]{ getString(R.string.comic_info), getString(R.string.favorite_delete) };
    }

}
