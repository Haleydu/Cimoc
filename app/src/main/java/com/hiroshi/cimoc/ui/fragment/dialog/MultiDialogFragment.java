package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class MultiDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private boolean[] check;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] item = getArguments().getStringArray(EXTRA_ITEM);
        check = getArguments().getBooleanArray(EXTRA_CHECK);
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setMultiChoiceItems(item, check, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index, boolean value) {
                        check[index] = value;
                    }
                })
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (getTargetFragment() != null) {
            ((MultiDialogListener) getTargetFragment()).onMultiPositiveClick(getArguments().getInt(EXTRA_TYPE), check);
        } else {
            ((MultiDialogListener) getActivity()).onMultiPositiveClick(getArguments().getInt(EXTRA_TYPE), check);
        }
    }

    public interface MultiDialogListener {
        void onMultiPositiveClick(int type, boolean[] check);
    }

    private static final String EXTRA_TITLE = "a";
    private static final String EXTRA_ITEM = "b";
    private static final String EXTRA_CHECK = "c";
    private static final String EXTRA_TYPE = "d";

    public static MultiDialogFragment newInstance(int title, String[] item, boolean[] check, int type) {
        MultiDialogFragment fragment = new MultiDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TITLE, title);
        bundle.putStringArray(EXTRA_ITEM, item);
        bundle.putBooleanArray(EXTRA_CHECK, check);
        bundle.putInt(EXTRA_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

}
