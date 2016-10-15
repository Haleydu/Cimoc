package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.hiroshi.cimoc.R;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/10/15.
 */

public class EditorDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_editor, null);
        final EditText editText = ButterKnife.findById(view, R.id.dialog_editor_text);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setView(view)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getTargetFragment() != null) {
                            ((EditorDialogListener) getTargetFragment()).onEditorPositiveClick(editText.getText().toString());
                        } else {
                            ((EditorDialogListener) getActivity()).onEditorPositiveClick(editText.getText().toString());
                        }
                        dismiss();
                    }
                });
        return builder.create();
    }

    public interface EditorDialogListener {
        void onEditorPositiveClick(String text);
    }

    private static final String EXTRA_TITLE = "a";

    public static EditorDialogFragment newInstance(int title) {
        EditorDialogFragment fragment = new EditorDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TITLE, title);
        fragment.setArguments(bundle);
        return fragment;
    }


}
