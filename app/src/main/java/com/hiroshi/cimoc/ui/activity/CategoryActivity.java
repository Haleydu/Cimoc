package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.parser.Category;
import com.hiroshi.cimoc.ui.adapter.CategoryAdapter;

import java.util.List;

import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/12/11.
 */

public class CategoryActivity extends BackActivity implements AdapterView.OnItemSelectedListener {

    @BindViews({R.id.category_spinner_subject, R.id.category_spinner_area, R.id.category_spinner_reader,
            R.id.category_spinner_year, R.id.category_spinner_progress, R.id.category_spinner_order})
    List<AppCompatSpinner> mSpinnerList;
    @BindViews({R.id.category_subject, R.id.category_area, R.id.category_reader,
            R.id.category_year, R.id.category_progress, R.id.category_order})
    List<View> mCategoryView;

    private Category mCategory;

    @Override
    protected void initView() {
        int source = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        if (mToolbar != null) {
            mToolbar.setTitle(getIntent().getStringExtra(Extra.EXTRA_KEYWORD));
        }
        mCategory = SourceManager.getInstance(this).getParser(source).getCategory();
        initSpinner();
    }

    private void initSpinner() {
        int[] type = new int[]{Category.CATEGORY_SUBJECT, Category.CATEGORY_AREA, Category.CATEGORY_READER,
                Category.CATEGORY_YEAR, Category.CATEGORY_PROGRESS, Category.CATEGORY_ORDER};
        for (int i = 0; i != type.length; ++i) {
             if (mCategory.hasAttribute(type[i])) {
                 mCategoryView.get(i).setVisibility(View.VISIBLE);
                 if (!mCategory.isComposite()) {
                     mSpinnerList.get(i).setOnItemSelectedListener(this);
                 }
                 mSpinnerList.get(i).setAdapter(new CategoryAdapter(this, mCategory.getAttrList(type[i])));
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        for (AppCompatSpinner spinner : mSpinnerList) {
            if (position == 0) {
                spinner.setEnabled(true);
            } else if (!parent.equals(spinner)) {
                spinner.setEnabled(false);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @OnClick(R.id.category_action_button) void onActionButtonClick() {
        String[] args = new String[mSpinnerList.size()];
        for (int i = 0; i != args.length; ++i) {
            args[i] = getSpinnerValue(mSpinnerList.get(i));
        }
        int source = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        String format = mCategory.getFormat(args);
        Intent intent = ResultActivity.createIntent(this, format, source, ResultActivity.LAUNCH_MODE_CATEGORY);
        startActivity(intent);
    }

    private String getSpinnerValue(AppCompatSpinner spinner) {
        if (!spinner.isShown()) {
            return null;
        }
        return ((CategoryAdapter) spinner.getAdapter()).getValue(spinner.getSelectedItemPosition());
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.category);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_category;
    }

    public static Intent createIntent(Context context, int source, String title) {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra(Extra.EXTRA_SOURCE, source);
        intent.putExtra(Extra.EXTRA_KEYWORD, title);
        return intent;
    }

}
