package com.hiroshi.cimoc.ui.activity.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.global.ClickEvents;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.ui.activity.BaseActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;

import java.util.List;

import butterknife.BindViews;

/**
 * Created by Hiroshi on 2016/10/9.
 */

public class EventSettingsActivity extends BaseActivity implements DialogCaller {

    @BindViews({ R.id.event_left, R.id.event_top, R.id.event_middle, R.id.event_bottom, R.id.event_right })
    List<Button> mButtonList;

    private int[] mChoiceArray;
    private String[] mKeyArray;
    private boolean isLong;

    @Override
    protected void initTheme() {
        super.initTheme();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        int value = getIntent().getBooleanExtra(Extra.EXTRA_IS_PORTRAIT, true) ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(value);
    }

    @Override
    protected void initView() {
        boolean isStream = getIntent().getBooleanExtra(Extra.EXTRA_IS_STREAM, false);
        isLong = getIntent().getBooleanExtra(Extra.EXTRA_IS_LONG, false);
        if (isStream) {
            mKeyArray = isLong ? ClickEvents.getStreamLongClickEvents() : ClickEvents.getStreamClickEvents();
            mChoiceArray = isLong ? ClickEvents.getStreamLongClickEventChoice(mPreference) :
                    ClickEvents.getStreamClickEventChoice(mPreference);
        } else {
            mKeyArray = isLong ? ClickEvents.getPageLongClickEvents() : ClickEvents.getPageClickEvents();
            mChoiceArray = isLong ? ClickEvents.getPageLongClickEventChoice(mPreference) :
                    ClickEvents.getPageClickEventChoice(mPreference);
        }

        for (int i = 0; i != 5; ++i) {
            final int index = i;
            mButtonList.get(i).setText(ClickEvents.getEventTitle(this, mChoiceArray[i]));
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

    private void showEventList(int index) {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.event_select,
                ClickEvents.getEventTitleArray(this), mChoiceArray[index], index);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
        mChoiceArray[requestCode] = index;
        mPreference.putInt(mKeyArray[requestCode], index);
        if (requestCode < 5) {
            mButtonList.get(requestCode).setText(ClickEvents.getEventTitle(this, index));
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_event;
    }



    public static Intent createIntent(Context context, boolean isLong, boolean isPortrait, boolean isStream) {
        Intent intent = new Intent(context, EventSettingsActivity.class);
        intent.putExtra(Extra.EXTRA_IS_LONG, isLong);
        intent.putExtra(Extra.EXTRA_IS_PORTRAIT, isPortrait);
        intent.putExtra(Extra.EXTRA_IS_STREAM, isStream);
        return intent;
    }

}
