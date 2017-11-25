package com.hw.szoftarch.worklogger.admin;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.WorkLoggerApplication;
import com.hw.szoftarch.worklogger.report.ReportActivity;
import com.hw.szoftarch.worklogger.stopper.StopperActivity;
import com.hw.szoftarch.worklogger.workinghour.WorkingHourActivity;

import java.util.List;

public class ConfigActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = ConfigActivity.class.getName();
    private boolean doubleBackToExitPressedOnce = false;

    private GoogleApiClient mGoogleApiClient;

    private String mSecurity;
    private String mIpAddress;
    private String mPort;
    private String mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
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
        mSecurity = WorkLoggerApplication.getSecurity();
        mIpAddress = WorkLoggerApplication.getIpAddress();
        mPort = WorkLoggerApplication.getPort();
        mService = WorkLoggerApplication.getService();

        updateUI();
        setUpdateListeners();
        setButtons();
    }

    private void updateUI() {
        final AppCompatSpinner securitySpinner = findViewById(R.id.http_https);
        final EditText ipText = findViewById(R.id.ip_address);
        final EditText portText = findViewById(R.id.port);
        final EditText serviceText = findViewById(R.id.service);

        ipText.setText(WorkLoggerApplication.getIpAddress());
        portText.setText(WorkLoggerApplication.getPort());
        serviceText.setText(WorkLoggerApplication.getService());

        final List<String> securityLevels = WorkLoggerApplication.getSecurityLevels();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, securityLevels);
        securitySpinner.setAdapter(arrayAdapter);

        final String security = WorkLoggerApplication.getSecurity();
        securitySpinner.setSelection(securityLevels.indexOf(security));
        updateFullAddressText();
    }

    private void updateFullAddressText() {
        final TextView fullUrl = findViewById(R.id.full_url);
        fullUrl.setText(WorkLoggerApplication.getFullServiceUrl());
    }

    private void setUpdateListeners() {
        final AppCompatSpinner securitySpinner = findViewById(R.id.http_https);
        final EditText ipText = findViewById(R.id.ip_address);
        final EditText portText = findViewById(R.id.port);
        final EditText serviceText = findViewById(R.id.service);

        securitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                mSecurity = (String) securitySpinner.getItemAtPosition(position);
                updateFullAddressTextLocally();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> adapterView) {
            }
        });

        ipText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mIpAddress = s.toString();
                updateFullAddressTextLocally();
            }
        });

        portText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPort = s.toString();
                updateFullAddressTextLocally();
            }
        });

        serviceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mService = s.toString();
                updateFullAddressTextLocally();
            }
        });
    }

    private void updateFullAddressTextLocally() {
        final String fullAddress = mSecurity + "://" + mIpAddress + ":" + mPort + "/" + mService + "/";
        final TextView fullUrl = findViewById(R.id.full_url);
        fullUrl.setText(fullAddress);
    }

    private void setButtons() {
        final Button discardButton = findViewById(R.id.btn_discard);
        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Toast.makeText(ConfigActivity.this, "Changes discarded.", Toast.LENGTH_SHORT).show();
                updateUI();
            }
        });
        final Button saveButton = findViewById(R.id.btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final AppCompatSpinner securitySpinner = findViewById(R.id.http_https);
                final EditText ipText = findViewById(R.id.ip_address);
                final EditText portText = findViewById(R.id.port);
                final EditText serviceText = findViewById(R.id.service);

                final String security = (String) securitySpinner.getSelectedItem();
                final String ip = ipText.getText().toString().trim();
                final String port = portText.getText().toString().trim();
                final String service = serviceText.getText().toString().trim();
                if (ip.equals("")) {
                    Toast.makeText(ConfigActivity.this, "IP address is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (port.equals("")) {
                    Toast.makeText(ConfigActivity.this, "Port is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (service.equals("")) {
                    Toast.makeText(ConfigActivity.this, "Service is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                WorkLoggerApplication.saveConfig(security, ip, port, service);
                Toast.makeText(ConfigActivity.this, "Config saved.", Toast.LENGTH_SHORT).show();

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

        } else if (id == R.id.nav_reports) {
            final Intent intent = new Intent(this, ReportActivity.class);
            finish();
            startActivity(intent);
        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
