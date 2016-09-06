package com.hiroshi.cimoc.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.fresco.ImagePipelineFactoryBuilder;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.utils.FileUtils;
import com.hiroshi.cimoc.utils.NotificationUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadService extends Service {

    private String dirPath =
            FileUtils.getPath(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc", "download");

    private HashMap<Long, Task> hashMap;
    private ExecutorService executor;
    private Future future;
    private OkHttpClient client;
    private Notification.Builder builder;
    private NotificationManager manager;

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
        client = new OkHttpClient();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = NotificationUtils.getBuilder(this, R.drawable.ic_file_download_white_24dp,
                R.string.download_service_doing, false);
        NotificationUtils.notifyBuilder(manager, builder);
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
                Request request = Manga.downloadRequest(source, cid, path);
                String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic, chapter);
                Task task = new Task(id, source, request, dir, false);
                addTask(id, task);
                if (future == null) {
                    task.download = true;
                    future = executor.submit(task);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationUtils.setBuilder(this, builder, R.string.download_service_complete, false);
        NotificationUtils.notifyBuilder(manager, builder);
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

    public class Task implements Runnable {

        private long id;
        private int source;
        private Request request;
        private String dir;
        private boolean download;

        public Task(long id, int source, Request request, String dir, boolean download) {
            this.id = id;
            this.source = source;
            this.request = request;
            this.dir = dir;
            this.download = download;
        }

        @Override
        public void run() {
            Headers headers = ImagePipelineFactoryBuilder.getHeaders(source);
            List<ImageUrl> list = Manga.downloadImages(client, source, request);
            int size = list.size();
            for (int i = 1; i <= size; ++i) {
                ImageUrl image = list.get(i - 1);
                String url = image.isLazy() ? Manga.downloadLazy(client, source, image.getUrl()) : image.getUrl();
                Request request = new Request.Builder()
                        .headers(headers)
                        .url(url)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        InputStream byteStream = response.body().byteStream();
                        if (writeToFile(byteStream, i, url)) {
                           // RxBus.getInstance().post(new RxEvent(RxEvent.DOWNLOAD_PROCESS, id, i));
                        }
                    }
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            onDownloadComplete();
        }

        private boolean writeToFile(InputStream byteStream, int count, String url) {
            String suffix = StringUtils.getSplit(url, "\\.", -1);
            if (suffix == null) {
                suffix = "jpg";
            }
            return FileUtils.writeBinaryToFile(dir, StringUtils.format("%03d.%s", count, suffix), byteStream);
        }

        private void onDownloadComplete() {
            removeTask(id);
            Task task = nextTask();
            if (task != null) {
                future = executor.submit(task);
            } else {
                stopSelf();
            }
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

    public static Intent createIntent(Context context, long id, int source, String cid, String path, String comic, String chapter) {
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
