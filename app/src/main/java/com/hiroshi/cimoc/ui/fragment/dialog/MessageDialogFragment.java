package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/10/12.
 */

public class MessageDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setMessage(getArguments().getInt(EXTRA_CONTENT))
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getTargetFragment() != null) {
                            ((MessageDialogListener) getTargetFragment()).onMessagePositiveClick(getArguments().getInt(EXTRA_TYPE));
                        } else {
                            ((MessageDialogListener) getActivity()).onMessagePositiveClick(getArguments().getInt(EXTRA_TYPE));
                        }
                    }
                });
        if (getArguments().getBoolean(EXTRA_NEGATIVE, false)) {
            builder.setNegativeButton(R.string.dialog_negative, null);
        }
        return builder.create();
    }

    public interface MessageDialogListener {
        void onMessagePositiveClick(int type);
    }

    private static final String EXTRA_TITLE = "a";
    private static final String EXTRA_CONTENT = "b";
    private static final String EXTRA_NEGATIVE = "c";
    private static final String EXTRA_TYPE = "d";

    public static MessageDialogFragment newInstance(int title, int content, boolean negative) {
        return newInstance(title, content, negative, -1);
    }

    public static MessageDialogFragment newInstance(int title, int content, boolean negative, int type) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TITLE, title);
        bundle.putInt(EXTRA_CONTENT, content);
        bundle.putBoolean(EXTRA_NEGATIVE, negative);
        bundle.putInt(EXTRA_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

}
