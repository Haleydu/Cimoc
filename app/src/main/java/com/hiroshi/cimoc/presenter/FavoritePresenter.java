package com.hiroshi.cimoc.presenter;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.manager.TagRefManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.FavoriteView;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/6.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private ComicManager mComicManager;
    private SourceManager mSourceManager;
    private TagRefManager mTagRefManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                MiniComic comic = (MiniComic) rxEvent.getData();
                mBaseView.OnComicFavorite(comic);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.OnComicUnFavorite((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.OnComicRestore((List<Object>) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onComicRead((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_CANCEL_HIGHLIGHT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onHighlightCancel((MiniComic) rxEvent.getData());
            }
        });
    }

    public Comic load(long id) {
        return mComicManager.load(id);
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listFavoriteInRx()
                .compose(new ToAnotherList<>(new Func1<Comic, Object>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Object>>() {
                    @Override
                    public void call(List<Object> list) {
                        mBaseView.onComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
                    }
                }));
    }

    public void cancelAllHighlight() {
        mComicManager.cancelHighlight();
    }

    public void unfavoriteComic(long id) {
        Comic comic = mComicManager.load(id);
        comic.setFavorite(null);
        mTagRefManager.deleteByComic(id);
        mComicManager.updateOrDelete(comic);
        mBaseView.OnComicUnFavorite(id);
    }

    public void checkUpdate() {
        final List<Comic> list = mComicManager.listFavorite();
        mCompositeSubscription.add(Manga.checkUpdate(mSourceManager, list)
                .doOnNext(new Action1<Comic>() {
                    @Override
                    public void call(Comic comic) {
                        if (comic != null) {
                            mComicManager.update(comic);
                        }
                    }
                })
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Comic>() {
                    private int count = 0;

                    @Override
                    public void onCompleted() {
                        mBaseView.onComicCheckComplete();
                        if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
                            Context context = App.getAppContext();
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CONTENT, context.getString(R.string.favorite_check_update_done));
                            bundle.putBoolean(FirebaseAnalytics.Param.SUCCESS, true);
                            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBaseView.onComicCheckFail();
                        if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CONTENT, e.toString());
                            bundle.putBoolean(FirebaseAnalytics.Param.SUCCESS, false);
                            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(App.getAppContext());
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle);
                        }
                    }

                    @Override
                    public void onNext(Comic comic) {
                        ++count;
                        MiniComic miniComic = comic == null ? null : new MiniComic(comic);
                        mBaseView.onComicCheckSuccess(miniComic, count, list.size());
                    }
                }));
    }

}
