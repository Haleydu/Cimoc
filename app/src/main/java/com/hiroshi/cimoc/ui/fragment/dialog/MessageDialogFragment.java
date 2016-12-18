package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.view.DialogView;

/**
 * Created by Hiroshi on 2016/10/12.
 */

public class MessageDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(DialogView.EXTRA_DIALOG_TITLE))
                .setMessage(getArguments().getInt(DialogView.EXTRA_DIALOG_CONTENT))
                .setPositiveButton(R.string.dialog_positive, this);
        if (getArguments().getBoolean(DialogView.EXTRA_DIALOG_NEGATIVE, false)) {
            builder.setNegativeButton(R.string.dialog_negative, null);
        }
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = getArguments().getInt(DialogView.EXTRA_DIALOG_REQUEST_CODE);
        Bundle extra = getArguments().getBundle(DialogView.EXTRA_DIALOG_BUNDLE);
        Bundle bundle = new Bundle();
        bundle.putBundle(DialogView.EXTRA_DIALOG_BUNDLE, extra);
        DialogView target = (DialogView) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

    public static MessageDialogFragment newInstance(int title, int content, boolean negative, Bundle extra, int requestCode) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogView.EXTRA_DIALOG_TITLE, title);
        bundle.putInt(DialogView.EXTRA_DIALOG_CONTENT, content);
        bundle.putBoolean(DialogView.EXTRA_DIALOG_NEGATIVE, negative);
        bundle.putBundle(DialogView.EXTRA_DIALOG_BUNDLE, extra);
        bundle.putInt(DialogView.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

}
