package com.hiroshi.cimoc.core;

import android.content.ContentResolver;
import android.util.Pair;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.saf.DocumentFile;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.DocumentUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/9.
 */
public class Download {

    /**
     *  version 1 [1.4.3.0, ...)
     *  comic:
     *  {
     *      list: 章节列表 array
     *      [{
     *          title: 章节名称 string
     *          path: 章节路径 string
     *      }]
     *      source: 图源 int
     *      cid: 漫画ID string
     *      title: 标题 string
     *      cover: 封面 string
     *      type: 类型 string ("comic")
     *      version: 版本 string ("1")
     *  }
     *  chapter:
     *  {
     *      title: 章节名称 string
     *      path: 章节路径 string
     *      type: 类型 string ("chapter")
     *      version: 版本 string ("1")
     *  }
     *
     *  version 2 [遥遥无期, 遥遥无期)
     *  comic:
     *  {
     *      list: 章节列表 array
     *      [ 章节路径 string ]
     *      source: 图源 int
     *      cid: 漫画ID string
     *      title: 标题 string
     *      cover: 封面 string
     *      type: 类型 int (1)
     *      version: 版本 int (2)
     *  }
     *  chapter:
     *  {
     *      title: 章节名称 string
     *      path: 章节路径 string
     *      max: 总页数 int
     *      list: 图片列表 array
     *      [ 文件名 string ]
     *      type: 类型 int (2)
     *      version: 版本 int (2)
     *  }
     */

    private static final String JSON_KEY_VERSION = "version";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_TYPE_COMIC = "comic";
    private static final String JSON_KEY_TYPE_CHAPTER = "chapter";
    private static final String JSON_KEY_COMIC_LIST = "list";
    private static final String JSON_KEY_COMIC_SOURCE = "source";
    private static final String JSON_KEY_COMIC_CID = "cid";
    private static final String JSON_KEY_COMIC_TITLE = "title";
    private static final String JSON_KEY_COMIC_COVER = "cover";
    private static final String JSON_KEY_CHAPTER_PATH = "path";
    private static final String JSON_KEY_CHAPTER_TITLE = "title";

    private static final String DOWNLOAD = "download";
    private static final String FILE_INDEX = "index.cdif";
    private static final String NO_MEDIA = ".nomedia";

    private static void createNoMedia(DocumentFile root) {
        DocumentFile home = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
        DocumentUtils.getOrCreateFile(home, NO_MEDIA);
    }

    private static DocumentFile createComicIndex(DocumentFile root, Comic comic) {
        DocumentFile home = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
        DocumentFile source = DocumentUtils.getOrCreateSubDirectory(home, String.valueOf(comic.getSource()));
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(source, comic.getCid());
        if (dir != null) {
            return DocumentUtils.getOrCreateFile(dir, FILE_INDEX);
        }
        return null;
    }

    /**
     * 写漫画索引，不关心是否成功，若没有索引文件，则不能排序章节及扫描恢复漫画，但不影响下载及观看
     * @param list
     * @param comic
     */
    public static void updateComicIndex(ContentResolver resolver, DocumentFile root, List<Chapter> list, Comic comic) {
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
        object.put(JSON_KEY_VERSION, "1");
        object.put(JSON_KEY_TYPE, JSON_KEY_TYPE_COMIC);
        object.put(JSON_KEY_COMIC_SOURCE, comic.getSource());
        object.put(JSON_KEY_COMIC_CID, comic.getCid());
        object.put(JSON_KEY_COMIC_TITLE, comic.getTitle());
        object.put(JSON_KEY_COMIC_COVER, comic.getCover());
        JSONArray array = new JSONArray();
        for (Chapter chapter : list) {
            JSONObject temp = new JSONObject();
            temp.put(JSON_KEY_CHAPTER_TITLE, chapter.getTitle());
            temp.put(JSON_KEY_CHAPTER_PATH, chapter.getPath());
            array.put(temp);
        }
        object.put(JSON_KEY_COMIC_LIST, array);
        return object.toString();
    }

    public static DocumentFile updateChapterIndex(ContentResolver resolver, DocumentFile root, Task task) {
        try {
            String jsonString = getJsonFromChapter(task.getTitle(), task.getPath());
            DocumentFile dir1 = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
            DocumentFile dir2 = DocumentUtils.getOrCreateSubDirectory(dir1, String.valueOf(task.getSource()));
            DocumentFile dir3 = DocumentUtils.getOrCreateSubDirectory(dir2, task.getCid());
            DocumentFile dir4 = DocumentUtils.getOrCreateSubDirectory(dir3, DecryptionUtils.urlDecrypt(task.getPath().replaceAll("/|\\?", "-")));
            if (dir4 != null) {
                DocumentFile file = DocumentUtils.getOrCreateFile(dir4, FILE_INDEX);
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
        object.put(JSON_KEY_VERSION, "1");
        object.put(JSON_KEY_TYPE, JSON_KEY_TYPE_CHAPTER);
        object.put(JSON_KEY_CHAPTER_TITLE, title);
        object.put(JSON_KEY_CHAPTER_PATH, path);
        return object.toString();
    }

    /**
     * 1.4.4 以前位于 存储目录/download/图源名称/漫画名称
     * 1.4.4 以后位于 存储目录/download/图源ID/漫画ID
     * @param root 存储目录
     * @param comic
     * @return
     */
    private static DocumentFile getComicDir(DocumentFile root, Comic comic, String title) {
        DocumentFile result = DocumentUtils.findFile(root, DOWNLOAD, String.valueOf(comic.getSource()), comic.getCid());
        if (result == null) {
            result = DocumentUtils.findFile(root, DOWNLOAD, title, comic.getTitle());
        }
        return result;
    }

    public static List<String> getComicIndex(ContentResolver resolver, DocumentFile root, Comic comic, String title) {
        DocumentFile dir = getComicDir(root, comic, title);
        if (dir != null) {
            DocumentFile file = dir.findFile(FILE_INDEX);
            if (file != null) {
                if (hasMagicNumber(resolver, file)) {
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
        }
        return null;
    }

    private static List<String> readPathFromJson(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        // We use "c" as the key in old version
        JSONArray array = jsonObject.has(JSON_KEY_COMIC_LIST) ? jsonObject.getJSONArray(JSON_KEY_COMIC_LIST) : jsonObject.getJSONArray("c");
        int size = array.length();
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i != size; ++i) {
            JSONObject object = array.getJSONObject(i);
            list.add(object.has(JSON_KEY_CHAPTER_PATH) ? object.getString(JSON_KEY_CHAPTER_PATH) : object.getString("p"));
        }
        return list;
    }

    public static DocumentFile getChapterDir(DocumentFile root, Comic comic, Chapter chapter, String title) {
        DocumentFile result = DocumentUtils.findFile(root, DOWNLOAD, String.valueOf(comic.getSource()),
                comic.getCid(), DecryptionUtils.urlDecrypt(chapter.getPath().replaceAll("/|\\?", "-")));
        if (result == null) {
            result = DocumentUtils.findFile(root, DOWNLOAD, title, comic.getTitle(), chapter.getTitle());
        }
        return result;
    }

    public static Observable<List<ImageUrl>> images(final DocumentFile root, final Comic comic, final Chapter chapter, final String title) {
        return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
            @Override
            public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                DocumentFile dir = getChapterDir(root, comic, chapter, title);
                List<DocumentFile> files = dir.listFiles(new DocumentFile.DocumentFileFilter() {
                    @Override
                    public boolean call(DocumentFile file) {
                        return !file.getName().endsWith("cdif");
                    }
                }, new Comparator<DocumentFile>() {
                    @Override
                    public int compare(DocumentFile lhs, DocumentFile rhs) {
                        return lhs.getName().compareTo(rhs.getName());
                    }
                });

                List<ImageUrl> list = Storage.buildImageUrlFromDocumentFile(files, chapter.getPath(), chapter.getCount());
                if (list.size() != 0) {
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static void delete(DocumentFile root, Comic comic, List<Chapter> list, String title) {
        for (Chapter chapter : list) {
            DocumentFile dir = getChapterDir(root, comic, chapter, title);
            if (dir != null) {
                dir.delete();
            }
        }
    }

    public static void delete(DocumentFile root, Comic comic, String title) {
        DocumentFile dir = getComicDir(root, comic, title);
        if (dir != null) {
            dir.delete();
        }
    }

    private static String getIndexJsonFromDir(ContentResolver resolver, DocumentFile dir) {
        if (dir.isDirectory()) {
            DocumentFile file = dir.findFile(FILE_INDEX);
            if (hasMagicNumber(resolver, file)) {
                String jsonString = DocumentUtils.readLineFromFile(resolver, file);
                if (jsonString != null) {
                    return jsonString.substring(5);
                }
            }
        }
        return null;
    }

    private static Task buildTaskFromDir(ContentResolver resolver, DocumentFile dir) {
        String jsonString = getIndexJsonFromDir(resolver, dir);
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (JSON_KEY_TYPE_CHAPTER.equals(jsonObject.get(JSON_KEY_TYPE))) {
                    int count = DocumentUtils.countWithoutSuffix(dir, "cdif");
                    if (count != 0) {
                        String path = jsonObject.getString(JSON_KEY_CHAPTER_PATH);
                        String title = jsonObject.getString(JSON_KEY_CHAPTER_TITLE);
                        return new Task(null, -1, path, title, count, count);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     *  1.4.3 之后，因为有在章节文件夹内写索引文件，所以恢复起来简单
     *  1.4.3 之前，章节文件夹内没有索引文件，需要比较文件夹名称，有点麻烦，暂不实现
     */
    private static Comic buildComicFromDir(ContentResolver resolver, DocumentFile dir) {
        String jsonString = getIndexJsonFromDir(resolver, dir);
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                if (!JSON_KEY_TYPE_COMIC.equals(jsonObject.get(JSON_KEY_TYPE))) {
                    return null;
                }
                int source = jsonObject.getInt(JSON_KEY_COMIC_SOURCE);
                String title = jsonObject.getString(JSON_KEY_COMIC_TITLE);
                String cid = jsonObject.getString(JSON_KEY_COMIC_CID);
                String cover = jsonObject.getString(JSON_KEY_COMIC_COVER);
                return new Comic(source, cid, title, cover, System.currentTimeMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Observable<Pair<Comic, List<Task>>> scan(final ContentResolver resolver, final DocumentFile root) {
        return Observable.create(new Observable.OnSubscribe<Pair<Comic, List<Task>>>() {
            @Override
            public void call(Subscriber<? super Pair<Comic, List<Task>>> subscriber) {
                root.refresh();
                DocumentFile downloadDir = DocumentUtils.getOrCreateSubDirectory(root, DOWNLOAD);
                if (downloadDir != null) {
                    for (DocumentFile sourceDir : downloadDir.listFiles()) {
                        if (sourceDir.isDirectory()) {
                            for (DocumentFile comicDir : sourceDir.listFiles()) {
                                Comic comic = buildComicFromDir(resolver, comicDir);
                                if (comic != null) {
                                    List<Task> list = new LinkedList<>();
                                    for (DocumentFile chapterDir : comicDir.listFiles()) {
                                        Task task = buildTaskFromDir(resolver, chapterDir);
                                        if (task != null) {
                                            list.add(task);
                                        }
                                    }
                                    if (!list.isEmpty()) {
                                        subscriber.onNext(Pair.create(comic, list));
                                    }
                                }
                            }
                        }
                    }
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    private static boolean hasMagicNumber(ContentResolver resolver, DocumentFile file) {
        if (file != null) {
            char[] magic = DocumentUtils.readCharFromFile(resolver, file, 5);
            return Arrays.equals(magic, "cimoc".toCharArray());
        }
        return false;
    }

}
