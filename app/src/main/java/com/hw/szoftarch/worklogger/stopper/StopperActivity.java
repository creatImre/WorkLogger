package com.hw.szoftarch.worklogger.stopper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.hw.szoftarch.worklogger.admin.UserManagementActivity;
import com.hw.szoftarch.worklogger.entities.Issue;
import com.hw.szoftarch.worklogger.entities.WorkingHour;
import com.hw.szoftarch.worklogger.networking.RetrofitClient;
import com.hw.szoftarch.worklogger.networking.WorkLoggerService;
import com.hw.szoftarch.worklogger.report.ReportActivity;
import com.hw.szoftarch.worklogger.workinghour.IssueSpinnerItem;
import com.hw.szoftarch.worklogger.workinghour.WorkingHourActivity;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StopperActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String LOG_TAG = WorkingHourActivity.class.getName();
    private static final String PREF_NAME = "stopperPreferences";

    private SharedPreferences mPreferences;
    private boolean doubleBackToExitPressedOnce = false;

    private static final String START_TIME_KEY = "startDate";
    private static final String DURATION_KEY = "duration";
    private static final String STARTED_KEY = "started";
    private static final String STOPPED_KEY = "stopped";

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fabStart, fabStop, fabSend, fabDelete;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    private GoogleApiClient mGoogleApiClient;
    @NonNull
    private List<IssueSpinnerItem> mRetrievedIssues = new ArrayList<>();
    private Issue mSelectedIssue;

    private Handler mHandler = new Handler();
    private Runnable mUpdateClockTask = new Runnable() {
        public void run() {
            updateUI();
            mHandler.postDelayed(mUpdateClockTask, 1000);
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopper);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        fab = findViewById(R.id.fab);
        fabStart = findViewById(R.id.fab_start);
        fabStop = findViewById(R.id.fab_stop);
        fabSend = findViewById(R.id.fab_send);
        fabDelete = findViewById(R.id.fab_delete);

        fab.setOnClickListener(this);
        fabStart.setOnClickListener(this);
        fabStop.setOnClickListener(this);
        fabSend.setOnClickListener(this);
        fabDelete.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

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

        final IssueSpinnerItem emptyItem = new IssueSpinnerItem(null);
        mRetrievedIssues.add(emptyItem);
        final AppCompatSpinner issueSpinner = findViewById(R.id.issue_spinner);
        final ArrayAdapter<IssueSpinnerItem> arrayAdapter = new ArrayAdapter<>(this, R.layout.issue_spinner_item, mRetrievedIssues);
        issueSpinner.setAdapter(arrayAdapter);
        issueSpinner.setSelection(0);
        issueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                mSelectedIssue = mRetrievedIssues.get(position).getIssue();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
            }
        });

        final Button refreshIssuesButton = findViewById(R.id.btn_refresh_issues);
        refreshIssuesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                loadIssues();
            }
        });

        updateUI();
    }

    private void loadIssues() {
        if (!checkOnline()) {
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<List<Issue>> call = service.getIssues();
        call.enqueue(new Callback<List<Issue>>() {
            @Override
            public void onResponse(@NonNull Call<List<Issue>> call, @NonNull Response<List<Issue>> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "getIssues was successful");
                    List<Issue> returnedIssues = response.body();
                    if (returnedIssues == null) {
                        Log.d(LOG_TAG, "getIssues is null, not updating local list");
                    } else {
                        Log.d(LOG_TAG, "getIssues is not null, updating local list");
                        mRetrievedIssues.clear();
                        final IssueSpinnerItem emptyItem = new IssueSpinnerItem(null);
                        mRetrievedIssues.add(emptyItem);
                        for (final Issue issue : returnedIssues) {
                            mRetrievedIssues.add(new IssueSpinnerItem(issue));
                        }
                        Toast.makeText(StopperActivity.this, "Refreshed issues list.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(LOG_TAG, "getIssues was unsuccessful: " + response.message());
                    Toast.makeText(StopperActivity.this, "Cannot load issues data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "getIssues failed: " + t.getMessage());
                Toast.makeText(StopperActivity.this, "Cannot load issues data. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mUpdateClockTask);
        mHandler.postDelayed(mUpdateClockTask, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateClockTask);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            WorkLoggerApplication.signOutFromGoogle(mGoogleApiClient, this);
        } else if (id == R.id.nav_list) {
            final Intent intent = new Intent(this, WorkingHourActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_reports) {
            final Intent intent = new Intent(this, ReportActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_users) {
            final Intent intent = new Intent(this, UserManagementActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_config) {
            final Intent intent = new Intent(this, ConfigActivity.class);
            finish();
            startActivity(intent);
        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void addStopperResult(final WorkingHour stopperWorkingHour) {
        if (!checkOnline()) {
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<WorkingHour> call = service.addWorkingHour(stopperWorkingHour);
        call.enqueue(new Callback<WorkingHour>() {
            @Override
            public void onResponse(@NonNull Call<WorkingHour> call, @NonNull Response<WorkingHour> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "addWorkingHour was successful, clearing preferences...");
                    Toast.makeText(StopperActivity.this, "Time successfully saved.", Toast.LENGTH_SHORT).show();
                    clearPreferences();
                    updateUI(0L);
                } else {
                    Log.d(LOG_TAG, "addWorkingHour was unsuccessful: " + response.message());
                    Toast.makeText(StopperActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkingHour> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "addWorkingHour failed: " + t.getMessage());
                Toast.makeText(StopperActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
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

    private void sendLoggedTime() {
        if (mSelectedIssue == null) {
            Log.d(LOG_TAG, "No selected issue, cannot create proper WorkingHour.");
            Toast.makeText(StopperActivity.this, "Please select an issue before sending.", Toast.LENGTH_SHORT).show();
            return;
        }
        final boolean isStarted = mPreferences.getBoolean(STARTED_KEY, false);
        if (!isStarted) {
            Log.d(LOG_TAG, "Not started, cannot create proper WorkingHour.");
            Toast.makeText(StopperActivity.this, "Please start measuring before sending.", Toast.LENGTH_SHORT).show();
            return;
        }
        final boolean isStopped = mPreferences.getBoolean(STOPPED_KEY, false);
        if (!isStopped) {
            Log.d(LOG_TAG, "Not stopped, cannot create proper WorkingHour.");
            Toast.makeText(StopperActivity.this, "Please stop measuring before sending.", Toast.LENGTH_SHORT).show();
            return;
        }

        final WorkingHour workingHour = getFromPreferences();
        if (workingHour == null) {
            Log.d(LOG_TAG, "No saved time, cannot create proper WorkingHour.");
            Toast.makeText(StopperActivity.this, "No measured time found.", Toast.LENGTH_SHORT).show();
            return;
        }
        workingHour.setIssue(mSelectedIssue);
        addStopperResult(workingHour);
    }

    @Nullable
    private WorkingHour getFromPreferences() {
        final long duration = mPreferences.getLong(DURATION_KEY, 0L);
        if (duration == 0L) {
            return null;
        }
        final long startDateTime = mPreferences.getLong(START_TIME_KEY, 0L);
        if (startDateTime == 0L) {
            return null;
        }

        final WorkingHour workingHour = new WorkingHour();
        workingHour.setDuration(duration);
        workingHour.setStarting(startDateTime);
        return workingHour;
    }

    private void clearPreferences() {
        mPreferences.edit().clear().apply();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab_start:
                startStopper();
                break;
            case R.id.fab_stop:
                stopStopper();
                break;
            case R.id.fab_send:
                sendLoggedTime();
                break;
            case R.id.fab_delete:
                deleteStopper();
                break;
        }
    }

    private void startStopper() {
        final boolean isStarted = mPreferences.getBoolean(STARTED_KEY, false);
        if (isStarted) {
            Log.d(LOG_TAG, "Existing saved time, not starting new measurement.");
            Toast.makeText(StopperActivity.this, "Measured time found. Please delete or send before starting another measurement.", Toast.LENGTH_SHORT).show();
            return;
        }
        clearPreferences();
        saveStartDate(new DateTime());
        updateUI(0L);
        Toast.makeText(StopperActivity.this, "Started measurement.", Toast.LENGTH_SHORT).show();
    }

    private void saveStartDate(final DateTime dateTime) {
        mPreferences.edit().putLong(START_TIME_KEY, dateTime.getMillis()).apply();
        mPreferences.edit().putBoolean(STARTED_KEY, true).apply();
    }

    private void stopStopper() {
        final boolean isStarted = mPreferences.getBoolean(STARTED_KEY, false);
        if (!isStarted) {
            Log.d(LOG_TAG, "No saved time, not stopping measurement.");
            Toast.makeText(StopperActivity.this, "Measurement not started yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        final long startTime = mPreferences.getLong(START_TIME_KEY, 0L);
        final long elapsedTime = Utils.getElapsedTimeUntilNow(startTime);
        saveDuration(elapsedTime);
        updateUI(elapsedTime);
        Toast.makeText(StopperActivity.this, "Stopped measurement.", Toast.LENGTH_SHORT).show();
    }

    private void saveDuration(final long elapsedTime) {
        mPreferences.edit().putLong(DURATION_KEY, elapsedTime).apply();
        mPreferences.edit().putBoolean(STOPPED_KEY, true).apply();
    }

    private void deleteStopper() {
        final boolean isStarted = mPreferences.getBoolean(STARTED_KEY, false);
        if (isStarted) {
            askForDelete();
        } else {
            Log.d(LOG_TAG, "Not started yet, cannot delete anything.");
            Toast.makeText(StopperActivity.this, "No measurement to delete.", Toast.LENGTH_SHORT).show();
        }
    }

    private void askForDelete() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to delete?");

        final String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearPreferences();
                updateUI(0L);
                dialog.dismiss();
            }
        });

        final String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void updateUI() {
        final long durationTime = mPreferences.getLong(DURATION_KEY, 0L);
        if (durationTime != 0L) {
            updateUI(durationTime);
            return;
        }

        final long startTime = mPreferences.getLong(START_TIME_KEY, 0L);
        if (startTime == 0L) {
            updateUI(0L);
            return;
        }
        updateUI(Utils.getElapsedTimeUntilNow(startTime));
    }

    private void updateUI(final long elapsedTime) {
        final TextView elapsedTimeTextView = findViewById(R.id.elapsed_time);
        elapsedTimeTextView.setText(Utils.getShowedElapsedTime(elapsedTime));

        final TextView startedTimeTextView = findViewById(R.id.is_started);
        final boolean isStopped = mPreferences.getBoolean(STOPPED_KEY, false);
        if (isStopped) {
            startedTimeTextView.setText(getResources().getText(R.string.measuring_stopped));
            return;
        }
        final boolean isStarted = mPreferences.getBoolean(STARTED_KEY, false);
        if (isStarted) {
            startedTimeTextView.setText(getResources().getText(R.string.measuring_started));
            return;
        }
        startedTimeTextView.setText(getResources().getText(R.string.measuring_not_exists));
    }

    public void animateFAB() {
        if (isFabOpen) {
            fab.startAnimation(rotate_backward);
            fabStart.startAnimation(fab_close);
            fabStop.startAnimation(fab_close);
            fabSend.startAnimation(fab_close);
            fabDelete.startAnimation(fab_close);

            fabStart.setClickable(false);
            fabStop.setClickable(false);
            fabSend.setClickable(false);
            fabDelete.setClickable(false);

            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fabStart.startAnimation(fab_open);
            fabStop.startAnimation(fab_open);
            fabSend.startAnimation(fab_open);
            fabDelete.startAnimation(fab_open);

            fabStart.setClickable(true);
            fabStop.setClickable(true);
            fabSend.setClickable(true);
            fabDelete.setClickable(true);

            isFabOpen = true;
        }
    }

}
