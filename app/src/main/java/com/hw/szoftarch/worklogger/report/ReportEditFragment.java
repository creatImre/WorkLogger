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


public class ReportEditFragment extends DialogFragment {

    public static final String TAG = "ReportEdit";
    private List<UserSpinnerItem> mUserSpinnerItems = new ArrayList<>();
    private CalculatedReport mReport;

    private AppCompatSpinner mUsersSpinner;
    private AppCompatSpinner mReportTypeSpinner;
    private DatePicker mDatePicker;

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

        mUsersSpinner.setSelection(getSelectedUserSpinnerItemIndex(mReport));
        mReportTypeSpinner.setSelection(reportTypeAdapter.getPosition(ReportType.valueOf(mReport.getReport().getReportType())));
        Utils.updateDatePicker(mDatePicker, mReport.getReport().getStartDate());

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

                mReport.getReport().setStartDate(date.getMillis());
                mReport.getReport().setGoogleId(selectedGoogleId);
                mReport.getReport().setReportType(reportType.toString());

                if (listener != null) {
                    listener.onReportEdited(mReport);
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

    private int getSelectedUserSpinnerItemIndex(final CalculatedReport report) {
        final String googleId = report.getReport().getGoogleId();
        if (googleId.equals(Report.ALL)) {
            return 0;
        }
        for (int i = 1; i < mUserSpinnerItems.size(); i++) {
            final User user = mUserSpinnerItems.get(i).getUser();
            if (user == null) {
                continue;
            }
            if (user.getGoogleId().equals(googleId)) {
                return i;
            }
        }
        return -1;
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

    public void putReport(final CalculatedReport report) {
        mReport = report;
    }

    public interface EditCallback {
        void onReportEdited(CalculatedReport editedReport);
    }
}
