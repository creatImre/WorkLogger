package com.hw.szoftarch.worklogger.workinghour;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.Utils;
import com.hw.szoftarch.worklogger.entities.Issue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class WorkingHourAddFragment extends DialogFragment {

    public static final String TAG = "WorkingHourAdd";
    private List<IssueSpinnerItem> mIssueNames = new ArrayList<>();

    private AppCompatEditText durationHourEditText;
    private AppCompatEditText durationMinuteEditText;
    private AppCompatSpinner issueSpinner;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private AddCallback listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getTargetFragment() != null) {
            try {
                listener = (AddCallback) getTargetFragment();
            } catch (ClassCastException ce) {
                Log.e(TAG, "Target Fragment does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        } else {
            try {
                listener = (AddCallback) activity;
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
        durationHourEditText = root.findViewById(R.id.duration_hours);
        durationMinuteEditText = root.findViewById(R.id.duration_minutes);
        issueSpinner = root.findViewById(R.id.issue);
        final ArrayAdapter<IssueSpinnerItem> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.issue_spinner_item, mIssueNames);
        issueSpinner.setAdapter(arrayAdapter);
        issueSpinner.setSelection(0);

        datePicker = root.findViewById(R.id.date);
        timePicker = root.findViewById(R.id.time);
        timePicker.setIs24HourView(true);

        final Button btnAdd = root.findViewById(R.id.btn_ok);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String durationHourText = durationHourEditText.getText().toString();
                final String durationMinuteText = durationMinuteEditText.getText().toString();
                final int hours = durationHourText.equals("") ? 0 : Integer.parseInt(durationHourText);
                final int minutes = durationMinuteText.equals("") ? 0 : Integer.parseInt(durationMinuteText);
                if (hours == 0 && minutes == 0) {
                    Toast.makeText(getActivity(), "Please enter at least 1 minutes.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final long duration = Utils.getSeconds(hours, minutes, 0);
                final Issue issue = ((IssueSpinnerItem) issueSpinner.getSelectedItem()).getIssue();
                final Date date = Utils.getDateFromPickers(datePicker, timePicker);

                if (listener != null) {
                    listener.onWorkingHourAdded(duration, issue, date);
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

        durationHourEditText.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        return root;
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

    public interface AddCallback {
        void onWorkingHourAdded(long duration, Issue issue, Date date);
    }
}
