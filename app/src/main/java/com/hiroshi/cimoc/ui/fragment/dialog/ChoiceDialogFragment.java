package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.view.DialogView;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class ChoiceDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private String[] mItems;
    private int mChoice;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mItems = getArguments().getStringArray(DialogView.EXTRA_DIALOG_ITEMS);
        mChoice = getArguments().getInt(DialogView.EXTRA_DIALOG_CHOICE_ITEMS);
        builder.setTitle(getArguments().getInt(DialogView.EXTRA_DIALOG_TITLE))
                .setSingleChoiceItems(mItems, mChoice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mChoice = which;
                            }
                        })
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        String value = mChoice == -1 ? null : mItems[mChoice];
        int requestCode = getArguments().getInt(DialogView.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putInt(DialogView.EXTRA_DIALOG_RESULT_INDEX, mChoice);
        bundle.putString(DialogView.EXTRA_DIALOG_RESULT_VALUE, value);
        DialogView target = (DialogView) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

    public static ChoiceDialogFragment newInstance(int title, String[] item, int choice, int requestCode) {
        ChoiceDialogFragment fragment = new ChoiceDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogView.EXTRA_DIALOG_TITLE, title);
        bundle.putStringArray(DialogView.EXTRA_DIALOG_ITEMS, item);
        bundle.putInt(DialogView.EXTRA_DIALOG_CHOICE_ITEMS, choice);
        bundle.putInt(DialogView.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

}
