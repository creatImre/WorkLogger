package com.hw.szoftarch.worklogger.workinghour;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.entities.WorkingHour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LogWorkAdapter extends BaseAdapter {
    private Context mContext;
    private List<WorkingHour> mWorkingHours = new ArrayList<>();

    public LogWorkAdapter(final Context context) {
        this.mContext = context;
    }

    public void setWorkingHours(final List<WorkingHour> workingHours) {
        mWorkingHours = workingHours;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mWorkingHours.size();
    }

    @Override
    public Object getItem(int position) {
        return mWorkingHours.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mWorkingHours.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.working_hours_rowitem, parent, false);
        }

        final WorkingHour currentItem = (WorkingHour) getItem(position);

        TextView textViewStartDate = convertView.findViewById(R.id.start_date);
        TextView textViewDuration = convertView.findViewById(R.id.duration);
        TextView textViewIssueName = convertView.findViewById(R.id.issue_name);

        textViewStartDate.setText(currentItem.getFormattedDate());
        textViewDuration.setText((String.valueOf(currentItem.getDuration())));
        textViewIssueName.setText(currentItem.getIssueName());

        return convertView;
    }

    public WorkingHour remove(final int positionToDelete) {
        final WorkingHour workingHour = mWorkingHours.remove(positionToDelete);
        notifyDataSetChanged();
        return workingHour;
    }

    public void add(final WorkingHour workingHour) {
        mWorkingHours.add(workingHour);
        Collections.sort(mWorkingHours, WorkingHour.WorkingHourDateComparator.getInstance());
        notifyDataSetChanged();
    }

    public void update(final WorkingHour workingHour) {
        for (int i = 0; i < mWorkingHours.size(); i++) {
            if (mWorkingHours.get(i).getId() == workingHour.getId()) {
                mWorkingHours.set(i, workingHour);
            }
        }
        Collections.sort(mWorkingHours, WorkingHour.WorkingHourDateComparator.getInstance());
        notifyDataSetChanged();
    }
}
