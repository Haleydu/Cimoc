package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.utils.DocumentUtils;
import com.hiroshi.cimoc.utils.FileUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class Storage {

    public static String STORAGE_DIR;
    public static String PICTURE = "picture";

    public static DocumentFile getRootDocumentFile(Context context) {
        String uri = CimocApplication.getStorageUri();
        if (uri == null) {
            return DocumentFile.fromFile(new File(FileUtils.getPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc")));
        } else if (uri.startsWith("content")) {
            return DocumentFile.fromTreeUri(context, Uri.parse(uri));
        } else {
            return DocumentFile.fromFile(new File(Uri.parse(uri).getPath()));
        }
    }

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

    private static String buildFileName(String filename) {
        String suffix = StringUtils.split(filename, "\\.", -1);
        if (suffix == null) {
            suffix = "jpg";
        } else {
            suffix = suffix.split("\\?")[0];
        }
        return StringUtils.getDateStringWithSuffix(suffix);
    }

    public static Observable<String> savePicture(final ContentResolver resolver, final DocumentFile root, final InputStream stream, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, PICTURE);
                    if (dir != null) {
                        DocumentFile file = dir.createFile("image/jpeg", buildFileName(url));
                        DocumentUtils.writeBinaryToFile(resolver, file, stream);
                        subscriber.onNext(file.getUri().getPath()); // Todo 需要改
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
