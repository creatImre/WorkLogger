package com.hw.szoftarch.worklogger.workinghour;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.Utils;
import com.hw.szoftarch.worklogger.entities.Issue;
import com.hw.szoftarch.worklogger.entities.WorkingHour;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class WorkingHourEditFragment extends DialogFragment {

    public static final String TAG = "WorkingHourEdit";
    private List<IssueSpinnerItem> mIssueNames = new ArrayList<>();
    private WorkingHour mWorkingHour;

    private AppCompatEditText durationEditText;
    private AppCompatSpinner issueSpinner;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private EditCallback listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getTargetFragment() != null) {
            try {
                listener = (EditCallback) getTargetFragment();
            } catch (ClassCastException ce) {
                Log.e(TAG, "Target Fragment does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        } else {
            try {
                listener = (EditCallback) activity;
            } catch (ClassCastException ce) {
                Log.e(TAG, "Parent Activity does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.DialogFragmentTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_working_hour, container, false);
        durationEditText = root.findViewById(R.id.duration);
        issueSpinner = root.findViewById(R.id.issue);
        final ArrayAdapter<IssueSpinnerItem> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.issue_spinner_item, mIssueNames);
        issueSpinner.setAdapter(arrayAdapter);
        issueSpinner.setSelection(0);
        datePicker = root.findViewById(R.id.date);
        timePicker = root.findViewById(R.id.time);
        timePicker.setIs24HourView(true);

        durationEditText.setText(String.valueOf(mWorkingHour.getDuration()));
        issueSpinner.setSelection(arrayAdapter.getPosition(getSelectedIssueSpinnerItem(mWorkingHour)));
        Utils.updatePickers(datePicker, timePicker, mWorkingHour.getStartingDate());

        final Button btnAdd = root.findViewById(R.id.btn_ok);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (durationEditText.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Please enter a durationEditText!", Toast.LENGTH_SHORT).show();
                    return;
                }

                final long duration = Long.parseLong(durationEditText.getText().toString().trim());
                final Issue issue = ((IssueSpinnerItem) issueSpinner.getSelectedItem()).getIssue();
                final Date date = Utils.getDateFromPickers(datePicker, timePicker);

                mWorkingHour.setIssue(issue);
                mWorkingHour.setStarting(date.getTime());
                mWorkingHour.setDuration(duration);

                if (listener != null) {
                    listener.onWorkingHourEdited(mWorkingHour);
                }
                dismiss();
            }
        });

        final Button btnCancel = root.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return root;
    }

    private IssueSpinnerItem getSelectedIssueSpinnerItem(final WorkingHour mWorkingHour) {
        final Issue issue = mWorkingHour.getIssue();
        for (IssueSpinnerItem item : mIssueNames) {
            if (item.getIssue().equals(issue)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null) {
            final Window window = dialog.getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }
        return dialog;
    }

    public void putIssues(final List<Issue> issues) {
        for (final Issue issue: issues) {
            mIssueNames.add(new IssueSpinnerItem(issue));
        }
    }

    public void putWorkingHour(final WorkingHour workingHour) {
        mWorkingHour = workingHour;
    }

    public interface EditCallback {
        void onWorkingHourEdited(WorkingHour editedWorkingHour);
    }
}
