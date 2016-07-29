package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Kami;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class CimocFragment extends BaseFragment {

    @BindView(R.id.main_text_layout) TextInputLayout mInputLayout;
    @BindView(R.id.main_keyword_input) EditText mEditText;
    @BindView(R.id.main_search_btn) FloatingActionButton mSearchBtn;

    private int[] source;
    private int choice;
    private int array;

    @OnClick(R.id.main_search_btn) void onClick() {
        String keyword = mEditText.getText().toString();
        if (keyword.isEmpty()) {
            mInputLayout.setError(getString(R.string.empty_for_search));
        } else {
            startActivity(ResultActivity.createIntent(getActivity(), keyword, source[choice]));
        }
    }

    @OnLongClick(R.id.main_search_btn) boolean onLongClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog_Alert);
        builder.setTitle("图源选择");
        builder.setSingleChoiceItems(array, choice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                choice = which;
            }
        });
        builder.show();
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
        choice = 0;
        boolean enable = CimocApplication.getPreferences().getBoolean(CimocApplication.PREF_EX, false);
        array = enable ? R.array.ex_source_items : R.array.source_items;
        if (enable) {
            source = new int[]{ Kami.SOURCE_IKANMAN, Kami.SOURCE_DMZJ, Kami.SOURCE_HHAAZZ, Kami.SOURCE_CCTUKU, Kami.SOURCE_EHENTAI };
        } else {
            source = new int[]{ Kami.SOURCE_IKANMAN, Kami.SOURCE_DMZJ, Kami.SOURCE_HHAAZZ, Kami.SOURCE_CCTUKU };
        }
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
