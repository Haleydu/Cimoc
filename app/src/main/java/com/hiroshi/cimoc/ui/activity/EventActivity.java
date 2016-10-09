package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.EventUtils;

import java.util.List;

import butterknife.BindViews;

/**
 * Created by Hiroshi on 2016/10/9.
 */

public class EventActivity extends BaseActivity {

    @BindViews({ R.id.event_left, R.id.event_top, R.id.event_middle, R.id.event_bottom, R.id.event_right })
    List<Button> mButtonList;

    private int mTempChoice;
    private int[] mChoiceArray;
    private String[] mKeyArray;
    private boolean isLong;

    private PreferenceManager mPreference;

    @Override
    protected void initTheme() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int value = getIntent().getBooleanExtra(EXTRA_IS_PORTRAIT, true) ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(value);
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected void initNight() {}

    @Override
    protected void initProgressBar() {}

    @Override
    protected void initView() {
        mPreference = CimocApplication.getPreferences();
        isLong = getIntent().getBooleanExtra(EXTRA_IS_LONG, false);
        mKeyArray = isLong ? EventUtils.getLongClickEvents() : EventUtils.getClickEvents();
        mChoiceArray = isLong ? EventUtils.getLongClickEventChoice(mPreference) :
                EventUtils.getClickEventChoice(mPreference);
        for (int i = 0; i != 5; ++i) {
            final int index = i;
            mButtonList.get(i).setText(EventUtils.getTitleId(mChoiceArray[i]));
            mButtonList.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEventList(index);
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isLong) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    showEventList(5);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    showEventList(6);
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showEventList(final int index) {
        DialogUtils.buildSingleChoiceDialog(this, R.string.event_select, R.array.event_items, mChoiceArray[index],
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTempChoice = which;
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChoiceArray[index] = mTempChoice;
                        mPreference.putInt(mKeyArray[index], mTempChoice);
                        mButtonList.get(index).setText(EventUtils.getTitleId(mTempChoice));
                    }
                }).show();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_event;
    }

    private static final String EXTRA_IS_LONG = "a";
    private static final String EXTRA_IS_PORTRAIT = "b";

    public static Intent createIntent(Context context, boolean isLong, boolean isPortrait) {
        Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra(EXTRA_IS_LONG, isLong);
        intent.putExtra(EXTRA_IS_PORTRAIT, isPortrait);
        return intent;
    }

}
