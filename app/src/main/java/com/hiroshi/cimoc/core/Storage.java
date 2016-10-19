package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.utils.FileUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class Storage {

    public static String STORAGE_DIR;

    public static Observable<Void> moveFolder(final String path) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                String dst = FileUtils.getPath(path, "Cimoc");
                if (FileUtils.mkDirsIfNotExist(dst)) {
                    if (!FileUtils.isDirsExist(STORAGE_DIR) || FileUtils.copyFolder(STORAGE_DIR, dst)) {
                        FileUtils.deleteDir(STORAGE_DIR);
                        STORAGE_DIR = dst;
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new Throwable());
                    }
                } else {
                    subscriber.onError(new Throwable());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<String> savePicture(final InputStream inputStream, final String suffix) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String filename = StringUtils.getDateStringWithSuffix(suffix);
                String path = FileUtils.getPath(STORAGE_DIR, "picture", filename);
                if (FileUtils.writeBinaryToFile(FileUtils.getPath(STORAGE_DIR, "picture"), filename, inputStream)) {
                    subscriber.onNext(path);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}
