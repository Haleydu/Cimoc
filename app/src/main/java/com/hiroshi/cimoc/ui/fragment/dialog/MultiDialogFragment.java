package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class MultiDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnMultiChoiceClickListener {

    private boolean[] mCheckArray;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] item = getArguments().getStringArray(DialogCaller.EXTRA_DIALOG_ITEMS);
        if (item == null) {
            item = new String[0];
        }
        initCheckArray(item.length);
        builder.setTitle(getArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setMultiChoiceItems(item, mCheckArray, this)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    private void initCheckArray(int length) {
        mCheckArray = getArguments().getBooleanArray(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS);
        if (mCheckArray == null) {
            mCheckArray = new boolean[length];
            for (int i = 0; i != length; ++i) {
                mCheckArray[i] = false;
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which, boolean check) {
        mCheckArray[which] = check;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = getArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putBooleanArray(DialogCaller.EXTRA_DIALOG_RESULT_VALUE, mCheckArray);
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

    public static MultiDialogFragment newInstance(int title, String[] item, boolean[] check, int requestCode) {
        MultiDialogFragment fragment = new MultiDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putStringArray(DialogCaller.EXTRA_DIALOG_ITEMS, item);
        bundle.putBooleanArray(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS, check);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

}
