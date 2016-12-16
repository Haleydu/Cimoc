package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.support.v4.provider.DocumentFile;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.utils.DocumentUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class Storage {

    public static String DOWNLOAD = "download";
    public static String PICTURE = "picture";
    public static String BACKUP = "backup";

    private static boolean copyDir(ContentResolver resolver, DocumentFile src, DocumentFile dst, String name) {
        DocumentFile file = src.findFile(name);
        if (file != null && file.isDirectory()) {
            return DocumentUtils.copyDir(resolver, file, dst);
        }
        return true;
    }

    public static Observable<Integer> moveRootDir(final DocumentFile dst) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                DocumentFile src = CimocApplication.getDocumentFile();
                ContentResolver resolver = CimocApplication.getResolver();
                if (!src.getUri().equals(dst.getUri())) {
                    subscriber.onNext(1);
                    if (copyDir(resolver, src, dst, BACKUP)) {
                        subscriber.onNext(2);
                        if (copyDir(resolver, src, dst, DOWNLOAD)) {
                            subscriber.onNext(3);
                            if (copyDir(resolver, src, dst, PICTURE)) {
                                subscriber.onNext(4);
                                DocumentUtils.deleteDir(src);
                                subscriber.onCompleted();
                            }
                        }
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

    private static String buildFileName(String filename) {
        String suffix = StringUtils.split(filename, "\\.", -1);
        if (suffix == null) {
            suffix = "jpg";
        } else {
            suffix = suffix.split("\\?")[0];
        }
        return StringUtils.getDateStringWithSuffix(suffix);
    }

    public static Observable<String> savePicture(final InputStream stream, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    DocumentFile root = CimocApplication.getDocumentFile();
                    ContentResolver resolver = CimocApplication.getResolver();
                    DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, PICTURE);
                    if (dir != null) {
                        DocumentFile file = dir.createFile("", buildFileName(url));
                        DocumentUtils.writeBinaryToFile(resolver, file, stream);
                        subscriber.onNext(file.getUri().toString());
                        subscriber.onCompleted();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

}
