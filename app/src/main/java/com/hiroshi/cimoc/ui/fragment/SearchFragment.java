package com.hiroshi.cimoc.ui.fragment;

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
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SearchPresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.MultiDialogFragment;
import com.hiroshi.cimoc.ui.view.SearchView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchFragment extends BaseFragment implements SearchView,
        TextView.OnEditorActionListener, MultiDialogFragment.MultiDialogListener {

    @BindView(R.id.search_frame_layout) View mFrameLayout;
    @BindView(R.id.search_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.search_keyword_input) EditText mEditText;
    @BindView(R.id.search_action_button) FloatingActionButton mActionButton;

    private SearchPresenter mPresenter;
    private List<Source> mSourceList;

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
        } else {
            mActionButton.show();
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
                String[] arr1 = new String[size];
                boolean[] arr2 = new boolean[size];
                for (int i = 0; i < size; ++i) {
                    arr1[i] = mSourceList.get(i).getTitle();
                    arr2[i] = mSourceList.get(i).getCheck();
                }
                MultiDialogFragment fragment =
                        MultiDialogFragment.newInstance(R.string.search_source_select, arr1, arr2, -1);
                fragment.setTargetFragment(this, 0);
                fragment.show(getFragmentManager(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMultiPositiveClick(int type, boolean[] check) {
        int size = mSourceList.size();
        for (int i = 0; i < size; ++i) {
            mSourceList.get(i).setCheck(check[i]);
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
            for (Source source : mSourceList) {
                if (source.getCheck()) {
                    list.add(source.getType());
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
        mSourceList = new ArrayList<>(list);
        hideProgressBar();
    }

    @Override
    public void onSourceLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.search_source_load_fail);
    }

    @Override
    public void onSourceEnable(Source source) {
        mSourceList.remove(source);
        mSourceList.add(source);
    }

    @Override
    public void onSourceDisable(Source source) {
        mSourceList.remove(source);
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
