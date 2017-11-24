package com.hw.szoftarch.worklogger.workinghour;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.entities.WorkingHour;
import com.hw.szoftarch.worklogger.recycler_tools.DeleteCallback;
import com.hw.szoftarch.worklogger.recycler_tools.ItemSwipeHelperAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class WorkingHourAdapter extends RecyclerView.Adapter<WorkingHourAdapter.ItemViewHolder>
        implements ItemSwipeHelperAdapter {
    private List<WorkingHour> mWorkingHours = new ArrayList<>();
    private DeleteCallback mDeleteCallback;
    private boolean needToNotifyItem = true;

    WorkingHourAdapter(final DeleteCallback deleteCallback) {
        this.mDeleteCallback = deleteCallback;
    }

    WorkingHour getItem(final int position) {
        return mWorkingHours.get(position);
    }

    void setNotNeedToNotify() {
        needToNotifyItem = false;
    }

    void notifyItemChangedIfNeeded(final int position) {
        if (needToNotifyItem) {
            notifyItemChanged(position);
        }
        needToNotifyItem = true;
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView startDate, workDuration, issueName;

        ItemViewHolder(View view) {
            super(view);
            startDate = view.findViewById(R.id.start_date);
            workDuration = view.findViewById(R.id.work_duration);
            issueName = view.findViewById(R.id.issue_name);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.working_hours_rowitem, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final WorkingHour workingHour = mWorkingHours.get(position);

        holder.startDate.setText(workingHour.getFormattedDate());
        holder.workDuration.setText(workingHour.getFormattedDuration());
        holder.issueName.setText(workingHour.getIssueName());
    }

    @Override
    public int getItemCount() {
        return mWorkingHours.size();
    }

    @Override
    public void onItemSwipe(final int position, int direction) {
        if (direction == ItemTouchHelper.END) {
            mDeleteCallback.deleteItem(position);
        } else {
            notifyItemChanged(position);
        }
    }

    void remove(final int positionToDelete) {
        mWorkingHours.remove(positionToDelete);
        notifyItemRemoved(positionToDelete);
    }

    void add(final WorkingHour workingHour) {
        mWorkingHours.add(workingHour);
        Collections.sort(mWorkingHours, WorkingHour.WorkingHourComparator.getInstance());
        notifyDataSetChanged();
    }

    void update(final WorkingHour workingHour) {
        for (int i = 0; i < mWorkingHours.size(); i++) {
            if (mWorkingHours.get(i).getId() == workingHour.getId()) {
                mWorkingHours.set(i, workingHour);
            }
        }
        Collections.sort(mWorkingHours, WorkingHour.WorkingHourComparator.getInstance());
        notifyDataSetChanged();
    }

    void setWorkingHours(final List<WorkingHour> workingHours) {
        mWorkingHours = workingHours;
        Collections.sort(mWorkingHours, WorkingHour.WorkingHourComparator.getInstance());
        notifyDataSetChanged();
    }

    public long getItemId(final int position) {
        return mWorkingHours.get(position).getId();
    }
}
