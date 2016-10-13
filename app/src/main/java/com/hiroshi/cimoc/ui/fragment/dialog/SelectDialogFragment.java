package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.SelectAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class SelectDialogFragment extends DialogFragment implements BaseAdapter.OnItemClickListener {

    private SelectAdapter mSelectAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getInt(EXTRA_TITLE))
                .setView(initView())
                .setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getTargetFragment() != null) {
                            ((SelectDialogListener) getTargetFragment())
                                    .onSelectPositiveClick(getArguments().getInt(EXTRA_TYPE), mSelectAdapter.getDateSet());
                        } else {
                            ((SelectDialogListener) getActivity())
                                    .onSelectPositiveClick(getArguments().getInt(EXTRA_TYPE), mSelectAdapter.getDateSet());
                        }
                        dismiss();
                    }
                });
        if (getArguments().getInt(EXTRA_NEUTRAL) != -1) {
            builder.setNeutralButton(getArguments().getInt(EXTRA_NEUTRAL), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getTargetFragment() != null) {
                        ((SelectDialogListener) getTargetFragment())
                                .onSelectNeutralClick(getArguments().getInt(EXTRA_TYPE), mSelectAdapter.getDateSet());
                    } else {
                        ((SelectDialogListener) getActivity())
                                .onSelectNeutralClick(getArguments().getInt(EXTRA_TYPE), mSelectAdapter.getDateSet());
                    }
                    dismiss();
                }
            });
        }
        return builder.create();
    }

    private View initView() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select, null);
        List<Selectable> list = getArguments().getParcelableArrayList(EXTRA_LIST);
        mSelectAdapter = new SelectAdapter(getActivity(), list);
        mSelectAdapter.setOnItemClickListener(this);
        RecyclerView recyclerView = ButterKnife.findById(view, R.id.dialog_select_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mSelectAdapter);
        return view;
    }

    @Override
    public void onItemClick(View view, int position) {
        Selectable selectable = mSelectAdapter.getItem(position);
        if (!selectable.isDisable()) {
            boolean checked = !selectable.isChecked();
            selectable.setChecked(checked);
            mSelectAdapter.notifyItemChanged(position);
        }
    }

    public interface SelectDialogListener {
        void onSelectPositiveClick(int type, List<Selectable> list);
        void onSelectNeutralClick(int type, List<Selectable> list);
    }

    private static final String EXTRA_LIST = "a";
    private static final String EXTRA_TITLE = "b";
    private static final String EXTRA_NEUTRAL = "c";
    private static final String EXTRA_TYPE = "d";

    public static SelectDialogFragment newInstance(ArrayList<Selectable> list, int title) {
        return newInstance(list, title, -1);
    }

    public static SelectDialogFragment newInstance(ArrayList<Selectable> list, int title, int neutral) {
        return newInstance(list, title, neutral, -1);
    }

    public static SelectDialogFragment newInstance(ArrayList<Selectable> list, int title, int neutral, int type) {
        SelectDialogFragment fragment = new SelectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_LIST, list);
        bundle.putInt(EXTRA_TITLE, title);
        bundle.putInt(EXTRA_NEUTRAL, neutral);
        bundle.putInt(EXTRA_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

}
