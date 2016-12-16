package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.hiroshi.cimoc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/12/14.
 */

public class AutoCompleteAdapter extends ArrayAdapter<String> {

    private CustomFilter mFilter;

    public AutoCompleteAdapter(Context context) {
        super(context, R.layout.item_spinner);
        mFilter = new CustomFilter();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            List<String> values = new ArrayList<>();
            for (int i = 0; i != getCount(); ++i) {
                values.add(getItem(i));
            }
            result.values = values;
            result.count = values.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

}
