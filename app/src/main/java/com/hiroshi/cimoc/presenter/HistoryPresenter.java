package com.hiroshi.cimoc.presenter;

import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.fragment.HistoryFragment;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.db.entity.ComicRecord;

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

    public List<ComicRecord> getComicRecord() {
        return mComicManager.listHistory();
    }

    public BaseAdapter.OnItemClickListener getItemClickListener() {
        return new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ComicRecord comic = mHistoryFragment.getItem(position);
                Intent intent = DetailActivity.createIntent(mHistoryFragment.getActivity(), comic.getSource(), comic.getPath());
                mHistoryFragment.startActivity(intent);
            }
        };
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.HISTORY_COMIC:
                ComicRecord comic = (ComicRecord) msg.getData();
                mHistoryFragment.updateItem(comic);
                break;
        }
    }

}
