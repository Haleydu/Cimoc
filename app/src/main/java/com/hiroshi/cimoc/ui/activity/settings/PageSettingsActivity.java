package com.hiroshi.cimoc.ui.activity.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.ui.activity.BackActivity;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/13.
 */

public class PageSettingsActivity extends BackActivity {

    private int mReaderOrientationChoice;
    private int mReaderTurnChoice;
    private int mTriggerNum;
    private int mTempChoice;

    private DialogInterface.OnClickListener mSingleChoiceListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mTempChoice = which;
        }
    };

    @Override
    protected void initProgressBar() {}

    @Override
    protected void initView() {
        mReaderOrientationChoice = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mReaderTurnChoice = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TURN, PreferenceManager.READER_TURN_LTR);
        mTriggerNum = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, 5);
    }

    @OnClick({ R.id.settings_reader_click_event_btn, R.id.settings_reader_long_click_event_btn })
    void onReaderEventBtnClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event_btn;
        Intent intent = EventSettingsActivity.createIntent(this, isLong,
                mReaderOrientationChoice == PreferenceManager.READER_ORIENTATION_PORTRAIT, false);
        startActivity(intent);
    }

    @OnClick(R.id.settings_reader_orientation_btn) void onReaderOrientationBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_reader_orientation, R.array.reader_orientation_items,
                mReaderOrientationChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderOrientationChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_PAGE_ORIENTATION, mReaderOrientationChoice);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_turn_btn) void onReaderTurnBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_reader_turn, R.array.reader_turn_items,
                mReaderTurnChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderTurnChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_PAGE_TURN, mReaderTurnChoice);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_trigger_btn) void onTriggerBtnClick() {
        final String[] array = StringUtils.range(5, 50, 5);
        for (int i = 0; i != array.length; ++i) {
            if (Integer.parseInt(array[i]) == mTriggerNum) {
                DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_trigger, array, i,
                        mSingleChoiceListener, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTriggerNum = Integer.parseInt(array[mTempChoice]);
                                mPreference.putInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, mTriggerNum);
                            }
                        }).show();
            }
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.settings_reader_page_config);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_settings;
    }

}
