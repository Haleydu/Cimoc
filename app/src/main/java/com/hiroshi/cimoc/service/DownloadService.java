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
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.fresco.ImagePipelineFactoryBuilder;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
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

    private HashMap<Long, DownTask> hashMap;
    private ExecutorService executor;
    private Future future;
    private OkHttpClient client;
    private Notification.Builder builder;
    private NotificationManager manager;
    private TaskManager taskManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hashMap = new HashMap<>();
        executor = Executors.newCachedThreadPool();
        client = new OkHttpClient();
        taskManager = TaskManager.getInstance();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = NotificationUtils.getBuilder(this, R.drawable.ic_file_download_white_24dp,
                R.string.download_service_doing, false);
        NotificationUtils.notifyBuilder(manager, builder);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long id = intent.getLongExtra(EXTRA_ID, -1);
        long key = intent.getLongExtra(EXTRA_KEY, -1);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String path = intent.getStringExtra(EXTRA_PATH);

        int source = intent.getIntExtra(EXTRA_SOURCE, -1);
        String cid = intent.getStringExtra(EXTRA_CID);
        String comic = intent.getStringExtra(EXTRA_COMIC);
        if (id != -1 && source != -1 && key != -1 && !StringUtils.isEmpty(cid, path, comic, path)) {
            Request request = Manga.downloadRequest(source, cid, path);
            String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(source), comic, title);
            DownTask task = new DownTask(id, source, request, dir);
            addTask(id, task);
            if (future == null) {
                task.download = true;
                future = executor.submit(task);
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

    private synchronized DownTask nextTask() {
        for (DownTask task : hashMap.values()) {
            if (!task.download) {
                task.download = true;
                return task;
            }
        }
        return null;
    }

    public synchronized void addTask(long id, DownTask task) {
        if (!hashMap.containsKey(id)) {
            hashMap.put(id, task);
        }
    }

    public synchronized void removeTask(long id) {
        if (hashMap.containsKey(id)) {
            hashMap.remove(id);
        }
    }

    public class DownTask implements Runnable {

        private long id;
        private int source;
        private Request request;
        private String dir;
        private boolean download;

        public DownTask(long id, int source, Request request, String dir) {
            this.id = id;
            this.source = source;
            this.request = request;
            this.dir = dir;
            this.download = false;
        }

        @Override
        public void run() {
            RxBus.getInstance().post(new RxEvent(RxEvent.DOWNLOAD_STATE_CHANGE, Task.STATE_PARSE, id));
            Headers headers = ImagePipelineFactoryBuilder.getHeaders(source);
            List<ImageUrl> list = Manga.downloadImages(client, source, request);
            int size = list.size();
            // Todo 写数据库
            RxBus.getInstance().post(new RxEvent(RxEvent.DOWNLOAD_STATE_CHANGE, Task.STATE_DOING, id, size));
            for (int i = 0; i != size; ++i) {
                ImageUrl image = list.get(i);
                String url = image.isLazy() ? Manga.downloadLazy(client, source, image.getUrl()) : image.getUrl();
                Request request = new Request.Builder()
                        .headers(headers)
                        .url(url)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        InputStream byteStream = response.body().byteStream();
                        if (writeToFile(byteStream, i + 1, url)) {
                            // Todo 写数据库
                            RxBus.getInstance().post(new RxEvent(RxEvent.DOWNLOAD_PROCESS, id, i + 1, size));
                        }
                    }
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Todo 写数据库
            RxBus.getInstance().post(new RxEvent(RxEvent.DOWNLOAD_STATE_CHANGE, Task.STATE_FINISH, id));
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
            DownTask task = nextTask();
            if (task != null) {
                future = executor.submit(task);
            } else {
                stopSelf();
            }
        }

    }

    private class DownloadServiceBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }

    }

    private static final String EXTRA_ID = "a";
    private static final String EXTRA_KEY = "b";
    private static final String EXTRA_CID = "c";
    private static final String EXTRA_PATH = "d";
    private static final String EXTRA_SOURCE = "e";
    private static final String EXTRA_COMIC = "f";
    private static final String EXTRA_TITLE = "g";

    public static Intent createIntent(Context context, Task task) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(EXTRA_ID, task.getId());
        intent.putExtra(EXTRA_KEY, task.getKey());
        intent.putExtra(EXTRA_TITLE, task.getTitle());
        intent.putExtra(EXTRA_PATH, task.getPath());
        intent.putExtra(EXTRA_SOURCE, task.getSource());
        intent.putExtra(EXTRA_CID, task.getCid());
        intent.putExtra(EXTRA_COMIC, task.getComic());
        return intent;
    }

}
