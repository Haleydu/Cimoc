package com.hiroshi.cimoc.misc;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2018/2/13.
 */

public class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private List<Activity> mActivityList;

    public ActivityLifecycle() {
        mActivityList = new LinkedList<>();
    }

    public void clear() {
        for (Activity activity : mActivityList) {
           activity.finish();
        }
        mActivityList.clear();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mActivityList.add(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
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
        mActivityList.remove(activity);
    }

}
