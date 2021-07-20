package com.haleydu.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/10/12.
 */

public class MessageDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static MessageDialogFragment newInstance(int title, int content, boolean negative, int requestCode) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_CONTENT, content);
        bundle.putBoolean(DialogCaller.EXTRA_DIALOG_NEGATIVE, negative);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static MessageDialogFragment newInstance(int title, String content, boolean negative, int requestCode) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putString(DialogCaller.EXTRA_DIALOG_CONTENT_TEXT, content);
        bundle.putBoolean(DialogCaller.EXTRA_DIALOG_NEGATIVE, negative);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String content = getArguments().getString(DialogCaller.EXTRA_DIALOG_CONTENT_TEXT);
        if (content == null) {
            content = getString(getArguments().getInt(DialogCaller.EXTRA_DIALOG_CONTENT));
        }
        builder.setTitle(getArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setMessage(content)
                .setPositiveButton(R.string.dialog_positive, this);
        if (getArguments().getBoolean(DialogCaller.EXTRA_DIALOG_NEGATIVE, false)) {
            setCancelable(true);
            builder.setNegativeButton(R.string.dialog_negative, null);
        } else {
            setCancelable(false);
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = getArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, null);
    }

}
