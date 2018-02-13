package com.hiroshi.cimoc.ui.fragment.recyclerview.grid;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.misc.NotificationWrapper;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.FavoriteView;
import com.hiroshi.cimoc.utils.HintUtils;

import java.util.Calendar;
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

    private static final String NOTIFICATION_CHECK_UPDATE = "NOTIFICATION_CHECK_UPDATE";

    private FavoritePresenter mPresenter;
    private NotificationWrapper mNotification;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new FavoritePresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mGridAdapter.setSymbol(true);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mNotification != null) {
            mNotification.cancel();
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
                checkUpdate();
                break;
            case DIALOG_REQUEST_DELETE:
                mPresenter.unfavoriteComic(mSavedId);
                HintUtils.showToast(getActivity(), R.string.common_execute_success);
                break;
        }
    }

    public void cancelAllHighlight() {
        mPresenter.cancelAllHighlight();
        mGridAdapter.cancelAllHighlight();
    }

    private void checkUpdate() {
        if (mNotification == null) {
            mPresenter.checkUpdate();
            mNotification = new NotificationWrapper(getActivity(), NOTIFICATION_CHECK_UPDATE,
                    R.drawable.ic_sync_white_24dp, true);
            mNotification.post(getString(R.string.favorite_check_update_doing), 0, 0);
        } else {
            HintUtils.showToast(getActivity(), R.string.favorite_check_update_doing);
        }
    }

    @Override
    protected void performActionButtonClick() {
        if (mGridAdapter.getDateSet().isEmpty()) {
            return;
        }
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.favorite_check_update_confirm, true, DIALOG_REQUEST_UPDATE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        super.onComicLoadSuccess(list);
        WifiManager manager =
                (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled() &&
                mPreference.getBoolean(PreferenceManager.PREF_OTHER_CHECK_UPDATE, false)) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTimeInMillis(mPreference.getLong(PreferenceManager.PREF_OTHER_CHECK_UPDATE_LAST, 0));
            if (day != calendar.get(Calendar.DAY_OF_YEAR)) {
                mPreference.putLong(PreferenceManager.PREF_OTHER_CHECK_UPDATE_LAST, System.currentTimeMillis());
                checkUpdate();
            }
        }
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
        mNotification.post(progress, max);
    }

    @Override
    public void onComicCheckFail() {
        mNotification.post(getString(R.string.favorite_check_update_fail), false);
        mNotification = null;
    }

    @Override
    public void onComicCheckComplete() {
        mNotification.post(getString(R.string.favorite_check_update_done), false);
        mNotification.cancel();
        mNotification = null;
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
