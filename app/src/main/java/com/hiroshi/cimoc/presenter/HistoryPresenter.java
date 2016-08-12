package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.MiniComic;
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

    public void deleteHistory(MiniComic comic) {
        mComicManager.deleteHistory(comic.getId());
    }

    public void clearHistory() {
        mComicManager.cleanHistory();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.HISTORY_COMIC:
                mHistoryFragment.updateItem((MiniComic) msg.getData());
                break;
            case EventMessage.DELETE_HISTORY:
                mHistoryFragment.clearItem();
                mHistoryFragment.hideProgressDialog();
                String text = mHistoryFragment.getString(R.string.history_clear_success) + (int) msg.getData();
                mHistoryFragment.showSnackbar(text);
                break;
            case EventMessage.COMIC_DELETE:
                mHistoryFragment.removeItems((int) msg.getData());
                break;
        }
    }

}
