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
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SearchPresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.view.SearchView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchFragment extends BaseFragment implements SearchView,
        TextView.OnEditorActionListener, DialogInterface.OnMultiChoiceClickListener {

    @BindView(R.id.search_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.search_keyword_input) EditText mEditText;
    @BindView(R.id.search_action_button) FloatingActionButton mSearchBtn;

    private SearchPresenter mPresenter;
    private List<Source> mSourceList;
    private Set<Integer> mFilterSet;

    @Override
    protected void initPresenter() {
        mPresenter = new SearchPresenter();
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
        mEditText.setOnEditorActionListener(this);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            mPresenter.load();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_menu_source:
                int size = mSourceList.size();
                boolean[] select = new boolean[size];
                String[] title = new String[size];
                for (int i = 0; i != size; ++i) {
                    select[i] = mFilterSet.contains(mSourceList.get(i).getType());
                    title[i] = mSourceList.get(i).getTitle();
                }
                DialogUtils.buildMultiChoiceDialog(getActivity(), R.string.search_source_select,
                        title, select, this, -1, null, null).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (isChecked) {
            mFilterSet.add(mSourceList.get(which).getType());
        } else {
            mFilterSet.remove(mSourceList.get(which).getType());
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mSearchBtn.performClick();
            return true;
        }
        return false;
    }

    @OnClick(R.id.search_action_button) void onSearchButtonClick() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.search_keyword_empty));
        } else {
            ArrayList<Integer> list = new ArrayList<>(10);
            for (Source source : mSourceList) {
                int type = source.getType();
                if (mFilterSet.contains(type)) {
                    list.add(type);
                }
            }
            if (list.isEmpty()) {
                showSnackbar(R.string.search_source_none);
            } else {
                startActivity(ResultActivity.createIntent(getActivity(), keyword, list));
            }
        }
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        if (mSourceList == null) {
            mFilterSet = new HashSet<>(10);
            for (Source source : list) {
                int type = source.getType();
                if (type < 100) {
                    mFilterSet.add(type);
                }
            }
        }
        mSourceList = list;
    }

    @Override
    public void onSourceLoadFail() {
        showSnackbar(R.string.search_source_load_fail);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_search;
    }

}
