package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.CimocPresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.view.CimocView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class CimocFragment extends BaseFragment implements CimocView {

    @BindView(R.id.main_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.main_keyword_input) EditText mEditText;
    @BindView(R.id.main_search_btn) FloatingActionButton mSearchBtn;

    private CimocPresenter mPresenter;
    private List<Source> mSourceList;
    private Set<Integer> mFilterSet;

    @Override
    protected void initPresenter() {
        mPresenter = new CimocPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mInputLayout.setError(null);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mSearchBtn.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.cimoc_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cimoc_filter:
                int size = mSourceList.size();
                boolean[] select = new boolean[size];
                String[] title = new String[size];
                for (int i = 0; i != size; ++i) {
                    int sid = mSourceList.get(i).getSid();
                    select[i] = mFilterSet.contains(sid);
                    title[i] = SourceManager.getTitle(sid);
                }
                DialogUtils.buildMultiChoiceDialog(getActivity(), R.string.cimoc_filter, title, select,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    mFilterSet.add(mSourceList.get(which).getSid());
                                } else {
                                    mFilterSet.remove(mSourceList.get(which).getSid());
                                }
                            }
                        }, -1, null, null).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.main_search_btn) void onClick() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.cimoc_empty_error));
        } else {
            ArrayList<Integer> list = new ArrayList<>(10);
            for (Source source : mSourceList) {
                int sid = source.getSid();
                if (mFilterSet.contains(sid)) {
                    list.add(sid);
                }
            }
            if (list.isEmpty()) {
                showSnackbar(R.string.source_source_none);
            } else {
                startActivity(ResultActivity.createIntent(getActivity(), keyword, list));
            }
        }
    }

    @Override
    public void onLoadSuccess(List<Source> list) {
        if (mSourceList == null) {
            mFilterSet = new HashSet<>(10);
            for (Source source : list) {
                int sid = source.getSid();
                if (sid < 100) {
                    mFilterSet.add(sid);
                }
            }
        }
        mSourceList = list;
    }

    @Override
    public void onLoadFail() {
        showSnackbar(R.string.cimoc_load_fail);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_cimoc;
    }



}
