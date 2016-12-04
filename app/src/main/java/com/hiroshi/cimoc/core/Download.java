package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.support.v4.provider.DocumentFile;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.utils.DocumentUtils;
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

    private static final String DOWNLOAD = "download";
    private static final String COMIC_INDEX = "index.cdif";
    private static final String NO_MEDIA = ".nomedia";

    private static void createNoMedia(DocumentFile root) {
        DocumentFile home = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
        DocumentUtils.createFile(home, "", NO_MEDIA);
    }

    private static DocumentFile createComicIndex(DocumentFile root, Comic comic) {
        DocumentFile source = DocumentUtils.getOrCreateSubDirectory(root, String.valueOf(comic.getSource()));
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(source, comic.getCid());
        if (dir != null) {
            return dir.createFile("", COMIC_INDEX);
        }
        return null;
    }

    public static Observable<Void> updateComicIndex(final ContentResolver resolver, final DocumentFile root, final List<Chapter> list, final Comic comic) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    createNoMedia(root);
                    String jsonString = getJsonFromComic(list, comic);
                    DocumentFile file = createComicIndex(root, comic);
                    DocumentUtils.writeStringToFile(resolver, file, "cimoc".concat(jsonString));
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(new Exception());
                }
            }
        }).observeOn(Schedulers.io());
    }

    private static String getJsonFromComic(List<Chapter> list, Comic comic) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("version", "1");
        object.put("type", "comic");
        object.put("source", comic.getSource());
        object.put("cid", comic.getCid());
        object.put("title", comic.getTitle());
        object.put("cover", comic.getCover());
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

    public static DocumentFile updateChapterIndex(ContentResolver resolver, DocumentFile root, Task task) {
        try {
            String jsonString = getJsonFromChapter(task.getTitle(), task.getPath());
            DocumentFile dir1 = DocumentUtils.getOrCreateSubDirectory(root, String.valueOf(task.getSource()));
            DocumentFile dir2 = DocumentUtils.getOrCreateSubDirectory(dir1, task.getCid());
            DocumentFile dir3 = DocumentUtils.getOrCreateSubDirectory(dir2, task.getPath());
            if (dir3 != null) {
                DocumentFile file = dir3.createFile("", COMIC_INDEX);
                DocumentUtils.writeStringToFile(resolver, file, "cimoc".concat(jsonString));
                return dir3;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getJsonFromChapter(String title, String path) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("version", "1");
        object.put("type", "chapter");
        object.put("title", title);
        object.put("path", path);
        return object.toString();
    }

    public static List<String> getComicIndex(final int source, final String comic) {
        String dir = buildPath(source, comic);
        char[] magic = FileUtils.readCharFromFile(dir, "index.cdif", 5);
        if (Arrays.equals(magic, "cimoc".toCharArray())) {
            String jsonString = FileUtils.readSingleLineFromFile(dir, "index.cdif");
            if (jsonString != null) {
                try {
                    return readPathFromJson(jsonString.substring(5));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
