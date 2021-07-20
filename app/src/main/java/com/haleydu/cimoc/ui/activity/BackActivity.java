package com.haleydu.cimoc.ui.activity;

import android.graphics.PorterDuff;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ProgressBar;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.utils.ThemeUtils;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/11.
 */
public abstract class BackActivity extends BaseActivity {

    @Nullable
    @BindView(R.id.custom_progress_bar)
    ProgressBar mProgressBar;

    @Override
    protected void initToolbar() {
        super.initToolbar();
        if (mToolbar != null) {
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    protected void initView() {
        if (mProgressBar != null) {
            int resId = ThemeUtils.getResourceId(this, R.attr.colorAccent);
            mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, resId), PorterDuff.Mode.SRC_ATOP);
        }
    }

    protected boolean isProgressBarShown() {
        return mProgressBar != null && mProgressBar.isShown();
    }

    protected void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

}
