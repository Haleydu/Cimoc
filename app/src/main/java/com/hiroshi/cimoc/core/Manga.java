package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.manager.ChapterManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.parser.Parser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class Manga {

    private static boolean indexOfIgnoreCase(String str, String search) {
        return str.toLowerCase().indexOf(search.toLowerCase()) != -1;
    }

    public static Observable<Comic> getSearchResult(final Parser parser, final String keyword, final int page, final boolean strictSearch) {
        return Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                try {
                    Request request = parser.getSearchRequest(keyword, page);
                    Random random = new Random();
                    String html = getResponseBody(App.getHttpClient(), request);
                    SearchIterator iterator = parser.getSearchIterator(html, page);
                    if (iterator == null || iterator.empty()) {
                        throw new Exception();
                    }
                    while (iterator.hasNext()) {
                        Comic comic = iterator.next();
//                        if (comic != null && (comic.getTitle().indexOf(keyword) != -1 || comic.getAuthor().indexOf(keyword) != -1)) {
                        if (comic != null
                                && (indexOfIgnoreCase(comic.getTitle(), keyword)
                                || indexOfIgnoreCase(comic.getAuthor(), keyword)
                                || (!strictSearch))) {
                            subscriber.onNext(comic);
                            Thread.sleep(random.nextInt(200));
                        }
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Chapter>> getComicInfo(final Parser parser, final Comic comic) {
        return Observable.create(new Observable.OnSubscribe<List<Chapter>>() {
            @Override
            public void call(Subscriber<? super List<Chapter>> subscriber) {
                try {
//                    Mongo mongo = new Mongo();
                    List<Chapter> list = new ArrayList<>();

//                    list.addAll(mongo.QueryComicBase(comic));
                    if (list.isEmpty()) {
                        comic.setUrl(parser.getUrl(comic.getCid()));
                        Request request = parser.getInfoRequest(comic.getCid());
                        String html = getResponseBody(App.getHttpClient(), request);
                        Comic newComic = parser.parseInfo(html, comic);
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UPDATE_INFO, newComic));
                        request = parser.getChapterRequest(html, comic.getCid());
                        if (request != null) {
                            html = getResponseBody(App.getHttpClient(), request);
                        }
                        Long sourceComic = Long.parseLong(comic.getSource() + "000" + (comic.getId() == null ? "00" : comic.getId()));
                        list = parser.parseChapter(html, comic, sourceComic);
                        if (list == null) {
                            list = parser.parseChapter(html);
                        }
//                        mongo.UpdateComicBase(comic, list);
                    }
                    if (!list.isEmpty()) {
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    } else {
                        throw new ParseErrorException();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Comic>> getCategoryComic(final Parser parser, final String format,
                                                           final int page) {
        return Observable.create(new Observable.OnSubscribe<List<Comic>>() {
            @Override
            public void call(Subscriber<? super List<Comic>> subscriber) {
                try {
                    Request request = parser.getCategoryRequest(format, page);
                    String html = getResponseBody(App.getHttpClient(), request);
                    List<Comic> list = parser.parseCategory(html, page);
                    if (!list.isEmpty()) {
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<ImageUrl>> getChapterImage(final Chapter chapter,
                                                             final Parser parser,
                                                             final String cid,
                                                             final String path) {
        return Observable.create(new Observable.OnSubscribe<List<ImageUrl>>() {
            @Override
            public void call(Subscriber<? super List<ImageUrl>> subscriber) {
                String html;
//                Mongo mongo = new Mongo();
                List<ImageUrl> list = new ArrayList<>();
                try {
//                    List<ImageUrl> listdoc = new ArrayList<>();
//                    list.addAll(mongo.QueryComicChapter(mComic, path));
                    if (list.isEmpty()) {
                        Request request = parser.getImagesRequest(cid, path);
                        html = getResponseBody(App.getHttpClient(), request);
                        list = parser.parseImages(html,chapter);
                        if (list == null || list.size()==0) {
                            list = parser.parseImages(html);
                        }
//                        if (!list.isEmpty()) {
//                            mongo.InsertComicChapter(mComic, path, list);
//                        }
                    }

                    if (list.isEmpty()) {
                        throw new Exception();
                    } else {
                        for (ImageUrl imageUrl : list) {
                            imageUrl.setChapter(path);
                        }
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static List<ImageUrl> getImageUrls(Parser parser, int source, String cid, String path, String title, ChapterManager mChapterManager) throws InterruptedIOException {
        List<ImageUrl> list = new ArrayList<>();
//        Mongo mongo = new Mongo();
        Response response = null;
        try {
//            list.addAll(mongo.QueryComicChapter(source, cid, path));
            if (!list.isEmpty()) {
                return list;
            }
            Request request = parser.getImagesRequest(cid, path);
            response = App.getHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                List<Chapter> chapter = mChapterManager.getChapter(path, title);
                if (chapter != null && chapter.size() >= 1) {
                    list.addAll(parser.parseImages(response.body().string(), chapter.get(0)));
                }
                if (list.size() == 0) {
                    list.addAll(parser.parseImages(response.body().string()));
                }
//                mongo.InsertComicChapter(source, cid, path, list);
            } else {
                throw new NetworkErrorException();
            }
        } catch (InterruptedIOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return list;
    }

    public static String getLazyUrl(Parser parser, String url) throws InterruptedIOException {
        Response response = null;
        try {
            Request request = parser.getLazyRequest(url);
            response = App.getHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                return parser.parseLazy(response.body().string(), url);
            } else {
                throw new NetworkErrorException();
            }
        } catch (InterruptedIOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    public static Observable<String> loadLazyUrl(final Parser parser, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Request request = parser.getLazyRequest(url);
                String newUrl = null;
                try {
                    newUrl = parser.parseLazy(getResponseBody(App.getHttpClient(), request), url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subscriber.onNext(newUrl);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<String>> loadAutoComplete(final String keyword) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
//                RequestBody body = new FormBody.Builder()
//                        .add("key", keyword)
//                        .add("s", "1")
//                        .build();
//                Request request = new Request.Builder()
//                        .url("http://m.ikanman.com/support/word.ashx")
//                        .post(body)
//                        .build();
                Request request = new Request.Builder()
                        .url("http://m.ac.qq.com/search/smart?word=" + keyword)
                        .build();
                try {
                    String jsonString = getResponseBody(App.getHttpClient(), request);
//                    JSONArray array = new JSONArray(jsonString);
                    JSONObject jsonObject = new JSONObject(jsonString);
                    JSONArray array = jsonObject.getJSONArray("data");
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i != array.length(); ++i) {
//                        list.add(array.getJSONObject(i).getString("t"));
                        list.add(array.getJSONObject(i).getString("title"));
                    }
                    subscriber.onNext(list);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Comic> checkUpdate(
            final SourceManager manager, final List<Comic> list) {
        return Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(1500, TimeUnit.MILLISECONDS)
                        .readTimeout(1500, TimeUnit.MILLISECONDS)
                        .build();
                for (Comic comic : list) {
                    try {
                        Parser parser = manager.getParser(comic.getSource());
                        Request request = parser.getCheckRequest(comic.getCid());
                        String update = parser.parseCheck(getResponseBody(client, request));
                        if (comic.getUpdate() != null && update != null && !comic.getUpdate().equals(update)) {
                            comic.setFavorite(System.currentTimeMillis());
                            comic.setUpdate(update);
                            comic.setHighlight(true);
                            subscriber.onNext(comic);
                            continue;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    subscriber.onNext(null);
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static String getResponseBody(OkHttpClient client, Request request) throws NetworkErrorException {
        return getResponseBody(client, request, true);
    }

    private static String getResponseBody(OkHttpClient client, Request request, boolean retry) throws NetworkErrorException {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                byte[] bodybytes = response.body().bytes();
                String body = new String(bodybytes);
                Matcher m = Pattern.compile("charset=([\\w\\-]+)").matcher(body);
                if (m.find()) {
                    body = new String(bodybytes, m.group(1));
                }
                return body;
            } else if (retry)
                return getResponseBody(client, request, false);
        } catch (Exception e) {
            e.printStackTrace();
            if (retry)
                return getResponseBody(client, request, false);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        throw new NetworkErrorException();
    }

    public static class ParseErrorException extends Exception {
    }

    public static class NetworkErrorException extends Exception {
    }

}
