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

    public void onItemClick(int position) {
        MiniComic comic = mHistoryFragment.getItem(position);
        Intent intent = DetailActivity.createIntent(mHistoryFragment.getActivity(), comic.getId(), comic.getSource(), comic.getCid());
        mHistoryFragment.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.HISTORY_COMIC:
                mHistoryFragment.updateItem((MiniComic) msg.getData());
                break;
            case EventMessage.DELETE_HISTORY:
                mHistoryFragment.clearItem();
                break;
        }
    }

}
