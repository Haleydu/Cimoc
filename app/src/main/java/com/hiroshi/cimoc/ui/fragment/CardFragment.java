package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.CardAdapter;
import com.hiroshi.cimoc.ui.view.CardView;
import com.hiroshi.cimoc.utils.DialogUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public abstract class CardFragment extends BaseFragment implements CardView,
        BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener, CardAdapter.OnItemCheckedListener {

    @BindView(R.id.card_recycler_view) RecyclerView mRecyclerView;

    protected AlertDialog mProgressDialog;

    @Override
    protected void initView() {
        mProgressDialog = DialogUtils.buildCancelableFalseDialog(getActivity(), R.string.dialog_doing);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @OnClick(R.id.card_action_button) void onClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_card, null);
        final EditText editText = (EditText) view.findViewById(R.id.card_edit_text);
        builder.setTitle(getActionTitle());
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onActionConfirm(editText.getText().toString());
            }
        });
        builder.show();
    }

    protected abstract void onActionConfirm(String text);

    protected abstract @StringRes int getActionTitle();

    @Override
    public void onCardLoadFail() {
        showSnackbar(R.string.card_load_fail);
        onInitSuccess();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_card;
    }

}
