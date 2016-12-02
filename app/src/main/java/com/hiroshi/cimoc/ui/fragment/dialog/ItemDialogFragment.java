package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public class ItemDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] item = getArguments().getStringArray(EXTRA_ITEM);
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setItems(item, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int index) {
        if (getTargetFragment() != null) {
            ((ItemDialogListener) getTargetFragment()).onItemPositiveClick(getArguments().getInt(EXTRA_TYPE), index);
        } else {
            ((ItemDialogListener) getActivity()).onItemPositiveClick(getArguments().getInt(EXTRA_TYPE), index);
        }
    }

    public interface ItemDialogListener {
        void onItemPositiveClick(int type, int index);
    }

    private static final String EXTRA_TITLE = "a";
    private static final String EXTRA_ITEM = "b";
    private static final String EXTRA_TYPE = "c";

    public static ItemDialogFragment newInstance(int title, String[] item, int type) {
        ItemDialogFragment fragment = new ItemDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TITLE, title);
        bundle.putStringArray(EXTRA_ITEM, item);
        bundle.putInt(EXTRA_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

}
