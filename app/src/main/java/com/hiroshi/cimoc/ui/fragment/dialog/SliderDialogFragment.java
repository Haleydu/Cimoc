package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.hiroshi.cimoc.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/10/16.
 */

public class SliderDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_slider, null);
        final DiscreteSeekBar seekBar = ButterKnife.findById(view, R.id.dialog_slider_bar);
        seekBar.setMin(getArguments().getInt(EXTRA_MIN));
        seekBar.setMax(getArguments().getInt(EXTRA_MAX));
        seekBar.setProgress(getArguments().getInt(EXTRA_MAX));
        seekBar.setProgress(getArguments().getInt(EXTRA_PROGRESS));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setView(view)
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getTargetFragment() != null) {
                            ((SliderDialogListener) getTargetFragment()).onSliderPositiveClick(seekBar.getProgress());
                        } else {
                            ((SliderDialogListener) getActivity()).onSliderPositiveClick(seekBar.getProgress());
                        }
                        dismiss();
                    }
                });
        return builder.create();
    }

    public interface SliderDialogListener {
        void onSliderPositiveClick(int value);
    }

    private static final String EXTRA_TITLE = "a";
    private static final String EXTRA_MIN = "b";
    private static final String EXTRA_MAX = "c";
    private static final String EXTRA_PROGRESS = "d";

    public static SliderDialogFragment newInstance(int title, int min, int max, int progress) {
        SliderDialogFragment fragment = new SliderDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TITLE, title);
        bundle.putInt(EXTRA_MIN, min);
        bundle.putInt(EXTRA_MAX, max);
        bundle.putInt(EXTRA_PROGRESS, progress);
        fragment.setArguments(bundle);
        return fragment;
    }

}
