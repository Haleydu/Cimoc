package com.haleydu.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.View;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.component.DialogCaller;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;


/**
 * Created by Hiroshi on 2016/10/16.
 */

public class SliderDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private DiscreteSeekBar mSeekBar;

    public static SliderDialogFragment newInstance(int title, int min, int max, int progress, int requestCode) {
        SliderDialogFragment fragment = new SliderDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putIntArray(DialogCaller.EXTRA_DIALOG_ITEMS, new int[]{min, max, progress});
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_slider, null);
        int[] item = getArguments().getIntArray(DialogCaller.EXTRA_DIALOG_ITEMS);
        mSeekBar = view.findViewById(R.id.dialog_slider_bar);
        mSeekBar.setMin(item[0]);
        mSeekBar.setMax(item[1]);
        mSeekBar.setProgress(item[1]);
        mSeekBar.setProgress(item[2]);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(DialogCaller.EXTRA_DIALOG_TITLE))
                .setView(view)
                .setPositiveButton(R.string.dialog_positive, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        int requestCode = getArguments().getInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE);
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_RESULT_VALUE, mSeekBar.getProgress());
        DialogCaller target = (DialogCaller) (getTargetFragment() != null ? getTargetFragment() : getActivity());
        target.onDialogResult(requestCode, bundle);
    }

}
