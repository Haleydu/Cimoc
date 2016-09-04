package com.hiroshi.cimoc.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;
import com.hiroshi.cimoc.utils.FileUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadService extends Service {

    private static String dirPath =
            FileUtils.getPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc", "download");

    private HashMap<Long, Task> hashMap;
    private ExecutorService executor;
    private Future<Integer> future;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hashMap = new HashMap<>();
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            long id = intent.getLongExtra(EXTRA_ID, -1);
            int source = intent.getIntExtra(EXTRA_SOURCE, -1);
            String cid = intent.getStringExtra(EXTRA_CID);
            String path = intent.getStringExtra(EXTRA_PATH);
            String comic = intent.getStringExtra(EXTRA_COMIC);
            String chapter = intent.getStringExtra(EXTRA_CHAPTER);
            if (id != -1 && source != -1 && !StringUtils.isEmpty(cid, path, comic, path)) {
                addTask(id, new Task(id, source, cid, path, comic, chapter, false));
                Task task = nextTask();
                if (task != null && future == null) {
                    future = executor.submit(task);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public synchronized boolean isEmpty() {
        return hashMap.isEmpty();
    }

    private synchronized Task nextTask() {
        for (Task task : hashMap.values()) {
            if (!task.download) {
                task.download = true;
                return task;
            }
        }
        return null;
    }

    public synchronized void addTask(long id, Task task) {
        if (!hashMap.containsKey(id)) {
            hashMap.put(id, task);
        }
    }

    public synchronized void removeTask(long id) {
        if (hashMap.containsKey(id)) {
            hashMap.remove(id);
        }
    }

    public static class Task implements Callable<Integer> {

        private long id;
        private int source;
        private String cid;
        private String path;
        private String comic;
        private String chapter;
        private boolean download;

        public Task(long id, int source, String cid, String path, String comic, String chapter, boolean download) {
            this.id = id;
            this.source = source;
            this.cid = cid;
            this.path = path;
            this.comic = comic;
            this.chapter = chapter;
            this.download = download;
        }

        public void setDownload(boolean download) {
            this.download = download;
        }

        public boolean isDownload() {
            return this.download;
        }

        @Override
        public Integer call() throws Exception {
            OkHttpClient client = new OkHttpClient();
            String parent = FileUtils.getPath(dirPath, String.valueOf(source), comic, chapter);
            String referer = ControllerBuilderFactory.getReferer(source);
            List<ImageUrl> list = Manga.fetch(client, source, cid, path);
            if (!list.isEmpty()) {
                int count = 1;
                for (ImageUrl image : list) {
                    String url = image.getUrl();
                    if (image.isLazy()) {
                        url = Manga.fetch(client, source, url);
                    }
                    Request request = new Request.Builder()
                            .header("Referer", referer)
                            .url(url)
                            .build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            String suffix = StringUtils.getSplit(url, "\\.", -1);
                            if (suffix == null) {
                                suffix = "jpg";
                            }
                            if (FileUtils.writeToBinaryFile(parent,
                                    String.format(Locale.getDefault(), "%03d.%s", count, suffix),
                                    response.body().byteStream())) {
                                RxBus.getInstance().post(new RxEvent(RxEvent.DOWNLOAD_PROCESS, id, count));
                            }
                        }
                        response.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ++count;
                }
            }
            return null;
        }

    }

    public class DownloadServiceBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }

    }

    private static final String EXTRA_ID = "a";
    private static final String EXTRA_CID = "b";
    private static final String EXTRA_PATH = "c";
    private static final String EXTRA_SOURCE = "d";
    private static final String EXTRA_COMIC = "e";
    private static final String EXTRA_CHAPTER = "f";

    public static Intent createIntent(Context context, long id, String cid, String path, int source, String comic, String chapter) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_CID, cid);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_COMIC, comic);
        intent.putExtra(EXTRA_CHAPTER, chapter);
        return intent;
    }

}
