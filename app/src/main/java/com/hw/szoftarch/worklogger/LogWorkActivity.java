package com.hw.szoftarch.worklogger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hw.szoftarch.worklogger.entities.Issue;
import com.hw.szoftarch.worklogger.entities.Project;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.WorkingHour;
import com.hw.szoftarch.worklogger.networking.RetrofitClient;
import com.hw.szoftarch.worklogger.networking.WorkLoggerService;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogWorkActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fabManual, fabStopwatch;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    private boolean doubleBackToExitPressedOnce = false;
    private GoogleApiClient mGoogleApiClient;
    @Nullable private User mCurrentUser = null;
    private LogWorkAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_work);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fabManual = findViewById(R.id.fab_manual);
        fabStopwatch = findViewById(R.id.fab_stopwatch);
        fab.setOnClickListener(this);
        fabManual.setOnClickListener(this);
        fabStopwatch.setOnClickListener(this);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView =  navigationView.getHeaderView(0);
        ImageView navPicture = headerView.findViewById(R.id.nav_profile_picture);
        TextView navName = headerView.findViewById(R.id.nav_profile_name);
        TextView navEmail = headerView.findViewById(R.id.nav_profile_email);

        final GoogleSignInAccount account = WorkLoggerApplication.getUser();
        assert account != null;
        navName.setText(account.getDisplayName());
        navEmail.setText(account.getEmail());
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int iconSize = 64;
        if (am != null) {
            iconSize = am.getLauncherLargeIconSize();
        }
        Picasso.with(this)
                .load(account.getPhotoUrl()).transform(new CircleTransform())
                .resize(iconSize, iconSize)
                .placeholder(android.R.drawable.sym_def_app_icon)
                .error(android.R.drawable.sym_def_app_icon) //TODO default image for offline
                .into(navPicture);

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(LogWorkActivity.class.getName(), "onConnectionFailed:" + connectionResult);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final ListView list = findViewById(R.id.list);
        mAdapter = new LogWorkAdapter(this);
        list.setAdapter(mAdapter);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                askForDelete(position);
                return true;
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

    private boolean checkUser() {
        if (mCurrentUser != null) {
            return true;
        }
        Toast.makeText(this, "User data cannot be retrieved. Please try again.", Toast.LENGTH_SHORT).show();
        loadUser();
        return false;
    }

    private void askForDelete(final int positionToDelete) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme);
        builder.setMessage("Are you sure to delete?");

        final String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                completeDeletion(positionToDelete);
                dialog.dismiss();
            }
        });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void completeDeletion(final int positionToDelete) {
        if (!checkOnline()) {
            return;
        }
        final WorkingHour workingHourToDelete = (WorkingHour) mAdapter.getItem(positionToDelete);

        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<String> call = service.removeWorkingHour(workingHourToDelete);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(LogWorkActivity.class.getName(), "removeWorkingHour was successful");
                    mAdapter.remove(positionToDelete);
                } else {
                    Log.d(LogWorkActivity.class.getName(), "removeWorkingHour was unsuccessful, but not failed");
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.d(LogWorkActivity.class.getName(), "removeWorkingHour failed: " + t.getMessage());
            }
        });
    }

    private void loadUser() {
        if (!checkOnline()) {
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<User> call = service.login();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Log.d(LogWorkActivity.class.getName(), "login was successful");
                    final User responseUser = response.body();
                    if (responseUser == null) {
                        Log.d(LogWorkActivity.class.getName(), "login was successful, but null user returned");
                    } else {
                        mCurrentUser = responseUser;
                    }
                } else {
                    Log.d(LogWorkActivity.class.getName(), "login was unsuccessful, but not failed");
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.d(LogWorkActivity.class.getName(), "login failed: " + t.getMessage());
            }
        });
    }

    private void loadWorkingHours() {
        if (!checkOnline()) {
            return;
        }
        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<List<WorkingHour>> call = service.getWorkingHoursByUser();
        call.enqueue(new Callback<List<WorkingHour>>() {
            @Override
            public void onResponse(@NonNull Call<List<WorkingHour>> call, @NonNull Response<List<WorkingHour>> response) {
                if (response.isSuccessful()) {
                    Log.d(LogWorkActivity.class.getName(), "getWorkingHoursByUser was successful");
                    mAdapter.setWorkingHours(response.body());
                } else {
                    Log.d(LogWorkActivity.class.getName(), "getWorkingHoursByUser was unsuccessful, but not failed");
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<WorkingHour>> call, @NonNull Throwable t) {
                Log.d(LogWorkActivity.class.getName(), "getWorkingHoursByUser failed: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        loadUser();
        loadWorkingHours();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.fab:
                animateFAB();
                break;
            case R.id.fab_manual:
                addDebugWorkingHour();
                break;
            case R.id.fab_stopwatch:
                startStopWatch();
                break;
        }
    }

    private void addDebugWorkingHour() {
        if (!checkOnline()) {
            return;
        }
        if (!checkUser()) {
            return;
        }
        final WorkingHour workingHourToAdd = createDummyWorkingHour();

        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<String> call = service.addWorkingHour(workingHourToAdd);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(LogWorkActivity.class.getName(), "addWorkingHour was successful");
                    mAdapter.add(workingHourToAdd);
                } else {
                    Log.d(LogWorkActivity.class.getName(), "addWorkingHour was unsuccessful, but not failed");
                }
            }
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.d(LogWorkActivity.class.getName(), "addWorkingHour failed: " + t.getMessage());
            }
        });
    }

    @NonNull
    private WorkingHour createDummyWorkingHour() {
        final Project project = new Project();
        project.setDescription("descr1");
        project.setName("project1");
        project.setId(1);

        final Issue issue = new Issue();
        issue.setDescription("issue1");
        issue.setDescription("descr1");
        issue.setId(1);
        issue.setProject(project);

        final WorkingHour workingHourToAdd = new WorkingHour();
        workingHourToAdd.setDuration(10L);
        workingHourToAdd.setStarting(Calendar.getInstance().getTime().getTime());
        workingHourToAdd.setIssue(issue);
        workingHourToAdd.setUser(mCurrentUser);
        return workingHourToAdd;
    }

    private void startStopWatch() {
        Toast.makeText(this, "No function attached yet.", Toast.LENGTH_SHORT).show();
    }

    public void animateFAB() {
        if (isFabOpen) {
            fab.startAnimation(rotate_backward);
            fabManual.startAnimation(fab_close);
            fabStopwatch.startAnimation(fab_close);
            fabManual.setClickable(false);
            fabStopwatch.setClickable(false);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            fabManual.startAnimation(fab_open);
            fabStopwatch.startAnimation(fab_open);
            fabManual.setClickable(true);
            fabStopwatch.setClickable(true);
            isFabOpen = true;
        }
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

    private void signOut() {
        try {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.d(LogWorkActivity.class.getName(), "signOut status: " + status.getStatus());
                            finish();
                            final Intent intent = new Intent(LogWorkActivity.this, SignInActivity.class);
                            startActivity(intent);
                        }
                    });
        } catch(Exception e) {
            Log.e(LogWorkActivity.class.getName(), "Cannot sign out: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.log_work, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        final int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        final int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            signOut();
        } else if (id == R.id.nav_list) {

        } else if (id == R.id.nav_stopwatch) {

        } else if (id == R.id.nav_reports) {

        }

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
