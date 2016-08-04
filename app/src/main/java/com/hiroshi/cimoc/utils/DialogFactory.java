package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/8/4.
 */
public class DialogFactory {

    public static AlertDialog buildPositiveDialog(Context context, String title, String message, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog_Alert);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", listener);
        builder.setNegativeButton("取消", null);
        return builder.create();
    }

    public static AlertDialog buildSingleChoiceDialog(Context context, String title, int array, int choice,
                                                      OnClickListener listener, OnClickListener positive) {
        return buildSingleChoiceDialog(context, title, context.getResources().getStringArray(array), choice, listener, positive);
    }

    public static AlertDialog buildSingleChoiceDialog(Context context, String title, String[] array, int choice,
                                                      OnClickListener listener, OnClickListener positive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog_Alert);
        builder.setTitle(title);
        builder.setSingleChoiceItems(array, choice, listener);
        if (positive != null) {
            builder.setPositiveButton("确定", positive);
        }
        return builder.create();
    }

}
