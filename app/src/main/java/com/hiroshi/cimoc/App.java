package com.hiroshi.cimoc;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import androidx.multidex.MultiDex;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.helper.DBOpenHelper;
import com.hiroshi.cimoc.helper.UpdateHelper;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.misc.ActivityLifecycle;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.saf.DocumentFile;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.utils.DocumentUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import androidx.multidex.MultiDexApplication;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class App extends MultiDexApplication implements AppGetter, Thread.UncaughtExceptionHandler {

    public static int mWidthPixels;
    public static int mHeightPixels;
    public static int mCoverWidthPixels;
    public static int mCoverHeightPixels;
    public static int mLargePixels;

    private static OkHttpClient mHttpClient;

    private DocumentFile mDocumentFile;
    private static PreferenceManager mPreferenceManager;
    private ControllerBuilderProvider mBuilderProvider;
    
    private RecyclerView.RecycledViewPool mRecycledPool;
    private DaoSession mDaoSession;
    private ActivityLifecycle mActivityLifecycle;


    private static WifiManager manager_wifi;
    private static App mApp;
    private static Activity sActivity;

    // 默认Github源
    private static String UPDATE_CURRENT_URL = "https://api.github.com/repos/Haleydu/Cimoc/releases/latest";

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
        mActivityLifecycle = new ActivityLifecycle();
        registerActivityLifecycleCallbacks(mActivityLifecycle);
        mPreferenceManager = new PreferenceManager(this);
        DBOpenHelper helper = new DBOpenHelper(this, "cimoc.db");
        mDaoSession = new DaoMaster(helper.getWritableDatabase()).newSession(IdentityScopeType.None);
        UpdateHelper.update(mPreferenceManager, getDaoSession());
        Fresco.initialize(this);
        initPixels();

        manager_wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        //获取栈顶Activity以及当前App上下文
        mApp = this;
        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
//                Log.d("ActivityLifecycle:",activity+"onActivityCreated");
            }

            @Override
            public void onActivityStarted(Activity activity) {
//                Log.d("ActivityLifecycle:",activity+"onActivityStarted");
                sActivity = activity;

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("MODEL: ").append(Build.MODEL).append('\n');
        sb.append("SDK: ").append(Build.VERSION.SDK_INT).append('\n');
        sb.append("RELEASE: ").append(Build.VERSION.RELEASE).append('\n');
        sb.append('\n').append(e.getLocalizedMessage()).append('\n');
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append('\n');
            sb.append(element.toString());
        }
        try {
            DocumentFile doc = getDocumentFile();
            DocumentFile dir = DocumentUtils.getOrCreateSubDirectory(doc, "log");
            DocumentFile file = DocumentUtils.getOrCreateFile(dir, StringUtils.getDateStringWithSuffix("log"));
            DocumentUtils.writeStringToFile(getContentResolver(), file, sb.toString());
        } catch (Exception ex) {
        }
        mActivityLifecycle.clear();
        System.exit(1);
    }

    @Override
    public App getAppInstance() {
        return this;
    }

    public static Context getAppContext() {
        return mApp;
    }

    public static Resources getAppResources() {
        return mApp.getResources();
    }

    public static Activity getActivity() {
        return sActivity;
    }

    public static WifiManager getManager_wifi() {
        return manager_wifi;
    }

    private void initPixels() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        mWidthPixels = metrics.widthPixels;
        mHeightPixels = metrics.heightPixels;
        mCoverWidthPixels = mWidthPixels / 3;
        mCoverHeightPixels = mHeightPixels * mCoverWidthPixels / mWidthPixels;
        mLargePixels = 3 * metrics.widthPixels * metrics.heightPixels;
    }

    public void initRootDocumentFile() {
        String uri = mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE);
        mDocumentFile = Storage.initRoot(this, uri);
    }

    public DocumentFile getDocumentFile() {
        if (mDocumentFile == null) {
            initRootDocumentFile();
        }
        return mDocumentFile;
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public static PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public RecyclerView.RecycledViewPool getGridRecycledPool() {
        if (mRecycledPool == null) {
            mRecycledPool = new RecyclerView.RecycledViewPool();
            mRecycledPool.setMaxRecycledViews(GridAdapter.TYPE_GRID, 20);
        }
        return mRecycledPool;
    }

    public ControllerBuilderProvider getBuilderProvider() {
        if (mBuilderProvider == null) {
            mBuilderProvider = new ControllerBuilderProvider(getApplicationContext(),
                    SourceManager.getInstance(this).new HeaderGetter(), true);
        }
        return mBuilderProvider;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void setUpdateCurrentUrl(String updateCurrentUrl) {
        UPDATE_CURRENT_URL = updateCurrentUrl;
    }

    public static String getUpdateCurrentUrl() {
        return UPDATE_CURRENT_URL;
    }

    public static OkHttpClient getHttpClient() {

        //OkHttpClient返回null实现"仅WiFi联网"，后面要注意空指针处理
        if (!manager_wifi.isWifiEnabled() && mPreferenceManager.getBoolean(PreferenceManager.PREF_OTHER_CONNECT_ONLY_WIFI, false)) {
            return null;
        }

        if (mHttpClient == null) {

            // 3.OkHttp访问https的Client实例
            mHttpClient = new OkHttpClient().newBuilder()
                    .sslSocketFactory(createSSLSocketFactory())
                    .hostnameVerifier(new TrustAllHostnameVerifier())
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .build();
        }

        return mHttpClient;
    }

    // 1.实现X509TrustManager接口
    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    // 2.实现HostnameVerifier接口
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }


}
