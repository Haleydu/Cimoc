package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class ChoiceDialogFragment extends DialogFragment {

    private int choice;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        choice = getArguments().getInt(EXTRA_CHOICE);
        final String[] item = getArguments().getStringArray(EXTRA_ITEM);
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setSingleChoiceItems(item, choice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                choice = which;
                            }
                        })
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (item != null) {
                            String value = choice == -1 ? null : item[choice];
                            if (getTargetFragment() != null) {
                                ((ChoiceDialogListener) getTargetFragment())
                                        .onChoicePositiveClick(getArguments().getInt(EXTRA_TYPE), choice, value);
                            } else {
                                ((ChoiceDialogListener) getActivity())
                                        .onChoicePositiveClick(getArguments().getInt(EXTRA_TYPE), choice, value);
                            }
                        }
                        dismiss();
                    }
                });
        return builder.create();
    }

    public interface ChoiceDialogListener {
        void onChoicePositiveClick(int type, int choice, String value);
    }

    private static final String EXTRA_TITLE = "a";
    private static final String EXTRA_ITEM = "b";
    private static final String EXTRA_CHOICE = "c";
    private static final String EXTRA_TYPE = "d";

    public static ChoiceDialogFragment newInstance(int title, String[] item, int choice, int type) {
        ChoiceDialogFragment fragment = new ChoiceDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TITLE, title);
        bundle.putStringArray(EXTRA_ITEM, item);
        bundle.putInt(EXTRA_CHOICE, choice);
        bundle.putInt(EXTRA_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

}
