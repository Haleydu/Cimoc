package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.CimocPresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.view.BaseView;
import com.hiroshi.cimoc.utils.DialogUtils;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class CimocFragment extends BaseFragment implements BaseView {

    @BindView(R.id.main_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.main_keyword_input) EditText mEditText;
    @BindView(R.id.main_search_btn) FloatingActionButton mSearchBtn;

    private CimocPresenter mPresenter;

    private int choice;

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @OnClick(R.id.main_search_btn) void onClick() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.cimoc_empty_error));
        } else {
            int sid = mPresenter.getSid(choice);
            if (sid == -1) {
                showSnackbar(R.string.source_source_none);
            } else {
                startActivity(ResultActivity.createIntent(getActivity(), keyword, mPresenter.getSid(choice)));
            }
        }
    }

    @OnLongClick(R.id.main_search_btn) boolean onLongClick() {
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.cimoc_select_source, mPresenter.load(), choice,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choice = which;
                    }
                }, null).show();
        return true;
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
    protected void initData() {
        choice = 0;
        mPresenter.load();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_cimoc;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new CimocPresenter();
        mPresenter.attachView(this);
    }

}
