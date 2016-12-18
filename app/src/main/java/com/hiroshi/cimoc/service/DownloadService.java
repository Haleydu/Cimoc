package com.hiroshi.cimoc.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.fresco.ImagePipelineFactoryBuilder;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.utils.DocumentUtils;
import com.hiroshi.cimoc.utils.NotificationUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadService extends Service {

    private LongSparseArray<Pair<Worker, Future>> mWorkerArray;
    private ExecutorService mExecutorService;
    private OkHttpClient mHttpClient;
    private Notification.Builder builder;
    private NotificationManager notification;
    private TaskManager mTaskManager;
    private ContentResolver mContentResolver;

    private int mConnectTimes;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager manager = ((CimocApplication) getApplication()).getPreferenceManager();
        int num = manager.getInt(PreferenceManager.PREF_DOWNLOAD_THREAD, 1);
        mConnectTimes = manager.getInt(PreferenceManager.PREF_DOWNLOAD_CONNECTION, 0);
        mWorkerArray = new LongSparseArray<>();
        mExecutorService = Executors.newFixedThreadPool(num);
        mHttpClient = CimocApplication.getHttpClient();
        mTaskManager = TaskManager.getInstance();
        mContentResolver = getContentResolver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_START));
            if (notification == null) {
                notification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                builder = NotificationUtils.getBuilder(this, R.drawable.ic_file_download_white_24dp,
                        R.string.download_service_doing, true);
                NotificationUtils.notifyBuilder(1, notification, builder);
            }
            List<Task> list =  intent.getParcelableArrayListExtra(EXTRA_TASK);
            for (Task task : list) {
                Worker worker = new Worker(task);
                Future future = mExecutorService.submit(worker);
                addWorker(task.getId(), worker, future);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notification != null) {
            mExecutorService.shutdownNow();
            notifyCompleted();
        }
    }

    public synchronized void addWorker(long id, Worker worker, Future future) {
        if (mWorkerArray.get(id) == null) {
            mWorkerArray.put(id, Pair.create(worker, future));
        }
    }

    public synchronized void removeDownload(long id) {
        Pair<Worker, Future> pair = mWorkerArray.get(id);
        if (pair != null) {
            pair.second.cancel(true);
            mWorkerArray.remove(id);
        }
    }

    public synchronized void completeDownload(long id) {
        mWorkerArray.remove(id);
        if (mWorkerArray.size() == 0) {
            notifyCompleted();
            stopSelf();
        }
    }

    private void notifyCompleted() {
        if (notification != null) {
            NotificationUtils.setBuilder(this, builder, R.string.download_service_complete, false);
            NotificationUtils.notifyBuilder(1, notification, builder);
            notification = null;
        }
        mWorkerArray.clear();
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_STOP));
    }

    public synchronized void initTask(List<Task> list) {
        for (Task task : list) {
            Pair<Worker, Future> pair = mWorkerArray.get(task.getId());
            if (pair != null) {
                task.setState(pair.first.task.getState());
            }
        }
    }

    public class Worker implements Runnable {

        private Task task;

        Worker(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            try {
                List<ImageUrl> list = onDownloadParse();
                int size = list.size();
                if (size != 0) {
                    int source = task.getSource();
                    Headers headers = ImagePipelineFactoryBuilder.getHeaders(source);
                    DocumentFile dir = Download.updateChapterIndex(task);
                    if (dir != null) {
                        task.setMax(size);
                        task.setState(Task.STATE_DOING);
                        for (int i = task.getProgress(); i < size; ++i) {
                            int count = 0;  // 单页下载错误次数
                            boolean success = false; // 是否下载成功
                            while (count++ <= mConnectTimes && !success) {
                                onDownloadProgress(i);
                                ImageUrl image = list.get(i);
                                for (String url : image.getUrl()) {
                                    url = image.isLazy() ? Manga.getLazyUrl(mHttpClient, source, url) : url;
                                    if (url != null) {
                                        Request request = buildRequest(headers, url);
                                        success = RequestAndWrite(dir, request, i + 1, url);
                                    }
                                }
                            }
                            if (count == mConnectTimes + 1) {     // 单页下载错误
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, task.getId()));
                                break;
                            } else if (success && i + 1 == size) {
                                onDownloadProgress(size);
                            }
                        }
                    } else {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, task.getId()));
                    }
                } else {
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, task.getId()));
                }
            } catch (InterruptedIOException e) {
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PAUSE, task.getId()));
            }

            completeDownload(task.getId());
        }

        private boolean RequestAndWrite(DocumentFile parent, Request request, int num, String url) throws InterruptedIOException {
            Response response = null;
            try {
                response = mHttpClient.newCall(request).execute();
                if (response.isSuccessful()) {
                    //String mimeType = response.header("Content-Type", "image/jpeg");
                    String displayName = buildFileName(num, url);
                    DocumentFile file = parent.createFile(null, displayName);
                    DocumentUtils.writeBinaryToFile(mContentResolver, file, response.body().byteStream());
                    return true;
                }
            } catch (InterruptedIOException e) {
                // 由暂停下载引发，需要抛出以便退出外层循环，结束任务
                throw e;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            return false;
        }

        private Request buildRequest(Headers headers, String url) {
            return new Request.Builder()
                    .cacheControl(new CacheControl.Builder().noStore().build())
                    .headers(headers)
                    .url(url)
                    .get()
                    .build();
        }

        private String buildFileName(int num, String url) {
            String suffix = StringUtils.split(url, "\\.", -1);
            if (suffix == null) {
                suffix = "jpg";
            } else {
                suffix = suffix.split("\\?")[0];
            }
            return StringUtils.format("%03d.%s", num, suffix);
        }

        private List<ImageUrl> onDownloadParse() throws InterruptedIOException {
            task.setState(Task.STATE_PARSE);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PARSE, task.getId()));
            int source = task.getSource();
            return Manga.getImageUrls(mHttpClient, source, task.getCid(), task.getPath());
        }

        private void onDownloadProgress(int progress) {
            task.setProgress(progress);
            mTaskManager.update(task);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_PROCESS, task.getId(), progress, task.getMax()));
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
