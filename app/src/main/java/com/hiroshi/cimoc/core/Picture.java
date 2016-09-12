package com.hiroshi.cimoc.core;

import android.os.Environment;

import com.hiroshi.cimoc.utils.FileUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/10.
 */
public class Picture {

    public static String dirPath =
            FileUtils.getPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc", "picture");

    public static Observable<Void> save(final InputStream inputStream, final String suffix) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                String filename = suffix == null ?
                        StringUtils.getDateStringWithSuffix("jpg") : StringUtils.getDateStringWithSuffix(suffix);
                if (FileUtils.writeBinaryToFile(dirPath, filename, inputStream)) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}
