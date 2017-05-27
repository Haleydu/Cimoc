package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.BaseView;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public abstract class BasePresenter<T extends BaseView> {

    protected T mBaseView;
    protected CompositeSubscription mCompositeSubscription;

    public void attachView(T view) {
        this.mBaseView = view;
        onViewAttach();
        mCompositeSubscription = new CompositeSubscription();
        addSubscription(RxEvent.EVENT_SWITCH_NIGHT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onNightSwitch();
            }
        });
        initSubscription();
    }

    protected void onViewAttach() {}

    protected void initSubscription() {}

    protected void addSubscription(@RxEvent.EventType int type, Action1<RxEvent> action) {
        mCompositeSubscription.add(RxBus.getInstance().toObservable(type).subscribe(action, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        }));
    }

    public void detachView() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        mBaseView = null;
    }

}
