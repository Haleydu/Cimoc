package com.hiroshi.cimoc.core.base;

import android.util.Log;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.cimoc.utils.YuriClient;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public abstract class BaseManga {

    protected YuriClient client;

    public BaseManga() {
        client = YuriClient.getInstance();
    }

    public void get(String url) {
        client.enqueue(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    List<Chapter> list = new LinkedList<>();
                    Comic comic = parse(html, list);
                    EventBus.getDefault().post(new EventMessage(EventMessage.LOAD_COMIC_SUCCESS, comic, list));
                }
            }
        });
    }

    protected abstract Comic parse(String html, List<Chapter> list);

}
