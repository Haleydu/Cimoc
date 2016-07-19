package com.hiroshi.cimoc.ui.fragment;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class CimocFragment extends BaseFragment {

    @BindView(R.id.main_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.main_keyword_input) EditText mEditText;
    @BindView(R.id.main_search_btn) FloatingActionButton mSearchBtn;

    @OnClick(R.id.main_search_btn) void onClick() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.empty_for_search));
        } else {
            startActivity(ResultActivity.createIntent(getActivity(), keyword, Kami.SOURCE_DMZJ));
        }
    }

    @Override
    protected void initView() {
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
    protected int getLayoutView() {
        return R.layout.fragment_cimoc;
    }

    @Override
    protected void initPresenter() {}

    @Override
    protected BasePresenter getPresenter() {
        return null;
    }
}
