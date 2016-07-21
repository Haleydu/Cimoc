package com.hiroshi.cimoc.core.base;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public abstract class Manga {

    private OkHttpClient client;

    protected int source;
    protected String host;

    public Manga(int source, String host) {
        this.source = source;
        this.host = host;
        this.client = CimocApplication.getHttpClient();
    }

    public void search(String keyword, int page) {
        String url = parseSearchUrl(keyword, page);
        if (url == null) {
            EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, new LinkedList<>()));
        } else {
            Request request = buildRequest(url, null);
            enqueueClient(request, new OnResponseSuccessHandler() {
                @Override
                public void onSuccess(String html) {
                    List<Comic> list = parseSearch(html);
                    EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, list));
                }
            });
        }
    }

    public void into(final Comic comic) {
        Request request = buildRequest(parseIntoUrl(comic.getPath()), null);
        enqueueClient(request, new OnResponseSuccessHandler() {
            @Override
            public void onSuccess(String html) {
                List<Chapter> list = parseInto(html, comic);
                EventBus.getDefault().post(new EventMessage(EventMessage.LOAD_COMIC_SUCCESS, list));
            }
        });
    }

    public static int MODE_INIT = 1;
    public static int MODE_NEXT = 2;
    public static int MODE_PREV = 3;

    public void browse(String path, final int mode) {
        Request request = buildRequest(parseBrowseUrl(path), null);
        enqueueClient(request, new OnResponseSuccessHandler() {
            @Override
            public void onSuccess(String html) {
                String[] images = parseBrowse(html);
                if (images == null) {
                    EventBus.getDefault().post(new EventMessage(EventMessage.PARSE_PIC_FAIL, null));
                } else if (mode == MODE_INIT) {
                    EventBus.getDefault().post(new EventMessage(EventMessage.PARSE_PIC_INIT, images));
                } else if (mode == MODE_NEXT) {
                    EventBus.getDefault().post(new EventMessage(EventMessage.PARSE_PIC_NEXT, images));
                } else {
                    EventBus.getDefault().post(new EventMessage(EventMessage.PARSE_PIC_PREV, images));
                }
            }
        });
    }

    private Request buildRequest(String url, RequestBody body) {
        Request.Builder builder = new Request.Builder().url(url);
        if (body != null) {
            builder.post(body);
        }
        return builder.build();
    }

    public void enqueueClient(Request request, final OnResponseSuccessHandler handler) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                EventBus.getDefault().post(new EventMessage(EventMessage.NETWORK_ERROR, null));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    handler.onSuccess(response.body().string());
                } else {
                    EventBus.getDefault().post(new EventMessage(EventMessage.NETWORK_ERROR, null));
                }
            }
        });
    }

    protected abstract String parseSearchUrl(String keyword, int page);

    protected abstract List<Comic> parseSearch(String html);

    protected abstract String parseIntoUrl(String path);

    protected abstract List<Chapter> parseInto(String html, Comic comic);

    protected abstract String parseBrowseUrl(String path);

    protected abstract String[] parseBrowse(String html);

    private interface OnResponseSuccessHandler {
        void onSuccess(String html);
    }

    protected Comic build(String path, String title, String image, String update, String author, String intro, boolean status) {
        Comic comic = new Comic();
        comic.setSource(source);
        comic.setPath(path);
        comic.setTitle(title);
        comic.setImage(image);
        comic.setUpdate(update);
        comic.setAuthor(author);
        comic.setIntro(intro);
        comic.setStatus(status);
        return comic;
    }

    protected void fill(Comic comic, String title, String image, String update, String author, String intro, Boolean status) {
        comic.setTitle(title);
        comic.setImage(image);
        comic.setUpdate(update);
        comic.setAuthor(author);
        comic.setIntro(intro);
        if (status != null) {
            comic.setStatus(status);
        }
    }

}
