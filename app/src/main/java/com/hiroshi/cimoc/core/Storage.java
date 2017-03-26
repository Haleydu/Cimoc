package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.hiroshi.cimoc.saf.DocumentFile;
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

    private static boolean copyFile(ContentResolver resolver, DocumentFile src,
                                    DocumentFile parent, Subscriber<? super String> subscriber) {
        DocumentFile file = DocumentUtils.getOrCreateFile(parent, src.getName());
        if (file != null) {
            subscriber.onNext(StringUtils.format("正在移动 %s...", src.getUri().getLastPathSegment()));
            try {
                DocumentUtils.writeBinaryToFile(resolver, src, file);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean copyDir(ContentResolver resolver, DocumentFile src,
                                   DocumentFile parent, Subscriber<? super String> subscriber) {
        if (src.isDirectory()) {
            DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(parent, src.getName());
            for (DocumentFile file : src.listFiles()) {
                if (file.isDirectory()) {
                    if (!copyDir(resolver, file, dir, subscriber)) {
                        return false;
                    }
                } else if (!copyFile(resolver, file, dir, subscriber)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean copyDir(ContentResolver resolver, DocumentFile src,
                                   DocumentFile dst, String name, Subscriber<? super String> subscriber) {
        DocumentFile file = src.findFile(name);
        if (file != null && file.isDirectory()) {
            return copyDir(resolver, file, dst, subscriber);
        }
        return true;
    }

    private static void deleteDir(DocumentFile parent, String name, Subscriber<? super String> subscriber) {
        DocumentFile file = parent.findFile(name);
        if (file != null && file.isDirectory()) {
            subscriber.onNext(StringUtils.format("正在删除 %s", file.getUri().getLastPathSegment()));
            file.delete();
        }
    }

    private static boolean isDirSame(DocumentFile root, DocumentFile dst) {
        return root.getUri().getScheme().equals("file") && dst.getUri().getPath().endsWith("primary:Cimoc") ||
                root.getUri().getPath().equals(dst.getUri().getPath());
    }

    public static Observable<String> moveRootDir(final ContentResolver resolver, final DocumentFile root, final DocumentFile dst) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (dst.canRead() && !isDirSame(root, dst)) {
                    root.refresh();
                    if (copyDir(resolver, root, dst, BACKUP, subscriber) &&
                            copyDir(resolver, root, dst, DOWNLOAD, subscriber) &&
                            copyDir(resolver, root, dst, PICTURE, subscriber)) {
                        deleteDir(root, BACKUP, subscriber);
                        deleteDir(root, DOWNLOAD, subscriber);
                        deleteDir(root, PICTURE, subscriber);
                        subscriber.onCompleted();
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<String> savePicture(final ContentResolver resolver, final DocumentFile root,
                                                 final InputStream stream, final String filename) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, PICTURE);
                    if (dir != null) {
                        DocumentFile file = DocumentUtils.getOrCreateFile(dir, filename);
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
