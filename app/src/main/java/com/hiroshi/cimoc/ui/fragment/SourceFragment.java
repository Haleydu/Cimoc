package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SourcePresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.SourceAdapter;
import com.hiroshi.cimoc.utils.DialogFactory;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends BaseFragment {

    @BindView(R.id.source_recycler_view) RecyclerView mRecyclerView;

    private SourceAdapter mSourceAdapter;
    private SourcePresenter mPresenter;

    @Override
    protected void initView() {
        mSourceAdapter = new SourceAdapter(getActivity(), mPresenter.list());
        mSourceAdapter.setOnItemLongClickListener(new BaseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogFactory.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.source_delete_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPresenter.delete(mSourceAdapter.getItem(position));
                                mSourceAdapter.remove(position);
                            }
                        }).show();
            }
        });
        mSourceAdapter.setOnItemCheckedListener(new SourceAdapter.OnItemCheckedListener() {
            @Override
            public void onItemCheckedListener(boolean isChecked, int position) {
                Source source = mSourceAdapter.getItem(position);
                source.setEnable(isChecked);
                mPresenter.update(source);
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setAdapter(mSourceAdapter);
        mRecyclerView.addItemDecoration(mSourceAdapter.getItemDecoration());
    }

    @OnClick(R.id.source_add_btn) void onSourceAddClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_source, null);
        final EditText editText = (EditText) view.findViewById(R.id.source_edit_text);
        builder.setTitle(R.string.source_add);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int sid = SourceManager.getId(editText.getText().toString());
                if (sid == -1) {
                    showSnackbar(R.string.source_add_error);
                } else {
                    mPresenter.add(sid);
                }
            }
        });
        builder.show();
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new SourcePresenter(this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_source;
    }

    public void addItem(Source source) {
        mSourceAdapter.add(source);
    }

}
