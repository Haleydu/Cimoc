package com.hiroshi.cimoc.presenter;

import android.content.Intent;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/18.
 */
public class HistoryPresenter extends BasePresenter {

    private HistoryFragment mHistoryFragment;
    private ComicManager mComicManager;

    public HistoryPresenter(HistoryFragment fragment) {
        mHistoryFragment = fragment;
        mComicManager = ComicManager.getInstance();
    }

    public List<MiniComic> getComic() {
        return mComicManager.listHistory();
    }

    public void onItemClick(MiniComic comic) {
        Intent intent = DetailActivity.createIntent(mHistoryFragment.getActivity(), comic.getId(), comic.getSource(), comic.getCid());
        mHistoryFragment.startActivity(intent);
    }

    public void onPositiveClick(MiniComic comic) {
        mComicManager.removeHistory(comic.getId());
    }

    public void onHistoryClearClick() {
        mComicManager.cleanHistory();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.HISTORY_COMIC:
                mHistoryFragment.updateItem((MiniComic) msg.getData());
                break;
            case EventMessage.DELETE_HISTORY:
                int count = (int) msg.getData();
                mHistoryFragment.clearItem();
                mHistoryFragment.hideProgressDialog();
                mHistoryFragment.showSnackbar("删除成功 共 " + count + " 条记录");
                break;
        }
    }

}
