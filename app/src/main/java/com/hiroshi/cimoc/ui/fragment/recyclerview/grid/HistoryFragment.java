package com.hiroshi.cimoc.ui.fragment.recyclerview.grid;

import android.os.Bundle;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.BuildConfig;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.HistoryPresenter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.HistoryView;
import com.hiroshi.cimoc.utils.HintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends GridFragment implements HistoryView {

    private static final int DIALOG_REQUEST_CLEAR = 1;
    private static final int DIALOG_REQUEST_INFO = 2;
    private static final int DIALOG_REQUEST_DELETE = 3;

    private static final int OPERATION_INFO = 0;
    private static final int OPERATION_DELETE = 1;

    private HistoryPresenter mPresenter;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new HistoryPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initData() {
        mPresenter.load();
        if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_REDUCE_AD, false)) {
            NUMBER_OF_ADS=2;
        }
        loadNativeAds();
    }

    @Override
    protected void performActionButtonClick() {
        if (mGridAdapter.getDateSet().isEmpty()) {
            return;
        }
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.history_clear_confirm, true, DIALOG_REQUEST_CLEAR);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
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
                                R.string.history_delete_confirm, true, DIALOG_REQUEST_DELETE);
                        fragment.setTargetFragment(this, 0);
                        fragment.show(getFragmentManager(), null);
                }
                break;
            case DIALOG_REQUEST_CLEAR:
                showProgressDialog();
                mPresenter.clear();
                break;
            case DIALOG_REQUEST_DELETE:
                showProgressDialog();
                mPresenter.delete(mSavedId);
                break;
        }
    }

    @Override
    public void onHistoryClearSuccess() {
        hideProgressDialog();
        mGridAdapter.clear();
        HintUtils.showToast(getActivity(), R.string.common_execute_success);
    }

    @Override
    public void onHistoryDelete(long id) {
        hideProgressDialog();
        mGridAdapter.removeItemById(mSavedId);
        HintUtils.showToast(getActivity(), R.string.common_execute_success);
    }

    @Override
    public void OnComicRestore(List<Object> list) {
        mGridAdapter.addAll(0, list);
    }

    @Override
    public void onItemUpdate(MiniComic comic) {
        mGridAdapter.remove(comic);
        mGridAdapter.add(0, comic);
    }

    @Override
    protected int getActionButtonRes() {
        return R.drawable.ic_delete_white_24dp;
    }


    @Override
    protected String[] getOperationItems() {
        return new String[]{getString(R.string.comic_info), getString(R.string.history_delete)};
    }

    public static int NUMBER_OF_ADS = 5;
    private AdLoader adLoader;
    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();

    private void insertAdsInCimocItems() {

        if (mNativeAds.size() <= 0) {
            return;
        }
        int offset = (mGridAdapter.getDateSet().size() / mNativeAds.size()) + 1;
        int index = 0;
        for (UnifiedNativeAd ad : mNativeAds) {
            if (mGridAdapter.getItemCount() == 0) return;
            mGridAdapter.add(index, ad);
            index = index + offset;
        }
        mGridAdapter.notifyDataSetChanged();
    }

    private void loadNativeAds() {
        AdLoader.Builder builder = new AdLoader.Builder(getActivity(), BuildConfig.ADMOB_NATIVE_HISTORY_UNIT_ID);
        adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // A native ad loaded successfully, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        mNativeAds.add(unifiedNativeAd);
                        if (!adLoader.isLoading()) {
                            insertAdsInCimocItems();
                        }
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        if (!adLoader.isLoading()) {
                            insertAdsInCimocItems();
                        }
                    }
                }).build();

        adLoader.loadAds(new AdRequest.Builder().build(), NUMBER_OF_ADS);
    }
}
