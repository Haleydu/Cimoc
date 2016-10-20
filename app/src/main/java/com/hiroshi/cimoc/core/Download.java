package com.hiroshi.cimoc.core;

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

    public static Observable<Void> updateComicIndex(final List<Chapter> list, final int source, final String cid,
                                                    final String title, final String cover) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    FileUtils.createFile(FileUtils.getPath(Storage.STORAGE_DIR, "download"), ".nomedia");
                    String jsonString = writeComicToJson(list, source, cid, title, cover);
                    String dir = buildPath(source, title);
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

    private static String writeComicToJson(List<Chapter> list, int source, String cid, String title, String cover) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("version", "1");
        object.put("type", "comic");
        object.put("source", source);
        object.put("cid", cid);
        object.put("title", title);
        object.put("cover", cover);
        JSONArray array = new JSONArray();
        for (Chapter chapter : list) {
            JSONObject temp = new JSONObject();
            temp.put("title", chapter.getTitle());
            temp.put("path", chapter.getPath());
            array.put(temp);
        }
        object.put("list", array);
        return object.toString();
    }

    private static String writeChapterToJson(String title, String path) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("version", "1");
        object.put("type", "chapter");
        object.put("title", title);
        object.put("path", path);
        return object.toString();
    }

    public static boolean updateChapterIndex(int source, String comic, String title, String path) {
        try {
            String jsonString = writeChapterToJson(title, path);
            String dir = buildPath(source, comic, title);
            return FileUtils.writeStringToFile(dir, "index.cdif", "cimoc".concat(jsonString));
        } catch (JSONException e) {
            return false;
        }
    }

    public static Observable<List<String>> getComicIndex(final int source, final String comic) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                String dir = buildPath(source, comic);
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
        JSONObject jsonObject = new JSONObject(jsonString);
        // We use "c" as the key in old version
        JSONArray array = jsonObject.has("list") ? jsonObject.getJSONArray("list") : jsonObject.getJSONArray("c");
        int size = array.length();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i != size; ++i) {
            JSONObject object = array.getJSONObject(i);
            list.add(object.has("path") ? object.getString("path") : object.getString("p"));
        }
        return list;
    }

    public static Observable<List<ImageUrl>> images(final int source, final String comic, final String title) {
        return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
            @Override
            public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                String dir = buildPath(source, comic, title);
                String[] filenames = FileUtils.listFilesNameNoSuffix(dir, "cdif");
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

    public static void delete(int source, String comic, List<String> list) {
        for (String title : list) {
            String dir = buildPath(source, comic, title);
            FileUtils.deleteDir(dir);
        }
    }

    public static void delete(int source, String comic) {
        String dir = buildPath(source, comic);
        FileUtils.deleteDir(dir);
    }

    public static String buildPath(int source, String comic) {
        return FileUtils.getPath(Storage.STORAGE_DIR, "download", SourceManager.getTitle(source),
                FileUtils.filterFilename(comic));
    }

    public static String buildPath(int source, String comic, String chapter) {
        return FileUtils.getPath(Storage.STORAGE_DIR, "download", SourceManager.getTitle(source),
                FileUtils.filterFilename(comic), FileUtils.filterFilename(chapter));
    }

}
