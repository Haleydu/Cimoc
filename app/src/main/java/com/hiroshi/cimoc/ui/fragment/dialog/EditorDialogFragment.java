package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.view.DialogView;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/10/15.
 */

public class EditorDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private EditText mEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_editor, null);
        mEditText = ButterKnife.findById(view, R.id.dialog_editor_text);
        mEditText.setText(getArguments().getString(DialogView.EXTRA_DIALOG_CONTENT));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(DialogView.EXTRA_DIALOG_TITLE))
                .setView(view)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = getArguments().getInt(DialogView.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putString(DialogView.EXTRA_DIALOG_RESULT_VALUE, mEditText.getText().toString());
        DialogView target = (DialogView) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

    public static EditorDialogFragment newInstance(int title, String content, int requestCode) {
        EditorDialogFragment fragment = new EditorDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogView.EXTRA_DIALOG_TITLE, title);
        bundle.putString(DialogView.EXTRA_DIALOG_CONTENT, content);
        bundle.putInt(DialogView.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

}
