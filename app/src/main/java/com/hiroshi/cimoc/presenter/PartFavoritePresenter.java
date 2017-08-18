package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.TagManager;
import com.hiroshi.cimoc.manager.TagRefManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.PartFavoriteView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class PartFavoritePresenter extends BasePresenter<PartFavoriteView> {

    private ComicManager mComicManager;
    private TagRefManager mTagRefManager;
    private long mTagId;
    private LongSparseArray<Comic> mSavedComic;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
        mSavedComic = new LongSparseArray<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onComicRemove((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_TAG_UPDATE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData();
                List<Long> list = (List<Long>) rxEvent.getData(1);
                if (list.contains(mTagId)) {
                    MiniComic comic = new MiniComic(mComicManager.load(id));
                    mBaseView.onComicAdd(comic);
                } else {
                    mBaseView.onComicRemove(id);
                }
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_CANCEL_HIGHLIGHT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onHighlightCancel((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onComicRead((MiniComic) rxEvent.getData());
            }
        });
    }

    private Observable<List<Comic>> getObservable(long id) {
        if (id == TagManager.TAG_CONTINUE) {
            return mComicManager.listContinueInRx();
        } else if (id == TagManager.TAG_FINISH) {
            return mComicManager.listFinishInRx();
        } else {
            return mComicManager.listFavoriteByTag(id);
        }
    }

    public void load(long id) {
        mTagId = id;
        mCompositeSubscription.add(getObservable(id)
                .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
                    }
                }));
    }

    private List<Long> buildIdList(List<MiniComic> list) {
        List<Long> result = new ArrayList<>(list.size());
        for (MiniComic comic : list) {
            result.add(comic.getId());
        }
        return result;
    }

    public void loadComicTitle(List<MiniComic> list) {
        // TODO 不使用 in
        mCompositeSubscription.add(mComicManager.listFavoriteNotIn(buildIdList(list))
                .compose(new ToAnotherList<>(new Func1<Comic, String>() {
                    @Override
                    public String call(Comic comic) {
                        mSavedComic.put(comic.getId(), comic);
                        return comic.getTitle();
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> list) {
                        mBaseView.onComicTitleLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicTitleLoadFail();
                    }
                }));
    }

    public void insert(boolean[] check) {
        // Todo 异步
        if (check != null && mSavedComic != null && check.length == mSavedComic.size()) {
            List<TagRef> rList = new ArrayList<>();
            List<MiniComic> cList = new ArrayList<>();
            for (int i = 0; i != check.length; ++i) {
                if (check[i]) {
                    MiniComic comic = new MiniComic(mSavedComic.valueAt(i));
                    rList.add(new TagRef(null, mTagId, comic.getId()));
                    cList.add(comic);
                }
            }
            mTagRefManager.insertInTx(rList);
            mBaseView.onComicInsertSuccess(cList);
        } else {
            mBaseView.onComicInsertFail();
        }
        mSavedComic.clear();
    }

    public void delete(long id) {
        mTagRefManager.delete(mTagId, id);
    }

}
