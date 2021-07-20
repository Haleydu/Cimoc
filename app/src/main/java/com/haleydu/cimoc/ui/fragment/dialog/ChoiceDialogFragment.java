package com.haleydu.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class ChoiceDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private String[] mItems;

    public static ChoiceDialogFragment newInstance(int title, String[] item, int choice, int requestCode) {
        ChoiceDialogFragment fragment = new ChoiceDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putStringArray(DialogCaller.EXTRA_DIALOG_ITEMS, item);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS, choice);
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mItems = getArguments().getStringArray(DialogCaller.EXTRA_DIALOG_ITEMS);
        int choice = getArguments().getInt(DialogCaller.EXTRA_DIALOG_CHOICE_ITEMS);
        builder.setTitle(getArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setSingleChoiceItems(mItems, choice, null)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int index = ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition();
        String value = index == -1 ? null : mItems[index];
        int requestCode = getArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_RESULT_INDEX, index);
        bundle.putString(DialogCaller.EXTRA_DIALOG_RESULT_VALUE, value);
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

}
