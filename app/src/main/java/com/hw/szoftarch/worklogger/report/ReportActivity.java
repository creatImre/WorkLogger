package com.hw.szoftarch.worklogger.report;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.Utils;
import com.hw.szoftarch.worklogger.WorkLoggerApplication;
import com.hw.szoftarch.worklogger.admin.ConfigActivity;
import com.hw.szoftarch.worklogger.entities.Report;
import com.hw.szoftarch.worklogger.entities.ReportType;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.WorkingHour;
import com.hw.szoftarch.worklogger.networking.RetrofitClient;
import com.hw.szoftarch.worklogger.networking.WorkLoggerService;
import com.hw.szoftarch.worklogger.stopper.StopperActivity;
import com.hw.szoftarch.worklogger.workinghour.WorkingHourActivity;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = ReportActivity.class.getName();
    private boolean doubleBackToExitPressedOnce = false;
    private GoogleApiClient mGoogleApiClient;
    private final List<UserSpinnerItem> mRetrievedUsers = new ArrayList<>();
    private DateTime mSelectedStartDate = new DateTime();
    private Report mGeneratedReport;
    private boolean mCurrentReportSaved = false;
    private long mWorkedHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        WorkLoggerApplication.setGoogleAccountDataToNavigationDrawer(this);
        WorkLoggerApplication.setAdminMenuVisibleIfAdmin(navigationView);
        WorkLoggerApplication.setReportMenuVisibleIfProjectLeaderOrAdmin(navigationView);

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final UserSpinnerItem allUserItem = new UserSpinnerItem(null);
        mRetrievedUsers.add(allUserItem);
        initUI();
        getAllUsers();
    }

    private void initUI() {
        final AppCompatSpinner usersSpinner = findViewById(R.id.users);
        final AppCompatSpinner typeSpinner = findViewById(R.id.type);
        final Button startDateButton = findViewById(R.id.btn_start_date);
        final Button generateButton = findViewById(R.id.btn_generate);
        final Button saveButton = findViewById(R.id.btn_save);

        final ArrayAdapter<UserSpinnerItem> usersAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, mRetrievedUsers);
        usersSpinner.setAdapter(usersAdapter);

        final ArrayAdapter<ReportType> typesAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, ReportType.values());
        typeSpinner.setAdapter(typesAdapter);

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                selectStartDate(startDateButton);
            }
        });
        startDateButton.setText(Utils.getDateText(mSelectedStartDate));
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                generateReport();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                sendReport();
            }
        });
        final TextView subject = findViewById(R.id.subject);
        final TextView startDate = findViewById(R.id.startdate);
        final TextView interval = findViewById(R.id.interval);
        final TextView workedTime = findViewById(R.id.worked_time);

        subject.setText("");
        startDate.setText("");
        interval.setText("");
        workedTime.setText("");
    }

    private void selectStartDate(final Button button) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                mSelectedStartDate = new DateTime(year, month, day,0,0);
                button.setText(Utils.getDateText(mSelectedStartDate));
            }
        }
        ,year, month, day).show();
    }

    private void generateReport() {
        final User owner = WorkLoggerApplication.getCurrentUser();
        if (owner == null) {
            Log.d(LOG_TAG, "User is null. Cannot use is to create report.");
            Toast.makeText(this, "Cannot generate report. User data cannot be retrieved. Please try again.", Toast.LENGTH_SHORT).show();

        }
        final Report report = new Report();
        report.setOwner(owner);
        report.setStartDate(mSelectedStartDate.getMillis());

        final AppCompatSpinner usersSpinner = findViewById(R.id.users);
        final AppCompatSpinner typeSpinner = findViewById(R.id.type);

        final User selectedUser = ((UserSpinnerItem) usersSpinner.getSelectedItem()).getUser();
        if (selectedUser == null) {
            report.setGoogleId(Report.ALL);
        }
        final ReportType type = (ReportType) typeSpinner.getSelectedItem();
        report.setReportType(type.toString());
        mGeneratedReport = report;
        mCurrentReportSaved = false;
    }

    private void addStopperResult(final WorkingHour stopperWorkingHour) {

    }

    private void updateReport(){

    }

    private void deleteReport() {

    }

    private void sendReport() {
        if (!checkOnline()) {
            return;
        }
        if (mGeneratedReport == null) {
            Toast.makeText(this, "Generate a report before saving.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCurrentReportSaved) {
            Toast.makeText(this, "Report is already saved.", Toast.LENGTH_SHORT).show();
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<Report> call = service.addReport(mGeneratedReport);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull Response<Report> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "addReport was successful");
                    Toast.makeText(ReportActivity.this, "Report successfully saved.", Toast.LENGTH_SHORT).show();
                    mGeneratedReport = response.body();
                    mCurrentReportSaved = true;
                    getWorkedHoursForReport();
                } else {
                    Log.d(LOG_TAG, "addReport was unsuccessful: " + response.message());
                    Toast.makeText(ReportActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "addReport failed: " + t.getMessage());
                Toast.makeText(ReportActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getWorkedHoursForReport() {
        if (!checkOnline()) {
            return;
        }
        if (!mCurrentReportSaved) {
            Toast.makeText(this, "Save report before getting worked hours.", Toast.LENGTH_SHORT).show();
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<Long> call = service.getWorkedHoursForReport(mGeneratedReport.getId());
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "getWorkedHoursForReport was successful");
                    Long responseValue = response.body();
                    if (responseValue == null) {
                        Log.d(LOG_TAG, "returned worked hours is null.");
                    } else {
                        mWorkedHours = responseValue;
                        updateUI();
                    }
                } else {
                    Log.d(LOG_TAG, "getWorkedHoursForReport was unsuccessful: " + response.message());
                    Toast.makeText(ReportActivity.this, "Cannot get from server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "getWorkedHoursForReport failed: " + t.getMessage());
                Toast.makeText(ReportActivity.this, "Cannot get from server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllUsers() {
        if (!checkOnline()) {
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<List<User>> call = service.getUsers();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "getUsers was successful");
                    final List<User> users = response.body();
                    if (users == null) {
                        Log.d(LOG_TAG, "returned users is null.");
                    } else {
                        mRetrievedUsers.clear();
                        final UserSpinnerItem allUserItem = new UserSpinnerItem(null);
                        mRetrievedUsers.add(allUserItem);
                        for (User user : users) {
                            mRetrievedUsers.add(new UserSpinnerItem(user));
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "getUsers was unsuccessful: " + response.message());
                    Toast.makeText(ReportActivity.this, "Cannot get from server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "getUsers failed: " + t.getMessage());
                Toast.makeText(ReportActivity.this, "Cannot get from server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkOnline() {
        final boolean online = WorkLoggerApplication.appIsOnline();
        if (online) {
            return true;
        }
        Toast.makeText(this, "You're offline. Please go online to complete operation.", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void updateUI() {
        final TextView subject = findViewById(R.id.subject);
        final TextView startDate = findViewById(R.id.startdate);
        final TextView interval = findViewById(R.id.interval);
        final TextView workedTime = findViewById(R.id.worked_time);

        String name = "Everybody";
        for (UserSpinnerItem item: mRetrievedUsers) {
            User itemUser = item.getUser();
            if (itemUser != null && itemUser.getGoogleId().equals(mGeneratedReport.getGoogleId())) {
                name = itemUser.getName();
            }
        }
        subject.setText(name);
        final DateTime dateTime = new DateTime(mGeneratedReport.getStartDate());
        startDate.setText(Utils.getDateText(dateTime));
        final ReportType type = ReportType.valueOf(mGeneratedReport.getReportType());
        final String shownInterval = type.getShownName();
        interval.setText(shownInterval);
        workedTime.setText(String.valueOf(mWorkedHours));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
            } else {
                doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            WorkLoggerApplication.signOutFromGoogle(mGoogleApiClient, this);
        } else if (id == R.id.nav_list) {
            final Intent intent = new Intent(this, WorkingHourActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_stopwatch) {
            final Intent intent = new Intent(this, StopperActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_users) {

        } else if (id == R.id.nav_config) {
            final Intent intent = new Intent(this, ConfigActivity.class);
            finish();
            startActivity(intent);
        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
