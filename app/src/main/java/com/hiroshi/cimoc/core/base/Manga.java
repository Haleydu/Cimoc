package com.hiroshi.cimoc.core.base;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
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

    protected int source;
    protected String host;

    public Manga(int source, String host) {
        this.source = source;
        this.host = host;
        this.client = CimocApplication.getHttpClient();
    }

    public void search(String keyword, int page) {
        Request request = buildSearchRequest(keyword, page);
        if (request == null) {
            EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_FAIL, null));
        } else {
            enqueueClient(request, new OnResponseSuccessHandler() {
                @Override
                public void onSuccess(String html) {
                    List<Comic> list = parseSearch(html);
                    if (list == null || list.isEmpty()) {
                        EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_FAIL, null));
                    } else {
                        EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, list));
                    }
                }
            });
        }
    }

    public void into(final Comic comic) {
        enqueueClient(buildIntoRequest(comic.getCid()), new OnResponseSuccessHandler() {
            @Override
            public void onSuccess(String html) {
                List<Chapter> list = parseInto(html, comic);
                if (list == null || list.isEmpty()) {
                    EventBus.getDefault().post(new EventMessage(EventMessage.LOAD_COMIC_FAIL, null));
                } else {
                    EventBus.getDefault().post(new EventMessage(EventMessage.LOAD_COMIC_SUCCESS, list));
                }
            }
        });
    }

    public void browse(String cid, String path) {
        enqueueClient(buildBrowseRequest(cid, path), new OnResponseSuccessHandler() {
            @Override
            public void onSuccess(String html) {
                String[] images = parseBrowse(html);
                if (images == null) {
                    EventBus.getDefault().post(new EventMessage(EventMessage.PARSE_PIC_FAIL, null));
                } else {
                    EventBus.getDefault().post(new EventMessage(EventMessage.PARSE_PIC_SUCCESS, images));
                }
            }
        });
    }

    public void cancel() {
        client.dispatcher().cancelAll();
    }

    private void enqueueClient(Request request, final OnResponseSuccessHandler handler) {
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

    protected String execute(String url) {
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract Request buildSearchRequest(String keyword, int page);

    protected abstract List<Comic> parseSearch(String html);

    protected abstract Request buildIntoRequest(String cid);

    protected abstract List<Chapter> parseInto(String html, Comic comic);

    protected abstract Request buildBrowseRequest(String cid, String path);

    protected abstract String[] parseBrowse(String html);

    private interface OnResponseSuccessHandler {
        void onSuccess(String html);
    }

}
