package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.App;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/8/24.
 */
public class Update {

    private static final String UPDATE_URL = "http://pan.baidu.com/share/list?uk=223062232&shareid=2388458898&dir=/update";
    private static final String SERVER_FILENAME = "server_filename";
    private static final String LIST = "list";

    public static Observable<String> check() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                OkHttpClient client = App.getHttpClient();
                Request request = new Request.Builder().url(UPDATE_URL).build();
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        JSONObject object = new JSONObject(json).getJSONArray(LIST).getJSONObject(0);
                        String version = object.getString(SERVER_FILENAME);
                        subscriber.onNext(version);
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
                subscriber.onError(new Exception());
            }
        }).subscribeOn(Schedulers.io());
    }

}
