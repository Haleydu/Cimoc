package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.CimocApplication;

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

    public static Observable<String> check() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String url = "http://pan.baidu.com/share/listInRx?uk=223062232&shareid=2388458898&dir=/update";
                OkHttpClient client = CimocApplication.getHttpClient();
                Request request = new Request.Builder().url(url).build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String json = response.body().string();
                        JSONObject object = new JSONObject(json).getJSONArray("listInRx").getJSONObject(0);
                        String version = object.getString("server_filename");
                        subscriber.onNext(version);
                    } else {
                        subscriber.onError(new Exception());
                    }
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(new Exception());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

}
