package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.component.DialogCaller;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class SliderDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private DiscreteSeekBar mSeekBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_slider, null);
        int[] item = getArguments().getIntArray(DialogCaller.EXTRA_DIALOG_ITEMS);
        mSeekBar = ButterKnife.findById(view, R.id.dialog_slider_bar);
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

    public static SliderDialogFragment newInstance(int title, int min, int max, int progress, int requestCode) {
        SliderDialogFragment fragment = new SliderDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DialogCaller.EXTRA_DIALOG_TITLE, title);
        bundle.putIntArray(DialogCaller.EXTRA_DIALOG_ITEMS, new int[]{ min, max, progress });
        bundle.putInt(DialogCaller.EXTRA_DIALOG_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);
        return fragment;
    }

}
