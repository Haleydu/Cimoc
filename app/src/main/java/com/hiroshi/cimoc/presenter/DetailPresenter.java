package com.hiroshi.cimoc.presenter;

import android.util.Log;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter<DetailView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private Comic mComic;

    public DetailPresenter() {
        this.mComicManager = ComicManager.getInstance();
        this.mTaskManager = TaskManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                String last = (String) rxEvent.getData();
                int page = (int) rxEvent.getData(1);
                mComic.setHistory(System.currentTimeMillis());
                mComic.setLast(last);
                mComic.setPage(page);
                mBaseView.onChapterChange(last);
            }
        });
        addSubscription(RxEvent.COMIC_PAGE_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComic.setPage((Integer) rxEvent.getData());
                if (mComic.getId() != null) {
                    mComicManager.update(mComic);
                }
            }
        });
    }

    public void loadDetail(long id, final int source, final String cid) {
        Observable<Comic> observable =
                id == -1 ? mComicManager.loadInRx(source, cid) : mComicManager.loadInRx(id);
        observable.doOnNext(new Action1<Comic>() {
            @Override
            public void call(Comic comic) {
                if (comic == null) {
                    comic = new Comic(source, cid);
                } if (comic.getFavorite() != null && comic.getHighlight()) {
                    comic.setHighlight(false);
                    comic.setFavorite(System.currentTimeMillis());
                }
                mComic = comic;
            }
        }).flatMap(new Func1<Comic, Observable<List<Chapter>>>() {
            @Override
            public Observable<List<Chapter>> call(Comic comic) {
                return Manga.info(source, mComic);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Chapter>>() {
                    @Override
                    public void onCompleted() {
                        mBaseView.onDetailLoadSuccess();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (mBaseView != null) {
                            if (e instanceof Manga.NetworkErrorException) {
                                mBaseView.onNetworkError();
                            } else {
                                mBaseView.onComicLoad(mComic);
                                mBaseView.onParseError();
                            }
                        }
                    }

                    @Override
                    public void onNext(List<Chapter> list) {
                        mBaseView.onComicLoad(mComic);
                        mBaseView.onChapterLoad(list);
                    }
                });
    }

    public void updateIndex(List<Chapter> list) {
        Download.update(list, mComic.getSource(), mComic.getCid(), mComic.getTitle(), mComic.getCover())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onUpdateIndexSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onUpdateIndexFail();
                    }
                });
    }

    public void loadDownload(final String[] array) {
        Long key = mComic.getId() == null ? -1 : mComic.getId();
        mTaskManager.list(key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        boolean[] download = new boolean[array.length];
                        boolean[] complete = new boolean[array.length];
                        for (int i = 0; i != array.length; ++i) {
                            for (Task task : list) {
                                if (task.getPath().equals(array[i])) {
                                    download[i] = true;
                                    complete[i] = task.getMax() != 0 && task.getProgress() == task.getMax();
                                }
                            }
                        }
                        mBaseView.onDownloadLoadSuccess(download, complete);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onDownloadLoadFail();
                    }
                });
    }

    public void addTask(final List<Chapter> list) {
        mComicManager.callInTx(new Callable<ArrayList<Task>>() {
            @Override
            public ArrayList<Task> call() throws Exception {
                Long key = mComic.getId();
                mComic.setDownload(System.currentTimeMillis());
                if (key != null) {
                    mComicManager.update(mComic);
                } else {
                    key = mComicManager.insert(mComic);
                    mComic.setId(key);
                }
                ArrayList<Task> taskList = new ArrayList<>(list.size());
                for (Chapter chapter : list) {
                    Task task = new Task(null, key, chapter.getPath(), chapter.getTitle(), 0, 0);
                    long id = mTaskManager.insert(task);
                    task.setId(id);
                    task.setInfo(mComic.getSource(), mComic.getCid(), mComic.getTitle());
                    task.setState(Task.STATE_WAIT);
                    taskList.add(task);
                }
                RxBus.getInstance().post(new RxEvent(RxEvent.TASK_ADD, new MiniComic(mComic), taskList));
                return taskList;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<Task>>() {
                    @Override
                    public void call(ArrayList<Task> list) {
                        mBaseView.onTaskAddSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTaskAddFail();
                    }
                });
    }

    public long updateLast(String last) {
        mComic.setHistory(System.currentTimeMillis());
        if (!last.equals(mComic.getLast())) {
            mComic.setLast(last);
            mComic.setPage(1);
        }
        if (mComic.getId() != null) {
            mComicManager.update(mComic);
        } else {
            long id = mComicManager.insert(mComic);
            mComic.setId(id);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.HISTORY_COMIC, new MiniComic(mComic)));
        return mComic.getId();
    }

    public void updateComic() {
        if (mComic.getId() != null) {
            mComicManager.update(mComic);
        }
    }

    public Comic getComic() {
        return mComic;
    }

    public void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        if (mComic.getId() == null) {
            long id = mComicManager.insert(mComic);
            mComic.setId(id);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.FAVORITE_COMIC, new MiniComic(mComic)));
    }

    public void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        if (mComic.getHistory() == null && mComic.getDownload() == null) {
            mComicManager.deleteByKey(id);
            mComic.setId(null);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.UN_FAVORITE_COMIC, id));
    }

}
