package com.hw.szoftarch.worklogger.report;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;

import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.Utils;
import com.hw.szoftarch.worklogger.entities.Report;
import com.hw.szoftarch.worklogger.entities.ReportType;
import com.hw.szoftarch.worklogger.entities.User;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;


public class ReportAddFragment extends DialogFragment {

    public static final String TAG = "ReportAdd";
    private List<UserSpinnerItem> mUserSpinnerItems = new ArrayList<>();

    private AppCompatSpinner mUsersSpinner;
    private AppCompatSpinner mReportTypeSpinner;
    private DatePicker mDatePicker;

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
        final View root = inflater.inflate(R.layout.fragment_report, container, false);

        mUsersSpinner = root.findViewById(R.id.users);
        final ArrayAdapter<UserSpinnerItem> usersAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, mUserSpinnerItems);
        mUsersSpinner.setAdapter(usersAdapter);
        mUsersSpinner.setSelection(0);

        mReportTypeSpinner = root.findViewById(R.id.report_type);
        final ArrayAdapter<ReportType> reportTypeAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, ReportType.values());
        mReportTypeSpinner.setAdapter(reportTypeAdapter);
        mReportTypeSpinner.setSelection(0);

        mDatePicker = root.findViewById(R.id.date);

        final Button btnAdd = root.findViewById(R.id.btn_ok);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final User user = ((UserSpinnerItem) mUsersSpinner.getSelectedItem()).getUser();
                final ReportType reportType = (ReportType) mReportTypeSpinner.getSelectedItem();
                final String selectedGoogleId;
                if (user == null) {
                    selectedGoogleId = Report.ALL;
                } else {
                    selectedGoogleId = user.getGoogleId();
                }
                final DateTime date = Utils.getDateFromDatePicker(mDatePicker);

                final Report report = new Report();
                report.setStartDate(date.getMillis());
                report.setGoogleId(selectedGoogleId);
                report.setReportType(reportType.toString());

                if (listener != null) {
                    listener.onReportAdded(report);
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

    public void putUsers(final List<User> users) {
        mUserSpinnerItems.add(new UserSpinnerItem((null)));
        for (final User user: users) {
            mUserSpinnerItems.add(new UserSpinnerItem(user));
        }
    }

    public interface AddCallback {
        void onReportAdded(Report report);
    }
}
