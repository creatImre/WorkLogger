package com.hw.szoftarch.worklogger.report;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.WorkLoggerApplication;
import com.hw.szoftarch.worklogger.admin.ConfigActivity;
import com.hw.szoftarch.worklogger.entities.ReportType;
import com.hw.szoftarch.worklogger.stopper.StopperActivity;
import com.hw.szoftarch.worklogger.workinghour.WorkingHourActivity;

import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = ReportActivity.class.getName();
    private boolean doubleBackToExitPressedOnce = false;
    private GoogleApiClient mGoogleApiClient;
    private final List<UserSpinnerItem> mRetrievedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

        initUI();
        updateUI();
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
                selectStartDate();
            }
        });
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

    private void selectStartDate() {
        //TODO
    }

    private void generateReport() {
        //TODO
    }

    private void sendReport() {
        //TODO
    }

    private void updateUI() {
        //TODO
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
