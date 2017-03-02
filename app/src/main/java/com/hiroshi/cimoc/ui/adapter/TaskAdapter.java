package com.hiroshi.cimoc.ui.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskAdapter extends BaseAdapter<Task> {

    private String last;
    private int colorId;

    static class TaskHolder extends BaseViewHolder {
        @BindView(R.id.task_page) TextView taskPage;
        @BindView(R.id.task_title) TextView taskTitle;
        @BindView(R.id.task_state) TextView taskState;
        @BindView(R.id.task_progress) ProgressBar taskProgress;
        @BindView(R.id.task_last) View taskLast;

        TaskHolder(View view, int color) {
            super(view);
            taskProgress.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public TaskAdapter(Context context, List<Task> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_task, parent, false);
        return new TaskHolder(view, ContextCompat.getColor(mContext, colorId));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Task task = mDataSet.get(position);
        TaskHolder viewHolder = (TaskHolder) holder;
        viewHolder.taskTitle.setText(task.getTitle());
        viewHolder.taskState.setText(getState(task));
        int progress = task.getProgress();
        int max = task.getMax();
        viewHolder.taskPage.setText(StringUtils.getProgress(progress, max));
        viewHolder.taskProgress.setMax(max);
        viewHolder.taskProgress.setProgress(progress);
        if (task.getPath().equals(last)) {
            viewHolder.taskLast.setVisibility(View.VISIBLE);
        } else {
            viewHolder.taskLast.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int offset = parent.getWidth() / 90;
                outRect.set(0, 0, 0, offset);
            }
        };
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void setLast(String value) {
        if (value == null || value.equals(last)) {
            return;
        }
        String temp = last;
        last = value;
        for (int i = 0; i != mDataSet.size(); ++i) {
            String path = mDataSet.get(i).getPath();
            if (path.equals(last)) {
                notifyItemChanged(i);
            } else if (path.equals(temp)) {
                notifyItemChanged(i);
            }
        }
    }

    public int getPositionById(long id) {
        int size = mDataSet.size();
        for (int i = 0; i != size; ++i) {
            if (mDataSet.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public void removeById(List<Long> list) {
        Set<Long> set = new HashSet<>(list);
        Iterator<Task> it = mDataSet.iterator();
        while (it.hasNext()) {
            Task task = it.next();
            if (set.contains(task.getId())) {
                it.remove();
            }
        }
        notifyDataSetChanged();
    }

    private int getState(Task task) {
        switch (task.getState()) {
            default:
            case Task.STATE_PAUSE:
                return R.string.task_pause;
            case Task.STATE_PARSE:
                return R.string.task_parse;
            case Task.STATE_DOING:
                return R.string.task_doing;
            case Task.STATE_FINISH:
                return R.string.task_finish;
            case Task.STATE_WAIT:
                return R.string.task_wait;
            case Task.STATE_ERROR:
                return R.string.task_error;
        }
    }

}
