package com.hw.szoftarch.worklogger.report;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.recycler_tools.DeleteCallback;
import com.hw.szoftarch.worklogger.recycler_tools.ItemSwipeHelperAdapter;

import java.util.ArrayList;
import java.util.List;

class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ItemViewHolder>
        implements ItemSwipeHelperAdapter {
    private List<CalculatedReport> mCalculatedReports = new ArrayList<>();
    private DeleteCallback mDeleteCallback;
    private boolean needToNotifyItem = true;

    ReportAdapter(final DeleteCallback deleteCallback) {
        this.mDeleteCallback = deleteCallback;
    }

    CalculatedReport getItem(final int position) {
        return mCalculatedReports.get(position);
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

    void clear() {
        mCalculatedReports = new ArrayList<>();
        notifyDataSetChanged();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView subject, startDate, interval, workedTime;

        ItemViewHolder(View view) {
            super(view);
            subject = view.findViewById(R.id.subject);
            startDate = view.findViewById(R.id.start_date);
            interval = view.findViewById(R.id.interval);
            workedTime = view.findViewById(R.id.worked_time);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_rowitem, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final CalculatedReport report = mCalculatedReports.get(position);

        holder.subject.setText(report.getSubjectName());
        holder.startDate.setText(report.getStartDateText());
        holder.interval.setText(report.getIntervalText());
        holder.workedTime.setText(report.getWorkedHoursText());
    }

    @Override
    public int getItemCount() {
        return mCalculatedReports.size();
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
        mCalculatedReports.remove(positionToDelete);
        notifyItemRemoved(positionToDelete);
    }

    void add(final CalculatedReport calculatedReport) {
        mCalculatedReports.add(calculatedReport);
        notifyItemInserted(mCalculatedReports.size() - 1);
    }

    void update(final CalculatedReport calculatedReport) {
        for (int i = 0; i < mCalculatedReports.size(); i++) {
            if (mCalculatedReports.get(i).getId() == calculatedReport.getId()) {
                mCalculatedReports.set(i, calculatedReport);
                notifyItemChanged(i);
            }
        }
    }

    void setCalculatedReport(final List<CalculatedReport> calculatedReports) {
        mCalculatedReports = calculatedReports;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return mCalculatedReports.get(position).getId();
    }
}
