package com.hiroshi.cimoc.core;

import android.os.Environment;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/9.
 */
public class Download {

    public static String dirPath =
            FileUtils.getPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc", "download");

    public static Observable<Void> update(final List<Chapter> list, final int source, final String cid, final String comic, final String cover) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    FileUtils.createFile(dirPath, ".nomedia");
                    String jsonString = writeInfoToJson(list, source, cid, comic, cover);
                    String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic);
                    if (FileUtils.writeStringToFile(dir, "index.cdif", "cimoc".concat(jsonString))) {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new Exception());
                    }
                } catch (Exception e) {
                    subscriber.onError(new Exception());
                }
            }
        }).observeOn(Schedulers.io());
    }

    private static String writeInfoToJson(List<Chapter> list, int source, String cid, String comic, String cover) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("s", source);
        object.put("i", cid);
        object.put("m", comic);
        object.put("o", cover);
        JSONArray array = new JSONArray();
        for (Chapter chapter : list) {
            JSONObject temp = new JSONObject();
            temp.put("t", chapter.getTitle());
            temp.put("p", chapter.getPath());
            array.put(temp);
        }
        object.put("c", array);
        return object.toString();
    }

    public static Observable<List<String>> get(final int source, final String comic) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic);

                char[] magic = FileUtils.readCharFromFile(dir, "index.cdif", 5);
                if (!Arrays.equals(magic, "cimoc".toCharArray())) {
                    subscriber.onError(new Exception());
                } else {
                    String jsonString = FileUtils.readSingleLineFromFile(dir, "index.cdif");
                    if (jsonString != null) {
                        try {
                            List<String> list = readPathFromJson(jsonString.substring(5));
                            subscriber.onNext(list);
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            e.printStackTrace();
                            subscriber.onError(new Exception());
                        }
                    } else {
                        subscriber.onError(new Exception());
                    }
                }
            }
        }).observeOn(Schedulers.io());
    }

    private static List<String> readPathFromJson(String jsonString) throws JSONException {
        JSONArray array = new JSONObject(jsonString).getJSONArray("c");
        int size = array.length();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i != size; ++i) {
            JSONObject object = array.getJSONObject(i);
            list.add(object.getString("p"));
        }
        return list;
    }

    public static Observable<List<ImageUrl>> images(final int source, final String comic, final String title) {
        return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
            @Override
            public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic, title);
                String[] filenames = FileUtils.listFilesName(dir);
                if (filenames.length == 0) {
                    subscriber.onError(new Exception());
                } else {
                    List<ImageUrl> list = new ArrayList<>(filenames.length);
                    for (int i = 0; i < filenames.length; ++i) {
                        list.add(new ImageUrl(i + 1, "file://".concat(FileUtils.getPath(dir, filenames[i])), false));
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Void> delete(final int source, final String comic, final String title) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic, title);
                FileUtils.deleteDir(dir);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Void> delete(final int source, final String comic, final List<String> list) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                for (String title : list) {
                    String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic, title);
                    FileUtils.deleteDir(dir);
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

}
