package com.hiroshi.cimoc.ui.fragment;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Administrator;
import com.hiroshi.cimoc.core.IKanman;
import com.hiroshi.cimoc.ui.activity.ResultActivity;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MainFragment extends BaseFragment {

    @BindView(R.id.main_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.main_keyword_input) EditText mEditText;

    @OnClick(R.id.main_search_btn) void search() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.empty_for_search));
        } else {
            startActivity(ResultActivity.createIntent(getActivity(), keyword, Administrator.SOURCE_IKANMAN));
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
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_main;
    }



}
