package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;

import com.hiroshi.cimoc.utils.DocumentUtils;
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

    private static String DOWNLOAD = "download";
    private static String PICTURE = "picture";
    private static String BACKUP = "backup";

    public static DocumentFile initRoot(Context context, String uri) {
        if (uri == null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc");
            if (file.exists() || file.mkdirs()) {
                return DocumentFile.fromFile(file);
            } else {
                return null;
            }
        } else if (uri.startsWith("content")) {
            return DocumentFile.fromTreeUri(context, Uri.parse(uri));
        } else if (uri.startsWith("file")) {
            return DocumentFile.fromFile(new File(Uri.parse(uri).getPath()));
        } else {
            return DocumentFile.fromFile(new File(uri, "Cimoc"));
        }
    }

    private static boolean copyDir(ContentResolver resolver, DocumentFile src, DocumentFile dst, String name) {
        DocumentFile file = src.findFile(name);
        if (file != null && file.isDirectory()) {
            return DocumentUtils.copyDir(resolver, file, dst);
        }
        return true;
    }

    public static Observable<String> moveRootDir(final ContentResolver resolver, final DocumentFile root, final DocumentFile dst) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (dst.canWrite()) {
                    if (!root.getUri().equals(dst.getUri())) {
                        subscriber.onNext("正在移动备份文件");
                        if (copyDir(resolver, root, dst, BACKUP)) {
                            subscriber.onNext("正在移动下载文件");
                            if (copyDir(resolver, root, dst, DOWNLOAD)) {
                                subscriber.onNext("正在移动截图文件");
                                if (copyDir(resolver, root, dst, PICTURE)) {
                                    subscriber.onNext("正在删除原文件");
                                    DocumentUtils.deleteDir(root);
                                    subscriber.onCompleted();
                                }
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

    public static Observable<String> savePicture(final ContentResolver resolver, final DocumentFile root,
                                                 final InputStream stream, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
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
