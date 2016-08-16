package com.hiroshi.cimoc.ui.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.FavoriteAdapter;
import com.hiroshi.cimoc.utils.DialogFactory;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends BaseFragment {

    @BindView(R.id.favorite_comic_list) RecyclerView mRecyclerView;

    private FavoriteAdapter mFavoriteAdapter;
    private FavoritePresenter mPresenter;
    private Notification.Builder mBuilder;
    private NotificationManager mManager;

    private boolean isChecking;

    @Override
    protected void initView() {
        isChecking = false;
        mManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(getActivity());
        mFavoriteAdapter = new FavoriteAdapter(getActivity(), mPresenter.getComicList());
        mFavoriteAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MiniComic comic = mFavoriteAdapter.cancelNew(position);
                Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid());
                startActivity(intent);
            }
        });
        mFavoriteAdapter.setOnItemLongClickListener(new BaseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogFactory.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.favorite_delete_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPresenter.deleteComic(mFavoriteAdapter.getItem(position));
                                mFavoriteAdapter.remove(position);
                            }
                        }).show();
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setAdapter(mFavoriteAdapter);
        mRecyclerView.addItemDecoration(mFavoriteAdapter.getItemDecoration());
    }

    @OnClick(R.id.favorite_check_btn) void onCheckClick() {
        if (!isChecking) {
            DialogFactory.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.favorite_update_confirm,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isChecking = true;
                            UpdateTask task = new UpdateTask();
                            task.execute(mPresenter.getComicArray());
                        }
                    }).show();
        } else {
            showSnackbar(R.string.favorite_update_doing);
        }
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_favorite;
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new FavoritePresenter(this);
    }

    public void addItem(MiniComic comic) {
        mFavoriteAdapter.add(0, comic);
    }

    public void removeItem(long id) {
        mFavoriteAdapter.removeById(id);
    }

    public void removeItems(int source) {
        mFavoriteAdapter.removeBySource(source);
    }

    public void addItems(List<MiniComic> list) {
        mFavoriteAdapter.addAll(0, list);
    }

    class UpdateTask extends AsyncTask<MiniComic, Integer, List<MiniComic>> {
        @Override
        protected void onPreExecute() {
            mBuilder.setSmallIcon(R.drawable.ic_sync_white_24dp)
                    .setContentTitle("Cimoc")
                    .setContentText("正在检查更新")
                    .setTicker("正在检查更新")
                    .setOngoing(true)
                    .setProgress(0, 0, true);
            notifyBuilder();
        }

        @Override
        protected List<MiniComic> doInBackground(MiniComic... params) {
            List<MiniComic> list = new LinkedList<>();
            int count = 1;
            for (MiniComic comic : params) {
                int source = comic.getSource();
                if (source >= 100) {
                    continue;
                }
                Manga manga = SourceManager.getManga(source);
                String update = manga.check(comic.getCid());
                if (update != null && !comic.getUpdate().equals(update)) {
                    comic.setUpdate(update);
                    comic.setStatus(true);
                    list.add(comic);
                }
                publishProgress(count++, params.length);
            }
            return list;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mBuilder.setProgress(values[1], values[0], false);
            notifyBuilder();
        }

        @Override
        protected void onPostExecute(List<MiniComic> list) {
            mPresenter.updateComic(list);
            mFavoriteAdapter.updateAll(list);
            mBuilder.setOngoing(false)
                    .setTicker("检查完成")
                    .setContentText("检查完成")
                    .setProgress(0, 0, false);
            notifyBuilder();
            isChecking = false;
        }
    }

    private void notifyBuilder() {
        if (Build.VERSION.SDK_INT >= 16) {
            mManager.notify(1, mBuilder.build());
        } else {
            mManager.notify(1, mBuilder.getNotification());
        }
    }

}
