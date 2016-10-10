package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Card;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public class CardAdapter<T extends Card> extends BaseAdapter<T> {

    private OnItemCheckedListener mOnItemCheckedListener;

    class CardHolder extends BaseViewHolder {
        @BindView(R.id.card_title) TextView cardTitle;
        @BindView(R.id.card_switch) SwitchCompat cardSwitch;

        CardHolder(final View view) {
            super(view);
            cardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mOnItemCheckedListener.onItemCheckedListener(isChecked, getAdapterPosition());
                }
            });
        }
    }

    public CardAdapter(Context context, List<T> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_card, parent, false);
        return new CardHolder(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Card card = mDataSet.get(position);
        CardHolder viewHolder = (CardHolder) holder;
        viewHolder.cardTitle.setText(card.getTitle());
        viewHolder.cardSwitch.setChecked(card.getEnable());
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(offset, offset, offset, 0);
            }
        };
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mOnItemCheckedListener = listener;
    }

    public interface OnItemCheckedListener {
        void onItemCheckedListener(boolean isChecked, int position);
    }

    public boolean contain(int type) {
        for (Card card : mDataSet) {
            if (card.getType() == type) {
                return true;
            }
        }
        return false;
    }

}
