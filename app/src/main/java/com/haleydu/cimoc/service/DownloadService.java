package com.haleydu.cimoc.service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import android.util.Pair;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.R;
import com.haleydu.cimoc.component.AppGetter;
import com.haleydu.cimoc.core.Download;
import com.haleydu.cimoc.core.Manga;
import com.haleydu.cimoc.global.Extra;
import com.haleydu.cimoc.manager.ChapterManager;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.manager.SourceManager;
import com.haleydu.cimoc.manager.TaskManager;
import com.haleydu.cimoc.misc.NotificationWrapper;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.model.Task;
import com.haleydu.cimoc.parser.Parser;
import com.haleydu.cimoc.rx.RxBus;
import com.haleydu.cimoc.rx.RxEvent;
import com.haleydu.cimoc.saf.DocumentFile;
import com.haleydu.cimoc.utils.DocumentUtils;
import com.haleydu.cimoc.utils.JMTTUtil;
import com.haleydu.cimoc.utils.StringUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadService extends Service implements AppGetter {

    private static final String NOTIFICATION_DOWNLOAD = "NOTIFICATION_DOWNLOAD";

    private LongSparseArray<Pair<Worker, Future>> mWorkerArray;
    private ExecutorService mExecutorService;
    private OkHttpClient mHttpClient;
    private NotificationWrapper mNotification;
    private TaskManager mTaskManager;
    private SourceManager mSourceManager;
    private ChapterManager mChapterManager;
    private ContentResolver mContentResolver;

    public static Intent createIntent(Context context, Task task) {
        ArrayList<Task> list = new ArrayList<>(1);
        list.add(task);
        return createIntent(context, list);
    }

    public static Intent createIntent(Context context, ArrayList<Task> list) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putParcelableArrayListExtra(Extra.EXTRA_TASK, list);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceManager manager = ((App) getApplication()).getPreferenceManager();
        int num = manager.getInt(PreferenceManager.PREF_DOWNLOAD_THREAD, 2);
        mWorkerArray = new LongSparseArray<>();
        mExecutorService = Executors.newFixedThreadPool(num);
        mHttpClient = App.getHttpClient();
        mTaskManager = TaskManager.getInstance(this);
        mSourceManager = SourceManager.getInstance(this);
        mContentResolver = getContentResolver();
        mChapterManager = ChapterManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_START));
            if (mNotification == null) {
                mNotification = new NotificationWrapper(this, NOTIFICATION_DOWNLOAD,
                        R.drawable.ic_file_download_white_24dp, true);
                mNotification.post(getString(R.string.download_service_doing), true);
            }
            List<Task> list = intent.getParcelableArrayListExtra(Extra.EXTRA_TASK);
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
        if (mNotification != null) {
            mExecutorService.shutdownNow();
            notifyCompleted();
        }
    }

    @Override
    public App getAppInstance() {
        return (App) getApplication();
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
        if (mNotification != null) {
            mNotification.post(getString(R.string.download_service_done), false);
            mNotification.cancel();
            mNotification = null;
        }
        mWorkerArray.clear();
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_STOP));
    }

    public synchronized void initTask(List<Task> list) {
        for (Task task : list) {
            Pair<Worker, Future> pair = mWorkerArray.get(task.getId());
            if (pair != null) {
                task.setState(pair.first.mTask.getState());
            }
        }
    }

    public class Worker implements Runnable {

        private Task mTask;
        private Parser mParse;

        Worker(Task task) {
            mTask = task;
            mParse = mSourceManager.getParser(task.getSource());
        }

        @Override
        public void run() {
            try {
                List<ImageUrl> list = onDownloadParse();
                int size = list.size();
                if (size != 0) {
                    DocumentFile dir = Download.updateChapterIndex(mContentResolver, getAppInstance().getDocumentFile(), mTask);
                    if (dir != null) {
                        mTask.setMax(size);
                        mTask.setState(Task.STATE_DOING);
                        boolean success = false;
                        for (int i = mTask.getProgress(); i < size; ++i) {
                            onDownloadProgress(i);
                            ImageUrl image = list.get(i);
                            int count = 0;  // 单页下载错误次数
                            success = false; // 是否下载成功
                            while (count++ < 20 && !success) {
                                String[] urls = image.getUrls();
                                for (int j = 0; !success && j < urls.length; ++j) {
                                    String url = image.isLazy() ? Manga.getLazyUrl(mParse, urls[j]) : urls[j];
                                    Request request = buildRequest(mParse.getHeader(url), url);
                                    success = RequestAndWrite(dir, request, i + 1, url);
                                }
                            }
                            if (!success) {     // 单页下载错误
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, mTask.getId()));
                                break;
                            }
                        }
                        if (success) {
                            onDownloadProgress(size);
                        }
                    } else {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, mTask.getId()));
                    }
                } else {
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_ERROR, mTask.getId()));
                }
            } catch (InterruptedIOException e) {
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PAUSE, mTask.getId()));
            }

            completeDownload(mTask.getId());
        }

        private boolean RequestAndWrite(DocumentFile parent, Request request, int num, String url) throws InterruptedIOException {
            if (request != null) {
                Response response = null;
                try {
                    if (mTask.getSource() == 72){
                        OkHttpClient mJMTTHttpClient = new OkHttpClient().newBuilder()
                                .followRedirects(true)
                                .followSslRedirects(true)
                                .retryOnConnectionFailure(true)
                                .addInterceptor(chain -> {
                                    String url1 = chain.request().url().toString();
                                    Response response1 = chain.proceed(chain.request());
                                    if (!url1.toLowerCase().contains("media/photos")) return response1;
                                    int cha = Integer.parseInt(url1.substring(url1.indexOf("photos/") + 7, url1.lastIndexOf("/")));
                                    if ( cha < 220980) return response1;
                                    byte[] res = new JMTTUtil().decodeImage(response1.body().byteStream());
                                    MediaType mediaType = MediaType.parse("image/avif,image/webp,image/apng,image/*,*/*");
                                    ResponseBody outputBytes = ResponseBody.create(mediaType, res);
                                    return response1.newBuilder().body(outputBytes).build();
                                })
                                .build();
                        response = mJMTTHttpClient.newCall(request).execute();
                    }else {
                        response = mHttpClient.newCall(request).execute();
                    }
                    if (response.isSuccessful()) {
                        String displayName = buildFileName(num, url);
                        displayName = displayName.replaceAll("[:/(\\\\)(\\?)<>\"(\\|)(\\.)]", "_")+".jpg";
                        DocumentFile file = DocumentUtils.getOrCreateFile(parent, displayName);
                        DocumentUtils.writeBinaryToFile(mContentResolver, file, response.body().byteStream());
                        return true;
                    }
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
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
            }
            return false;
        }

        private Request buildRequest(Headers headers, String url) {
            if (StringUtils.isEmpty(url)) {
                return null;
            }

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
            mTask.setState(Task.STATE_PARSE);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_STATE_CHANGE, Task.STATE_PARSE, mTask.getId()));
            return Manga.getImageUrls(mParse, mTask.getSource(), mTask.getCid(), mTask.getPath(), mTask.getTitle(), mChapterManager);
        }

        private void onDownloadProgress(int progress) {
            mTask.setProgress(progress);
            mTaskManager.update(mTask);
            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_PROCESS, mTask.getId(), progress, mTask.getMax()));
        }

    }

    public class DownloadServiceBinder extends Binder {

        public DownloadService getService() {
            return DownloadService.this;
        }

    }

}
