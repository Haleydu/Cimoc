package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.Parser;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;

import java.util.List;
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

    public static Observable<List<Comic>> search(final Parser parser, final String keyword, final int page) {
        return create(parser.getSearchRequest(keyword, page),
                new OnResponseSuccessHandler<Comic>() {
                    @Override
                    public List<Comic> onSuccess(String html) {
                        return parser.parseSearch(html, page);
                    }
                });
    }

    public static Observable<List<Chapter>> info(final Parser parser, final Comic comic) {
        return create(parser.getInfoRequest(comic.getCid()),
                new OnResponseSuccessHandler<Chapter>() {
                    @Override
                    public List<Chapter> onSuccess(String html) {
                        return parser.parseInfo(html, comic);
                    }
                });
    }

    public static Observable<List<ImageUrl>> images(final Parser parser, final String cid, final String path) {
        return create(parser.getImagesRequest(cid, path),
                new OnResponseSuccessHandler<ImageUrl>() {
                    @Override
                    public List<ImageUrl> onSuccess(String html) {
                        return parser.parseImages(html);
                    }
                });
    }

    public static Observable<String> load(final Parser parser, final String url) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    Request request = parser.getLazyRequest(url);
                    Response response = mClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String html = response.body().string();
                        String newUrl = parser.parseLazy(html);
                        subscriber.onNext(newUrl);
                    }
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Comic> check(final List<Comic> list) {
        return Observable.create(new Observable.OnSubscribe<Comic>() {
            @Override
            public void call(Subscriber<? super Comic> subscriber) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(500, TimeUnit.MILLISECONDS)
                        .readTimeout(1000, TimeUnit.MILLISECONDS)
                        .build();
                for (Comic comic : list) {
                    int source = comic.getSource();
                    if (source < 100) {
                        Parser parser = SourceManager.getParser(source);
                        try {
                            Request request = parser.getCheckRequest(comic.getCid());
                            Response response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                String html = response.body().string();
                                String update = parser.parseCheck(html);
                                if (!comic.getUpdate().equals(update)) {
                                    comic.setUpdate(update);
                                    comic.setHighlight(true);
                                    subscriber.onNext(comic);
                                    continue;
                                }
                            }
                            response.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    subscriber.onNext(null);
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
                    Response response = mClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String html = response.body().string();
                        List<T> list = handler.onSuccess(html);
                        if (list.isEmpty()) {
                            subscriber.onError(new EmptyResultException());
                        } else {
                            subscriber.onNext(list);
                            subscriber.onCompleted();
                        }
                    } else {
                        subscriber.onError(new ParseErrorException());
                    }
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(new NetworkErrorException());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private interface OnResponseSuccessHandler<T> {
        List<T> onSuccess(String html);
    }

    public static class ParseErrorException extends Exception {}

    public static class NetworkErrorException extends Exception {}

    public static class EmptyResultException extends Exception {}

}
