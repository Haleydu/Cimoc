package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.Parser;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.rx.RxObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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

    private static OkHttpClient mClient = CimocApplication.getHttpClient();

    public static Observable<Comic> search(final int source, final String keyword, final int page) {
        return Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                Parser parser = SourceManager.getParser(source);
                Request request = parser.getSearchRequest(keyword, page);
                Random random = new Random();
                try {
                    String html = getResponseBody(mClient, request);
                    SearchIterator iterator = parser.getSearchIterator(html, page);
                    if (iterator == null || iterator.empty()) {
                        subscriber.onError(new EmptyResultException());
                    } else {
                        while (iterator.hasNext()) {
                            subscriber.onNext(iterator.next());
                            Thread.sleep(random.nextInt(200));
                        }
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Chapter>> info(final int source, final Comic comic) {
        final Parser parser = SourceManager.getParser(source);
        return Observable.create(new Observable.OnSubscribe<List<Chapter>>() {
            @Override
            public void call(Subscriber<? super List<Chapter>> subscriber) {
                try {
                    Request request = parser.getInfoRequest(comic.getCid());
                    String html = getResponseBody(mClient, request);
                    String msg = parser.parseInfo(html, comic);
                    if (msg != null) {
                        request = parser.getChapterRequest(msg);
                        html = getResponseBody(mClient, request);
                    }
                    List<Chapter> list = parser.parseChapter(html);
                    if (list.isEmpty()) {
                        subscriber.onError(new EmptyResultException());
                    } else {
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<Comic>> recent(final int source, final int page) {
        final Parser parser = SourceManager.getParser(source);
        return create(parser.getRecentRequest(page),
                new OnResponseSuccessHandler<Comic>() {
                    @Override
                    public List<Comic> onSuccess(String html) {
                        return parser.parseRecent(html, page);
                    }
                });
    }

    public static Observable<List<ImageUrl>> images(final int source, final String cid, final String path) {
        final Parser parser = SourceManager.getParser(source);
        return create(parser.getImagesRequest(cid, path),
                new OnResponseSuccessHandler<ImageUrl>() {
                    @Override
                    public List<ImageUrl> onSuccess(String html) {
                        beforeImages(parser);
                        return parser.parseImages(html);
                    }
                });
    }

    private static void beforeImages(Parser parser) {
        Request request = parser.getBeforeImagesRequest();
        if (request != null) {
            try {
                parser.beforeImages(getResponseBody(mClient, request));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ImageUrl> downloadImages(OkHttpClient client, int source, String cid, String path) {
        List<ImageUrl> list = new LinkedList<>();
        Parser parser = SourceManager.getParser(source);
        beforeImages(parser);
        Request request = parser.getImagesRequest(cid, path);
        try {
            list = parser.parseImages(getResponseBody(client, request));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String downloadLazy(OkHttpClient client, int source, String url) {
        Parser parser = SourceManager.getParser(source);
        Request request = parser.getLazyRequest(url);
        try {
            return parser.parseLazy(getResponseBody(client, request), url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Observable<String> load(final int source, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Parser parser = SourceManager.getParser(source);
                Request request = parser.getLazyRequest(url);
                String newUrl = null;
                try {
                    newUrl = parser.parseLazy(getResponseBody(mClient, request), url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subscriber.onNext(newUrl);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<RxObject> check(final List<Comic> list) {
        return Observable.create(new Observable.OnSubscribe<RxObject>() {
            @Override
            public void call(Subscriber<? super RxObject> subscriber) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(500, TimeUnit.MILLISECONDS)
                        .readTimeout(1000, TimeUnit.MILLISECONDS)
                        .build();
                int size = list.size();
                int count = 0;
                for (Comic comic : list) {
                    int source = comic.getSource();
                    if (source < 100) {
                        Parser parser = SourceManager.getParser(source);
                        Request request = parser.getCheckRequest(comic.getCid());
                        try {
                            String update = parser.parseCheck(getResponseBody(client, request));
                            if (comic.getUpdate() != null && !comic.getUpdate().equals(update)) {
                                comic.setUpdate(update);
                                comic.setHighlight(true);
                                subscriber.onNext(new RxObject(comic, ++count, size));
                                continue;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    subscriber.onNext(new RxObject(null, ++count, size));
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    private static <T> Observable<List<T>> create(final Request request, final OnResponseSuccessHandler<T> handler) {
        return Observable.create(new Observable.OnSubscribe<List<T>>() {
            @Override
            public void call(Subscriber<? super List<T>> subscriber) {
                try {
                    List<T> list = handler.onSuccess(getResponseBody(mClient, request));
                    if (list.isEmpty()) {
                        subscriber.onError(new EmptyResultException());
                    } else {
                        subscriber.onNext(list);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private static String getResponseBody(OkHttpClient client, Request request) throws NetworkErrorException, EmptyResultException {
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new EmptyResultException();
            }
        } catch (IOException e){
            e.printStackTrace();
            throw new NetworkErrorException();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private interface OnResponseSuccessHandler<T> {
        List<T> onSuccess(String html);
    }

    public static class ParseErrorException extends Exception {}

    public static class NetworkErrorException extends Exception {}

    public static class EmptyResultException extends Exception {}

}
