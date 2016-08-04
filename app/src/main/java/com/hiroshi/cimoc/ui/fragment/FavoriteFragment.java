package com.hiroshi.cimoc.ui.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
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

    private ComicAdapter mComicAdapter;
    private FavoritePresenter mPresenter;
    private Notification.Builder mBuilder;
    private NotificationManager mManager;

    private boolean isChecking;

    @Override
    protected void initView() {
        isChecking = false;
        mManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new Notification.Builder(getActivity());
        mComicAdapter = new ComicAdapter(getActivity(), mPresenter.getComicList());
        mComicAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.onItemClick(mComicAdapter.getItem(position));
            }
        });
        mComicAdapter.setOnItemLongClickListener(new BaseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogFactory.buildPositiveDialog(getActivity(), "删除提示", "是否删除该收藏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.onPositiveClick(mComicAdapter.getItem(position));
                        mComicAdapter.remove(position);
                    }
                }).show();
            }
        });
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setAdapter(mComicAdapter);
        mRecyclerView.addItemDecoration(mComicAdapter.getItemDecoration());
    }

    @OnClick(R.id.favorite_check_btn) void onCheckClick() {
        if (!isChecking) {
            isChecking = true;
            UpdateTask task = new UpdateTask();
            task.execute(mPresenter.getComicArray());
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
        mComicAdapter.add(0, comic);
    }

    public void removeItem(long id) {
        mComicAdapter.removeById(id);
    }

    public void addItems(List<MiniComic> list) {
        mComicAdapter.addAll(0, list);
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
            if (Build.VERSION.SDK_INT >= 16) {
                mManager.notify(1, mBuilder.build());
            } else {
                mManager.notify(1, mBuilder.getNotification());
            }
        }

        @Override
        protected List<MiniComic> doInBackground(MiniComic... params) {
            List<MiniComic> list = new LinkedList<>();
            int count = 1;
            for (MiniComic comic : params) {
                int source = comic.getSource();
                if (source == Kami.SOURCE_EHENTAI) {
                    continue;
                }
                Manga manga = Kami.getMangaById(source);
                String update = manga.check(comic.getCid());
                if (update != null && !comic.getUpdate().equals(update)) {
                    comic.setUpdate(update);
                    list.add(comic);
                }
                publishProgress(count++, params.length);
            }
            return list;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.e("----------", "------" + values[0] + "---" + values[1]);
            mBuilder.setProgress(values[1], values[0], false)
                    .setTicker(null);
            if (Build.VERSION.SDK_INT >= 16) {
                mManager.notify(1, mBuilder.build());
            } else {
                mManager.notify(1, mBuilder.getNotification());
            }
        }

        @Override
        protected void onPostExecute(List<MiniComic> list) {
            //mPresenter.updateComic(list);
            mBuilder.setOngoing(false)
                    .setTicker(null)
                    .setContentText("检查完成")
                    .setProgress(0, 0, false);
            if (Build.VERSION.SDK_INT >= 16) {
                mManager.notify(1, mBuilder.build());
            } else {
                mManager.notify(1, mBuilder.getNotification());
            }
            isChecking = false;
        }
    }


}
