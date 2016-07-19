package com.hiroshi.cimoc.core.base;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public abstract class Manga {

    private OkHttpClient client;

    public Manga() {
        client = CimocApplication.getHttpClient();
    }

    public void search(String keyword, int page) {
        String url = parseSearchUrl(keyword, page);
        if (url == null) {
            EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, new LinkedList<>()));
        } else {
            enqueueClient(url, new OnResponseSuccessHandler() {
                @Override
                public void onSuccess(String html) {
                    List<Comic> list = parseSearch(html);
                    EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, list));
                }
            });
        }
    }

    public void into(String path) {
        String url = parseIntoUrl(path);
        enqueueClient(url, new OnResponseSuccessHandler() {
            @Override
            public void onSuccess(String html) {
                List<Chapter> list = new LinkedList<>();
                Comic comic = parseInto(html, list);
                EventBus.getDefault().post(new EventMessage(EventMessage.LOAD_COMIC_SUCCESS, comic, list));
            }
        });
    }

    public static int MODE_INIT = 1;
    public static int MODE_NEXT = 2;
    public static int MODE_PREV = 3;

    public void browse(String path, final int mode) {
        String url = parseBrowseUrl(path);
        enqueueClient(url, new OnResponseSuccessHandler() {
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

    public void enqueueClient(String url, final OnResponseSuccessHandler handler) {
        Request request = new Request.Builder()
                .url(url)
                .build();
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

    protected abstract Comic parseInto(String html, List<Chapter> list);

    protected abstract String parseBrowseUrl(String path);

    protected abstract String[] parseBrowse(String html);

    private interface OnResponseSuccessHandler {
        void onSuccess(String html);
    }

}
