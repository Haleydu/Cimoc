package com.hiroshi.cimoc.rx;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public class RxBus {

    private static RxBus instance;

    private Subject<Object, Object> bus;

    private RxBus() {
        bus = new SerializedSubject<>(PublishSubject.create());
    }

    public void post(RxEvent event) {
        bus.onNext(event);
    }

    public Observable<RxEvent> toObservable(@RxEvent.EventType final int type) {
        return bus.ofType(RxEvent.class)
                .filter(new Func1<RxEvent, Boolean>() {
                    @Override
                    public Boolean call(RxEvent rxEvent) {
                        return rxEvent.getType() == type;
                    }
                }).onBackpressureBuffer().observeOn(AndroidSchedulers.mainThread());
    }

    public static RxBus getInstance() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

}
