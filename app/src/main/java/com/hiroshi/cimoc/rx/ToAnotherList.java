package com.hiroshi.cimoc.rx;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/23.
 */

public class ToAnotherList<T, R> implements Observable.Transformer<List<T>, List<R>> {

    private Func1<T, R> func;

    public ToAnotherList(Func1<T, R> func) {
        this.func = func;
    }

    @Override
    public Observable<List<R>> call(Observable<List<T>> observable) {
        return observable.flatMap(new Func1<List<T>, Observable<T>>() {
            @Override
            public Observable<T> call(List<T> list) {
                return Observable.from(list);
            }
        }).map(func).toList();
    }
    
}
