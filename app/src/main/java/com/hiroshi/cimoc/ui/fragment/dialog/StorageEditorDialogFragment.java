package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.ui.activity.DirPickerActivity;

/**
 * Created by Hiroshi on 2016/12/5.
 */

public class StorageEditorDialogFragment extends EditorDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = (AlertDialog) super.onCreateDialog(savedInstanceState);
        mEditText.setEnabled(false);
        String title = getString(R.string.settings_other_storage_edit_neutral);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                int requestCode = getArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        getActivity().startActivityForResult(intent, requestCode);
                    } catch (ActivityNotFoundException e) {
                        ((DialogCaller) getActivity()).onDialogResult(requestCode, null);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), DirPickerActivity.class);
                    getActivity().startActivityForResult(intent, requestCode);
                }
            }
        });
        return dialog;
    }

    public static StorageEditorDialogFragment newInstance(int title, String content, int requestCode) {
        StorageEditorDialogFragment fragment = new StorageEditorDialogFragment();
        fragment.setArguments(createBundle(title, content, requestCode));
        return fragment;
    }

}
