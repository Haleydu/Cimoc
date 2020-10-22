package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.core.Update;
import com.hiroshi.cimoc.core.UpdateJson;
import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.MainView;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private ComicManager mComicManager;
    private static final String APP_VERSIONNAME = "versionName";
    private static final String APP_VERSIONCODE = "versionCode";
    private static final String APP_CONTENT = "content";
    private static final String APP_MD5 = "md5";
    private static final String APP_URL= "url";

    private static final String SOURCE_URL = "https://raw.githubusercontent.com/Haleydu/update/master/sourceBaseUrl.json";

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                MiniComic comic = (MiniComic) rxEvent.getData();
                mBaseView.onLastChange(comic.getId(), comic.getSource(), comic.getCid(),
                        comic.getTitle(), comic.getCover());
            }
        });
    }

    public boolean checkLocal(long id) {
        Comic comic = mComicManager.load(id);
        return comic != null && comic.getLocal();
    }

    public void loadLast() {
        mCompositeSubscription.add(mComicManager.loadLast()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Comic>() {
                    @Override
                    public void call(Comic comic) {
                        if (comic != null) {
                            mBaseView.onLastLoadSuccess(comic.getId(), comic.getSource(), comic.getCid(), comic.getTitle(), comic.getCover());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onLastLoadFail();
                    }
                }));
    }

    public void checkUpdate(final String version) {
        mCompositeSubscription.add(Update.check()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (-1 == version.indexOf(s) && -1 == version.indexOf("t")) {
                            mBaseView.onUpdateReady();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                }));
    }

    public void checkGiteeUpdate(final int appVersionCode) {
        mCompositeSubscription.add(Update.checkGitee()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String json) {
                        try {
                            String versionName = new JSONObject(json).getString(APP_VERSIONNAME);
                            String versionCodeString = new JSONObject(json).getString(APP_VERSIONCODE);
                            int ServerAppVersionCode = Integer.parseInt(versionCodeString);
                            String content = new JSONObject(json).getString(APP_CONTENT);
                            String md5 = new JSONObject(json).getString(APP_MD5);
                            String url = new JSONObject(json).getString(APP_URL);
                            if (appVersionCode < ServerAppVersionCode) {
                                mBaseView.onUpdateReady(versionName,content,url,ServerAppVersionCode,md5);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                    }
                }));
    }


    public void getSourceBaseUrl() {
        mCompositeSubscription.add(
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        OkHttpClient client = App.getHttpClient();
                        Request request = new Request.Builder().url(SOURCE_URL).build();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                            if (response.isSuccessful()) {
                                String json = response.body().string();
                                subscriber.onNext(json);
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
                }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String json) {
                                try {
                                    String HHAAZZ = new JSONObject(json).getString("HHAAZZ");
                                    String sw = new JSONObject(json).getString("sw");
                                    if (!HHAAZZ.equals(App.getPreferenceManager().getString(PreferenceManager.PREF_HHAAZZ_BASEURL, ""))){
                                        App.getPreferenceManager().putString(PreferenceManager.PREF_HHAAZZ_BASEURL, HHAAZZ);
                                    }
                                    if (!sw.equals(App.getPreferenceManager().getString(PreferenceManager.PREF_HHAAZZ_SW, ""))){
                                        App.getPreferenceManager().putString(PreferenceManager.PREF_HHAAZZ_SW, sw);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                            }
                        }));
    }
}
