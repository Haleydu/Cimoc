package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/8/4.
 */
public class DialogUtils {

    public static AlertDialog buildSingleChoiceDialog(Context context, int titleId, int itemsId, int choice,
                                                      OnClickListener listener, OnClickListener positive) {
        return buildSingleChoiceDialog(context, titleId, context.getResources().getStringArray(itemsId), choice, listener, positive);
    }

    public static AlertDialog buildSingleChoiceDialog(Context context, int titleId, CharSequence[] items, int choice,
                                                      OnClickListener listener, OnClickListener positive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setSingleChoiceItems(items, choice, listener);
        if (positive != null) {
            builder.setPositiveButton(R.string.dialog_positive, positive);
        }
        return builder.create();
    }

    public static AlertDialog buildMultiChoiceDialog(Context context, int titleId, CharSequence[] items, boolean[] checkedItems,
                                                     OnMultiChoiceClickListener listener, int textId, OnClickListener neutral,
                                                     OnClickListener positive) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleId);
        builder.setMultiChoiceItems(items, checkedItems, listener);
        if (positive != null) {
            builder.setPositiveButton(R.string.dialog_positive, positive);
        }
        if (neutral != null) {
            builder.setNeutralButton(textId, neutral);
        }
        return builder.create();
    }

    public static AlertDialog buildCancelableFalseDialog(Context context, int messageId) {
        return new AlertDialog.Builder(context).setCancelable(false).setMessage(messageId).create();
    }

}
