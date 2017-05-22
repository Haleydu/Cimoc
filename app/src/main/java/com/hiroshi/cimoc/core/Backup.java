package com.hiroshi.cimoc.core;

import android.content.ContentResolver;

import com.hiroshi.cimoc.misc.Pair;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.saf.DocumentFile;
import com.hiroshi.cimoc.utils.DocumentUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class Backup {

    private static final String BACKUP = "backup";

    // before 1.4.3
    private static final String SUFFIX_CIMOC = "cimoc";

    // cfbf = Cimoc Favorite Backup File
    private static final String SUFFIX_CFBF = "cfbf";

    // ctbf = Cimoc Tag Backup File
    private static final String SUFFIX_CTBF = "ctbf";

    private static final String JSON_CIMOC_KEY_COMIC_SOURCE = "s";
    private static final String JSON_CIMOC_KEY_COMIC_CID = "i";
    private static final String JSON_CIMOC_KEY_COMIC_TITLE = "t";
    private static final String JSON_CIMOC_KEY_COMIC_COVER = "c";
    private static final String JSON_CIMOC_KEY_COMIC_UPDATE = "u";
    private static final String JSON_CIMOC_KEY_COMIC_FINISH = "f";
    private static final String JSON_CIMOC_KEY_COMIC_LAST = "l";
    private static final String JSON_CIMOC_KEY_COMIC_PAGE = "p";

    private static final String JSON_KEY_VERSION = "version";
    private static final String JSON_KEY_TAG_OBJECT = "tag";
    private static final String JSON_KEY_TAG_TITLE = "title";
    private static final String JSON_KEY_COMIC_ARRAY = "comic";
    private static final String JSON_KEY_COMIC_SOURCE = "source";
    private static final String JSON_KEY_COMIC_CID = "cid";
    private static final String JSON_KEY_COMIC_TITLE = "title";
    private static final String JSON_KEY_COMIC_COVER = "cover";
    private static final String JSON_KEY_COMIC_UPDATE = "update";
    private static final String JSON_KEY_COMIC_FINISH = "finish";
    private static final String JSON_KEY_COMIC_LAST = "last";
    private static final String JSON_KEY_COMIC_PAGE = "page";

    public static Observable<String[]> loadFavorite(DocumentFile root) {
        return load(root, SUFFIX_CIMOC, SUFFIX_CFBF);
    }

    public static Observable<String[]> loadTag(DocumentFile root) {
        return load(root, SUFFIX_CTBF);
    }

    private static Observable<String[]> load(final DocumentFile root, final String... suffix) {
        return Observable.create(new Observable.OnSubscribe<String[]>() {
            @Override
            public void call(Subscriber<? super String[]> subscriber) {
                DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, BACKUP);
                if (dir != null) {
                    String[] files = DocumentUtils.listFilesWithSuffix(dir, suffix);
                    if (files.length != 0) {
                        Arrays.sort(files);
                        subscriber.onNext(files);
                        subscriber.onCompleted();
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

    public static void saveFavoriteAuto(ContentResolver resolver, DocumentFile root, List<Comic> list) {
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, BACKUP);
        if (dir != null) {
            try {
                JSONObject result = new JSONObject();
                result.put(JSON_KEY_VERSION, 1);
                result.put(JSON_KEY_COMIC_ARRAY, buildComicArray(list));
                DocumentFile file = DocumentUtils.getOrCreateFile(dir, "automatic.".concat(SUFFIX_CFBF));
                DocumentUtils.writeStringToFile(resolver, file, result.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int saveFavorite(ContentResolver resolver, DocumentFile root, List<Comic> list) {
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, BACKUP);
        if (dir != null) {
            try {
                JSONObject result = new JSONObject();
                result.put(JSON_KEY_VERSION, 1);
                result.put(JSON_KEY_COMIC_ARRAY, buildComicArray(list));
                String filename = StringUtils.getDateStringWithSuffix(SUFFIX_CFBF);
                DocumentFile file = DocumentUtils.getOrCreateFile(dir, filename);
                DocumentUtils.writeStringToFile(resolver, file, result.toString());
                return list.size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static int saveTag(final ContentResolver resolver, final DocumentFile root, final Tag tag, final List<Comic> list) {
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, BACKUP);
        if (dir != null) {
            try {
                JSONObject result = new JSONObject();
                result.put(JSON_KEY_VERSION, 1);
                result.put(JSON_KEY_TAG_OBJECT, buildTagObject(tag));
                result.put(JSON_KEY_COMIC_ARRAY, buildComicArray(list));
                String filename = tag.getTitle().concat(".").concat(SUFFIX_CTBF);
                DocumentFile file = DocumentUtils.getOrCreateFile(dir, filename);
                DocumentUtils.writeStringToFile(resolver, file, result.toString());
                return list.size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private static JSONObject buildTagObject(Tag tag) throws JSONException {
        JSONObject object = new JSONObject();
        object.put(JSON_KEY_TAG_TITLE, tag.getTitle());
        return object;
    }

    private static JSONArray buildComicArray(List<Comic> list) throws JSONException {
        JSONArray array = new JSONArray();
        for (Comic comic : list) {
            JSONObject object = new JSONObject();
            object.put(JSON_KEY_COMIC_SOURCE, comic.getSource());
            object.put(JSON_KEY_COMIC_CID, comic.getCid());
            object.put(JSON_KEY_COMIC_TITLE, comic.getTitle());
            object.put(JSON_KEY_COMIC_COVER, comic.getCover());
            object.put(JSON_KEY_COMIC_UPDATE, comic.getUpdate());
            object.put(JSON_KEY_COMIC_FINISH, comic.getFinish());
            object.put(JSON_KEY_COMIC_LAST, comic.getLast());
            object.put(JSON_KEY_COMIC_PAGE, comic.getPage());
            array.put(object);
        }
        return array;
    }

    private static String readBackupFile(ContentResolver resolver, DocumentFile root, String filename) {
        DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(root, BACKUP);
        if (dir != null) {
            DocumentFile file = dir.findFile(filename);
            return DocumentUtils.readLineFromFile(resolver, file);
        }
        return null;
    }

    public static Observable<Pair<String, List<Comic>>> restoreTag(final ContentResolver resolver, final DocumentFile root, final String filename) {
        return Observable.create(new Observable.OnSubscribe<Pair<String, List<Comic>>>() {
            @Override
            public void call(Subscriber<? super Pair<String, List<Comic>>> subscriber) {
                String jsonString = readBackupFile(resolver, root, filename);
                try {
                    JSONObject object = new JSONObject(jsonString);
                    List<Comic> list = loadComicArray(object.getJSONArray(JSON_KEY_COMIC_ARRAY), SUFFIX_CTBF);
                    subscriber.onNext(Pair.create(object.getJSONObject(JSON_KEY_TAG_OBJECT).getString(JSON_KEY_TAG_TITLE), list));
                    subscriber.onCompleted();
                } catch (JSONException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Comic>> restoreFavorite(final ContentResolver resolver, final DocumentFile root, final String filename) {
        return Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                List<Comic> list = new LinkedList<>();
                String jsonString = readBackupFile(resolver, root, filename);
                try {
                    if (filename.endsWith(SUFFIX_CIMOC)) {
                        list.addAll(loadComicArray(new JSONArray(jsonString), SUFFIX_CIMOC));
                    } else if (filename.endsWith(SUFFIX_CFBF)) {
                        JSONObject object = new JSONObject(jsonString);
                        list.addAll(loadComicArray(object.getJSONArray(JSON_KEY_COMIC_ARRAY), SUFFIX_CFBF));
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } catch (JSONException e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private static List<Comic> loadComicArray(JSONArray array, String suffix) throws JSONException {
        List<Comic> list = new LinkedList<>();
        switch (suffix) {
            case SUFFIX_CIMOC:
                for (int i = 0; i != array.length(); ++i) {
                    JSONObject object = array.getJSONObject(i);
                    int source = object.getInt(JSON_CIMOC_KEY_COMIC_SOURCE);
                    String cid = object.getString(JSON_CIMOC_KEY_COMIC_CID);
                    String title = object.getString(JSON_CIMOC_KEY_COMIC_TITLE);
                    String cover = object.getString(JSON_CIMOC_KEY_COMIC_COVER);
                    String update = object.optString(JSON_CIMOC_KEY_COMIC_UPDATE, null);
                    Boolean finish = object.has(JSON_CIMOC_KEY_COMIC_FINISH) ? object.getBoolean(JSON_CIMOC_KEY_COMIC_FINISH) : null;
                    String last = object.optString(JSON_CIMOC_KEY_COMIC_LAST, null);
                    Integer page = object.has(JSON_CIMOC_KEY_COMIC_PAGE) ? object.getInt(JSON_CIMOC_KEY_COMIC_PAGE) : null;
                    list.add(new Comic(null, source, cid, title, cover, false, false, update, finish, null, null, null, last, page));
                }
                break;
            case SUFFIX_CFBF:
            case SUFFIX_CTBF:
                for (int i = 0; i != array.length(); ++i) {
                    JSONObject object = array.getJSONObject(i);
                    int source = object.getInt(JSON_KEY_COMIC_SOURCE);
                    String cid = object.getString(JSON_KEY_COMIC_CID);
                    String title = object.getString(JSON_KEY_COMIC_TITLE);
                    String cover = object.getString(JSON_KEY_COMIC_COVER);
                    String update = object.optString(JSON_KEY_COMIC_UPDATE, null);
                    Boolean finish = object.has(JSON_KEY_COMIC_FINISH) ? object.getBoolean(JSON_KEY_COMIC_FINISH) : null;
                    String last = object.optString(JSON_KEY_COMIC_LAST, null);
                    Integer page = object.has(JSON_KEY_COMIC_PAGE) ? object.getInt(JSON_KEY_COMIC_PAGE) : null;
                    list.add(new Comic(null, source, cid, title, cover, false, false, update, finish, null, null, null, last, page));
                }
                break;
        }
        return list;
    }

}
