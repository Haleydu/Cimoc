package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.misc.Switcher;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SearchPresenter;
import com.hiroshi.cimoc.ui.adapter.AutoCompleteAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.MultiDialogFragment;
import com.hiroshi.cimoc.ui.view.SearchView;
import com.hiroshi.cimoc.utils.CollectionUtils;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchActivity extends BackActivity implements SearchView, TextView.OnEditorActionListener {

    private final static int DIALOG_REQUEST_SOURCE = 0;

    @BindView(R.id.search_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.search_keyword_input) AppCompatAutoCompleteTextView mEditText;
    @BindView(R.id.search_action_button) FloatingActionButton mActionButton;

    private ArrayAdapter<String> mArrayAdapter;

    private SearchPresenter mPresenter;
    private List<Switcher<Source>> mSourceList;
    private boolean mAutoComplete;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new SearchPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        mAutoComplete = mPreference.getBoolean(PreferenceManager.PREF_SEARCH_AUTO_COMPLETE, false);
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
            public void afterTextChanged(Editable s) {
                if (mAutoComplete) {
                    String keyword = mEditText.getText().toString();
                    if (!StringUtils.isEmpty(keyword)) {
                        mPresenter.loadAutoComplete(keyword);
                    }
                }
            }
        });
        mEditText.setOnEditorActionListener(this);
        if (mAutoComplete) {
            mArrayAdapter = new AutoCompleteAdapter(this);
            mEditText.setAdapter(mArrayAdapter);
        }
    }

    @Override
    protected void initData() {
        mSourceList = new ArrayList<>();
        mPresenter.loadSource();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
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
                        arr1[i] = mSourceList.get(i).getElement().getTitle();
                        arr2[i] = mSourceList.get(i).isEnable();
                    }
                    MultiDialogFragment fragment =
                            MultiDialogFragment.newInstance(R.string.search_source_select, arr1, arr2, DIALOG_REQUEST_SOURCE);
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
                        mSourceList.get(i).setEnable(check[i]);
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
        if (StringUtils.isEmpty(keyword)) {
            mInputLayout.setError(getString(R.string.search_keyword_empty));
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            for (Switcher<Source> switcher : mSourceList) {
                if (switcher.isEnable()) {
                    list.add(switcher.getElement().getType());
                }
            }
            if (list.isEmpty()) {
                HintUtils.showToast(this, R.string.search_source_none);
            } else {
                startActivity(ResultActivity.createIntent(this, keyword,
                        CollectionUtils.unbox(list), ResultActivity.LAUNCH_MODE_SEARCH));
            }
        }
    }

    @Override
    public void onAutoCompleteLoadSuccess(List<String> list) {
        mArrayAdapter.clear();
        mArrayAdapter.addAll(list);
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        hideProgressBar();
        for (Source source : list) {
            mSourceList.add(new Switcher<>(source, true));
        }
    }

    @Override
    public void onSourceLoadFail() {
        hideProgressBar();
        HintUtils.showToast(this, R.string.search_source_load_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.comic_search);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_search;
    }

    @Override
    protected boolean isNavTranslation() {
        return true;
    }

}
