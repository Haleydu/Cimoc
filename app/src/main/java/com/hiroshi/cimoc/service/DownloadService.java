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

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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

    private HashMap<Long, Download> hashMap;
    private ExecutorService executor;
    private Future future;
    private OkHttpClient client;
    private Notification.Builder builder;
    private NotificationManager notification;
    private TaskManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        hashMap = new HashMap<>();
        executor = Executors.newSingleThreadExecutor();
        client = new OkHttpClient();
        manager = TaskManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            List<Task> list =  intent.getParcelableArrayListExtra(EXTRA_TASK);
            for (Task task : list) {
                Download download = new Download(task);
                addDownload(task.getId(), download);
                if (future == null) {
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_START));
                    download.running = true;
                    future = executor.submit(download);
                    if (notification == null) {
                        notification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        builder = NotificationUtils.getBuilder(this, R.drawable.ic_file_download_white_24dp,
                                R.string.download_service_doing, true);
                        NotificationUtils.notifyBuilder(1, notification, builder);
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notification != null) {
            if (future != null) {
                future.cancel(true);
            }
            notifyCompleted();
        }
    }

    private Download nextDownload() {
        for (Download download : hashMap.values()) {
            if (!download.running) {
                download.running = true;
                return download;
            }
        }
        return null;
    }

    public synchronized void addDownload(long id, Download task) {
        if (!hashMap.containsKey(id)) {
            hashMap.put(id, task);
        }
    }

    public synchronized void removeDownload(long id) {
        if (hashMap.containsKey(id)) {
            if (hashMap.get(id).running) {
                future.cancel(true);
                Download download = nextDownload();
                if (download != null) {
                    future = executor.submit(download);
                } else {
                    notifyCompleted();
                    stopSelf();
                }
            }
            hashMap.remove(id);
        }
    }

    private void notifyCompleted() {
        NotificationUtils.setBuilder(this, builder, R.string.download_service_complete, false);
        NotificationUtils.notifyBuilder(1, notification, builder);
        future = null;
        notification = null;
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_STOP));
    }

    public synchronized void initTask(List<Task> list) {
        for (Task task : list) {
            Download download = hashMap.get(task.getId());
            if (download != null) {
                task.setState(download.task.getState());
            }
        }
    }

    public class Download implements Runnable {

        private Task task;
        private boolean running;

        Download(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            int connect = ((CimocApplication) getApplication()).getPreferenceManager().getInt(PreferenceManager.PREF_DOWNLOAD_CONNECTION, 0);
            int source = task.getSource();

            task.setState(Task.STATE_PARSE);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PARSE, task.getId()));

            Headers headers = ImagePipelineFactoryBuilder.getHeaders(source);
            List<ImageUrl> list = Manga.downloadImages(client, source, task.getCid(), task.getPath());
            int size = list.size();

            if (size != 0) {
                onDownloadDoing(size);

                for (int i = task.getProgress(); i != size; ++i) {
                    int count = 0;  // page download error
                    while (count <= connect) {
                        int error = 0;  // url download error
                        ImageUrl image = list.get(i);
                        String[] urls = image.getUrl();
                        for (String url : urls) {
                            url = image.isLazy() ? Manga.downloadLazy(client, source, url) : url;
                            if (url != null) {
                                Request request = new Request.Builder()
                                        .headers(headers)
                                        .url(url)
                                        .build();
                                InputStream byteStream = downloadUrl(request);
                                if (byteStream != null && writeToFile(byteStream, i + 1, url)) {
                                    onDownloadProgress(i + 1);
                                    break;
                                }
                            }
                            ++error;
                        }
                        if (error == urls.length) {
                            ++count;
                        } else {
                            break;
                        }
                    }
                    if (count == connect + 1) {     // page download error
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, task.getId()));
                        break;
                    }
                }
            } else {
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, task.getId()));
            }

            removeDownload(task.getId());
        }

        private InputStream downloadUrl(Request request) {
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().byteStream();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            return null;
        }

        private void onDownloadDoing(int max) {
            task.setMax(max);
            task.setState(Task.STATE_DOING);
            manager.update(task);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_DOING, task.getId(), max));
        }

        private void onDownloadProgress(int progress) {
            task.setProgress(progress);
            manager.update(task);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_PROCESS, task.getId(), progress, task.getMax()));
        }

        private boolean writeToFile(InputStream byteStream, int count, String url) {
            String suffix = StringUtils.getSplit(url, "\\.", -1);
            if (suffix == null) {
                suffix = "jpg";
            } else {
                suffix = suffix.split("\\?")[0];
            }
            String dir = FileUtils.getPath(dirPath, SourceManager.getTitle(task.getSource()), task.getComic(), task.getTitle());
            return FileUtils.writeBinaryToFile(dir, StringUtils.format("%03d.%s", count, suffix), byteStream);
        }

    }

    public class DownloadServiceBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }

    }

    private static final String EXTRA_TASK = "a";

    public static Intent createIntent(Context context, Task task) {
        ArrayList<Task> list = new ArrayList<>(1);
        list.add(task);
        return createIntent(context, list);
    }

    public static Intent createIntent(Context context, ArrayList<Task> list) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putParcelableArrayListExtra(EXTRA_TASK, list);
        return intent;
    }

}
