package com.hiroshi.cimoc.core.base;

import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.cimoc.utils.YuriClient;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public abstract class BaseSearch {

    protected YuriClient client;

    private int page;
    private String keyword;

    public BaseSearch() {
        client = YuriClient.getInstance();
    }

    public void first(String keyword) {
        this.page = 1;
        this.keyword = keyword;
        search(keyword, 1);
    }

    public void next() {
        search(keyword, ++page);
    }

    private void search(String keyword, int page) {
        String url = getUrl(keyword, page);
        client.enqueue(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    List<Comic> list = parse(html);
                    if (list.isEmpty()) {
                        EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_EMPTY, null));
                    } else {
                        EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, list));
                    }
                }
            }
        });
    }

    protected abstract List<Comic> parse(String html);

    protected abstract String getUrl(String keyword, int page);

}
