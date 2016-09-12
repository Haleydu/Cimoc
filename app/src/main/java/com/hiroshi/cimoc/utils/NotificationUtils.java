package com.hiroshi.cimoc.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/9/6.
 */
public class NotificationUtils {

    public static void notifyBuilder(int id, NotificationManager manager, Notification.Builder builder) {
        if (Build.VERSION.SDK_INT >= 16) {
            manager.notify(id, builder.build());
        } else {
            manager.notify(id, builder.getNotification());
        }
    }

    public static Notification.Builder getBuilder(Context context, @DrawableRes int icon, @StringRes int text, boolean ongoing,
                                                  int max, int progress, boolean indeterminate) {
        return new Notification.Builder(context)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(text))
                .setTicker(context.getString(text))
                .setOngoing(ongoing)
                .setProgress(max, progress, indeterminate);
    }

    public static Notification.Builder getBuilder(Context context, @DrawableRes int icon, @StringRes int text, boolean ongoing) {
        return getBuilder(context, icon, text, ongoing, 0, 0, false);
    }

    public static void setBuilder(Context context, Notification.Builder builder, @StringRes int text, boolean ongoing,
                                  int max, int progress, boolean indeterminate) {
        builder.setContentText(context.getString(text))
                .setTicker(context.getString(text))
                .setOngoing(ongoing)
                .setProgress(max, progress, indeterminate);
    }

    public static void setBuilder(Context context, Notification.Builder builder, @StringRes int text, boolean ongoing) {
        setBuilder(context, builder, text, ongoing, 0, 0, false);
    }

}
