package com.hiroshi.cimoc.ui.fragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SearchPresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.MultiDialogFragment;
import com.hiroshi.cimoc.ui.view.SearchView;
import com.hiroshi.cimoc.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchFragment extends BaseFragment implements SearchView, TextView.OnEditorActionListener {

    private final static int DIALOG_REQUEST_SOURCE = 0;

    @BindView(R.id.search_frame_layout) View mFrameLayout;
    @BindView(R.id.search_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.search_keyword_input) EditText mEditText;
    @BindView(R.id.search_action_button) FloatingActionButton mActionButton;

    private SearchPresenter mPresenter;
    private List<Pair<Source, Boolean>> mSourceList;

    @Override
    protected void initPresenter() {
        mPresenter = new SearchPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        setHasOptionsMenu(true);
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mActionButton != null && !mActionButton.isShown()) {
                    mActionButton.show();
                }
            }
        });
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
        mSourceList = new ArrayList<>();
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
        if (hidden) {
            mActionButton.hide();
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
                if (!mSourceList.isEmpty()) {
                    int size = mSourceList.size();
                    String[] arr1 = new String[size];
                    boolean[] arr2 = new boolean[size];
                    for (int i = 0; i < size; ++i) {
                        arr1[i] = mSourceList.get(i).first.getTitle();
                        arr2[i] = mSourceList.get(i).second;
                    }
                    MultiDialogFragment fragment =
                            MultiDialogFragment.newInstance(R.string.search_source_select, arr1, arr2, DIALOG_REQUEST_SOURCE);
                    fragment.setTargetFragment(this, 0);
                    fragment.show(getFragmentManager(), null);
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_SOURCE:
                boolean[] check = bundle.getBooleanArray(EXTRA_DIALOG_RESULT_VALUE);
                if (check != null) {
                    int size = mSourceList.size();
                    for (int i = 0; i < size; ++i) {
                        mSourceList.get(i).second = check[i];
                    }
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            mActionButton.performClick();
            return true;
        }
        return false;
    }

    @OnClick(R.id.search_action_button) void onSearchButtonClick() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.search_keyword_empty));
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            for (Pair<Source, Boolean> pair : mSourceList) {
                if (pair.second) {
                    list.add(pair.first.getType());
                }
            }
            if (list.isEmpty()) {
                showSnackbar(R.string.search_source_none);
            } else {
                startActivity(ResultActivity.createIntent(getActivity(), keyword,
                        CollectionUtils.unbox(list), ResultActivity.LAUNCH_TYPE_SEARCH));
            }
        }
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        hideProgressBar();
        for (Source source : list) {
            mSourceList.add(Pair.create(source, true));
        }
    }

    @Override
    public void onSourceLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.search_source_load_fail);
    }

    @Override
    public void onSourceEnable(Source source) {
        mSourceList.add(Pair.create(source, true));
    }

    @Override
    public void onSourceDisable(Source source) {
        Iterator<Pair<Source, Boolean>> iterator = mSourceList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().first.getId().equals(source.getId())) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mFrameLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), primary));
        mInputLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), primary));
        mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_search;
    }

}
