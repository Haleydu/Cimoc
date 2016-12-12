package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.ArrayAdapter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.Category;
import com.hiroshi.cimoc.model.Pair;

import java.util.List;

import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/12/11.
 */

public class CategoryActivity extends BackActivity {

    @BindViews({R.id.category_spinner_subject, R.id.category_spinner_area, R.id.category_spinner_reader,
            R.id.category_spinner_year, R.id.category_spinner_progress, R.id.category_spinner_order})
    List<AppCompatSpinner> mSpinnerList;
    @BindViews({R.id.category_subject, R.id.category_area, R.id.category_reader,
            R.id.category_year, R.id.category_progress, R.id.category_order})
    List<View> mCategoryView;

    private Category mCategory;

    @Override
    protected void initView() {
        //int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        int source = SourceManager.SOURCE_DMZJ;
        mCategory = SourceManager.getParser(source).getCategory();
        initSpinner();
    }

    @Override
    protected void initData() {
        super.initData();
    }

    private void initSpinner() {
        int[] type = new int[]{Category.CATEGORY_SUBJECT, Category.CATEGORY_AREA, Category.CATEGORY_READER,
                Category.CATEGORY_YEAR, Category.CATEGORY_PROGRESS, Category.CATEGORY_ORDER};
        for (int i = 0; i != type.length; ++i) {
             if (mCategory.hasAttribute(type[i])) {
                mCategoryView.get(i).setVisibility(View.VISIBLE);
                mSpinnerList.get(i).setAdapter(new ArrayAdapter<>(this, R.layout.item_category, mCategory.getAttrList(type[i])));
            }
        }
    }

    @OnClick(R.id.category_action_button) void onActionButtonClick() {
        String[] args = new String[mSpinnerList.size()];
        for (int i = 0; i != args.length; ++i) {
            args[i] = getSpinnerValue(mSpinnerList.get(i));
        }
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        String format = mCategory.getFormat(args);
        Intent intent = ResultActivity.createIntent(this, format, source, ResultActivity.LAUNCH_TYPE_CATEGORY);
        startActivity(intent);
    }

    @SuppressWarnings("unchecked")
    private String getSpinnerValue(AppCompatSpinner spinner) {
        if (!spinner.isShown()) {
            return null;
        }
        return ((Pair<String, String>) spinner.getSelectedItem()).second;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_category;
    }

    private static final String EXTRA_SOURCE = "a";

    public static Intent createIntent(Context context, int source) {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

}
