package com.hiroshi.cimoc.ui.fragment.dialog;

import android.app.DialogFragment;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Hiroshi on 2016/10/14.
 */

public class ProgressDialogFragment extends DialogFragment {

    @BindView(R.id.dialog_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.dialog_progress_text) TextView mTextView;

    private Unbinder unbinder;
    private CompositeSubscription mCompositeSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_progress, container, false);
        unbinder = ButterKnife.bind(this, view);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        int resId = ThemeUtils.getResourceId(getActivity(), R.attr.colorAccent);
        mProgressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getActivity(), resId), PorterDuff.Mode.SRC_ATOP);
        mCompositeSubscription = new CompositeSubscription();
        mCompositeSubscription.add(RxBus.getInstance().toObservable(RxEvent.EVENT_DIALOG_PROGRESS).subscribe(new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mTextView.setText((String) rxEvent.getData());
            }
        }));
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.unsubscribe();
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    public static ProgressDialogFragment newInstance() {
        return new ProgressDialogFragment();
    }

}
