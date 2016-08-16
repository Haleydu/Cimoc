package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/8/4.
 */
public class DialogFactory {

    public static AlertDialog buildPositiveDialog(Context context, int titleId, int messageId, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.dialog_positive, listener);
        builder.setNegativeButton(R.string.dialog_negative, null);
        return builder.create();
    }

    public static AlertDialog buildSingleChoiceDialog(Context context, int titleId, int array, int choice,
                                                      OnClickListener listener, OnClickListener positive) {
        return buildSingleChoiceDialog(context, titleId, context.getResources().getStringArray(array), choice, listener, positive);
    }

    public static AlertDialog buildSingleChoiceDialog(Context context, int titleId, String[] array, int choice,
                                                      OnClickListener listener, OnClickListener positive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setSingleChoiceItems(array, choice, listener);
        if (positive != null) {
            builder.setPositiveButton(R.string.dialog_positive, positive);
        }
        return builder.create();
    }

    public static AlertDialog buildCancelableFalseDialog(Context context) {
        return new AlertDialog.Builder(context).setCancelable(false).create();
    }

}
