package com.hiroshi.cimoc.misc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2018/2/11.
 */

public class NotificationWrapper {

    private NotificationManager mManager;
    private NotificationCompat.Builder mBuilder;
    private int mId;

    public NotificationWrapper(Context context, String id, @DrawableRes int icon, boolean ongoing) {
        String title = context.getString(R.string.app_name);
        mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mManager.createNotificationChannel(new NotificationChannel(id, id, NotificationManager.IMPORTANCE_MIN));
        }
        mBuilder = new NotificationCompat.Builder(context, id);
        mBuilder.setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), icon))
                .setOngoing(ongoing);
        mId = id.hashCode();
    }

    public void post(int progress, int max) {
        mBuilder.setProgress(max, progress, false);
        mManager.notify(mId, mBuilder.build());
    }

    public void post(String content, int progress, int max) {
        mBuilder.setContentText(content).setTicker(content);
        post(progress, max);
    }

    public void post(String content, boolean ongoing) {
        mBuilder.setOngoing(ongoing);
        post(content, 0, 0);
    }

    public void cancel() {
        mManager.cancel(mId);
    }

}
