package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.DocumentUtils;

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
        DocumentFile home = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
        DocumentFile source = DocumentUtils.getOrCreateSubDirectory(home, String.valueOf(comic.getSource()));
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(source, comic.getCid());
        if (dir != null) {
            return DocumentUtils.createFile(dir, "", COMIC_INDEX);
        }
        return null;
    }

    /**
     * 写漫画索引，不关心是否成功，若没有索引文件，则不能排序章节及扫描恢复漫画，但不影响下载及观看
     * @param list
     * @param comic
     */
    public static void updateComicIndex(List<Chapter> list, Comic comic) {
        ContentResolver resolver = CimocApplication.getResolver();
        DocumentFile root = CimocApplication.getDocumentFile();
        try {
            createNoMedia(root);
            String jsonString = getJsonFromComic(list, comic);
            DocumentFile file = createComicIndex(root, comic);
            DocumentUtils.writeStringToFile(resolver, file, "cimoc".concat(jsonString));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static DocumentFile updateChapterIndex(Task task) {
        ContentResolver resolver = CimocApplication.getResolver();
        DocumentFile root = CimocApplication.getDocumentFile();
        try {
            String jsonString = getJsonFromChapter(task.getTitle(), task.getPath());
            DocumentFile dir1 = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
            DocumentFile dir2 = DocumentUtils.getOrCreateSubDirectory(dir1, String.valueOf(task.getSource()));
            DocumentFile dir3 = DocumentUtils.getOrCreateSubDirectory(dir2, task.getCid());
            DocumentFile dir4 = DocumentUtils.getOrCreateSubDirectory(dir3, task.getPath());
            if (dir4 != null) {
                DocumentFile file = dir4.createFile("", COMIC_INDEX);
                DocumentUtils.writeStringToFile(resolver, file, "cimoc".concat(jsonString));
                return dir4;
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

    /**
     * 1.4.4 以前位于 存储目录/download/图源名称/漫画名称
     * 1.4.4 以后位于 存储目录/download/图源ID/漫画ID
     * @param root 存储目录
     * @param comic
     * @return
     */
    private static DocumentFile getComicDir(DocumentFile root, Comic comic) {
        DocumentFile result = DocumentUtils.findFile(root, DOWNLOAD, String.valueOf(comic.getSource()), comic.getCid());
        if (result == null) {
            result = DocumentUtils.findFile(root, DOWNLOAD, SourceManager.getTitle(comic.getSource()), comic.getTitle());
        }
        return result;
    }

    public static List<String> getComicIndex(Comic comic) {
        ContentResolver resolver = CimocApplication.getResolver();
        DocumentFile root = CimocApplication.getDocumentFile();
        DocumentFile dir = getComicDir(root, comic);
        DocumentFile file = dir.findFile(COMIC_INDEX);
        if (file != null) {
            char[] magic = DocumentUtils.readCharFromFile(resolver, file, 5);
            if (Arrays.equals(magic, "cimoc".toCharArray())) {
                String jsonString = DocumentUtils.readLineFromFile(resolver, file);
                if (jsonString != null) {
                    try {
                        return readPathFromJson(jsonString.substring(5));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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

    private static DocumentFile getChapterDir(DocumentFile root, Comic comic, Chapter chapter) {
        DocumentFile result = DocumentUtils.findFile(root, DOWNLOAD, String.valueOf(comic.getSource()), comic.getCid(), chapter.getPath());
        if (result == null) {
            result = DocumentUtils.findFile(root, DOWNLOAD, SourceManager.getTitle(comic.getSource()), comic.getTitle(), chapter.getTitle());
        }
        return result;
    }

    public static Observable<List<ImageUrl>> images(final Comic comic, final Chapter chapter) {
        return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
            @Override
            public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                DocumentFile root = CimocApplication.getDocumentFile();
                DocumentFile dir = getChapterDir(root, comic, chapter);
                Uri[] uris = DocumentUtils.listUrisWithoutSuffix(dir, ".cdif");
                if (uris.length != 0) {
                    List<ImageUrl> list = new ArrayList<>(uris.length);
                    for (int i = 0; i < uris.length; ++i) {
                        String uri = uris[i].toString();
                        if (uri.startsWith("file")) {   // content:// 解码会出错 file:// 中文路径不解码 Fresco 读取不了
                            uri = DecryptionUtils.urlDecrypt(uri);
                        }
                        list.add(new ImageUrl(i + 1, uri, false));
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

    public static void delete(Comic comic, List<Chapter> list) {
        DocumentFile root = CimocApplication.getDocumentFile();
        for (Chapter chapter : list) {
            DocumentFile dir = getChapterDir(root, comic, chapter);
            if (dir != null) {
                DocumentUtils.deleteDir(dir);
            }
        }
    }

    public static void delete(Comic comic) {
        DocumentFile root = CimocApplication.getDocumentFile();
        DocumentFile dir = getComicDir(root, comic);
        if (dir != null) {
            DocumentUtils.deleteDir(dir);
        }
    }

}
