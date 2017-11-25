package com.hw.szoftarch.worklogger.workinghour;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.WorkLoggerApplication;
import com.hw.szoftarch.worklogger.admin.ConfigActivity;
import com.hw.szoftarch.worklogger.entities.Issue;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.WorkingHour;
import com.hw.szoftarch.worklogger.networking.RetrofitClient;
import com.hw.szoftarch.worklogger.networking.WorkLoggerService;
import com.hw.szoftarch.worklogger.recycler_tools.ClickListener;
import com.hw.szoftarch.worklogger.recycler_tools.DeleteCallback;
import com.hw.szoftarch.worklogger.recycler_tools.RecyclerTouchListener;
import com.hw.szoftarch.worklogger.recycler_tools.SwipeTouchHelperCallback;
import com.hw.szoftarch.worklogger.stopper.StopperActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkingHourActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WorkingHourAddFragment.AddCallback, WorkingHourEditFragment.EditCallback, DeleteCallback {

    private static final String LOG_TAG = WorkingHourActivity.class.getName();

    private boolean doubleBackToExitPressedOnce = false;
    private GoogleApiClient mGoogleApiClient;
    private WorkingHourAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    @NonNull
    private List<Issue> mRetrievedIssues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_work);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final WorkingHourAddFragment addFragment = new WorkingHourAddFragment();
                addFragment.putIssues(mRetrievedIssues);
                addFragment.show(getFragmentManager(), WorkingHourAddFragment.TAG);
            }
        });

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        WorkLoggerApplication.setAdminMenuVisibleIfAdmin(navigationView);
        WorkLoggerApplication.setGoogleAccountDataToNavigationDrawer(this);

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

        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.d(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                        updateData();
                    }
                }
        );
        initList();
        loadUser();
        loadIssues();
        loadWorkingHours();
    }

    private void initList() {
        mAdapter = new WorkingHourAdapter(this);
        final RecyclerView foodsRecyclerView = findViewById(android.R.id.list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        foodsRecyclerView.setLayoutManager(mLayoutManager);
        foodsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        foodsRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        foodsRecyclerView.setAdapter(mAdapter);
        foodsRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), foodsRecyclerView,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!checkOnline()) {
                            return;
                        }
                        final WorkingHourEditFragment editFragment = new WorkingHourEditFragment();
                        editFragment.putIssues(mRetrievedIssues);
                        editFragment.putWorkingHour(mAdapter.getItem(position));
                        editFragment.show(getFragmentManager(), WorkingHourEditFragment.TAG);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                    }
                }));

        ItemTouchHelper.Callback callback = new SwipeTouchHelperCallback(mAdapter);
        ItemTouchHelper mTouchHelper = new ItemTouchHelper(callback);
        mTouchHelper.attachToRecyclerView(foodsRecyclerView);
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.log_work, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            Log.d(LOG_TAG, "Refresh menu item selected");
            mSwipeRefreshLayout.setRefreshing(true);
            updateData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            WorkLoggerApplication.signOutFromGoogle(mGoogleApiClient, this);
        } else if (id == R.id.nav_stopwatch) {
            final Intent intent = new Intent(this, StopperActivity.class);
            finish();
            startActivity(intent);
        } else if (id == R.id.nav_reports) {

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

    private void updateData() {
        if (!checkOnline()) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }
        loadUser();
        loadIssues();
        loadWorkingHours();
    }

    private boolean checkOnline() {
        final boolean online = WorkLoggerApplication.appIsOnline();
        if (online) {
            return true;
        }
        Toast.makeText(this, "You're offline. Please go online to complete operation.", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void askForDelete(final int positionToDelete) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to delete?");

        final String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAdapter.setNotNeedToNotify();
                completeDeletion(positionToDelete);
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
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialogInterface) {
                mAdapter.notifyItemChangedIfNeeded(positionToDelete);
            }
        });
        builder.show();
    }

    private void completeDeletion(final int positionToDelete) {
        if (!checkOnline()) {
            mAdapter.notifyItemChanged(positionToDelete);
            return;
        }

        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<String> call = service.removeWorkingHour(mAdapter.getItemId(positionToDelete));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "removeWorkingHour was successful");
                    mAdapter.remove(positionToDelete);
                } else {
                    mAdapter.notifyItemChanged(positionToDelete);
                    Log.d(LOG_TAG, "removeWorkingHour was unsuccessful: " + response.message());
                    Toast.makeText(WorkingHourActivity.this, "Cannot delete on server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                mAdapter.notifyItemChanged(positionToDelete);
                Log.d(LOG_TAG, "removeWorkingHour failed: " + t.getMessage());
                Toast.makeText(WorkingHourActivity.this, "Cannot delete on server. Please try again.", Toast.LENGTH_SHORT).show();
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
                    Log.d(LOG_TAG, "login was successful");
                    final User responseUser = response.body();
                    if (responseUser == null) {
                        Log.d(LOG_TAG, "login was successful, but null user returned");
                    } else {
                        WorkLoggerApplication.setCurrentUser(responseUser);
                        final NavigationView navigationView = findViewById(R.id.nav_view);
                        WorkLoggerApplication.setAdminMenuVisibleIfAdmin(navigationView);
                    }
                } else {
                    Log.d(LOG_TAG, "login was unsuccessful: " + response.message());
                    Toast.makeText(WorkingHourActivity.this, "Cannot get current user data from server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "login failed: " + t.getMessage());
                Toast.makeText(WorkingHourActivity.this, "Cannot get current user data from server. Please try again.", Toast.LENGTH_SHORT).show();
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
                    Log.d(LOG_TAG, "getWorkingHoursByUser was successful");
                    mAdapter.setWorkingHours(response.body());
                } else {
                    Log.d(LOG_TAG, "getWorkingHoursByUser was unsuccessful: " + response.message());
                    Toast.makeText(WorkingHourActivity.this, "Cannot update data. Please try again.", Toast.LENGTH_SHORT).show();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<WorkingHour>> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "getWorkingHoursByUser failed: " + t.getMessage());
                Toast.makeText(WorkingHourActivity.this, "Cannot update data. Please try again.", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
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
                        mRetrievedIssues = returnedIssues;
                    }
                } else {
                    Log.d(LOG_TAG, "getIssues was unsuccessful: " + response.message());
                    Toast.makeText(WorkingHourActivity.this, "Cannot load issues data. Please try again.", Toast.LENGTH_SHORT).show();
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "getIssues failed: " + t.getMessage());
                Toast.makeText(WorkingHourActivity.this, "Cannot load issues data. Please try again.", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void addWorkingHour(final WorkingHour workingHourToAdd) {
        if (!checkOnline()) {
            return;
        }
        Log.d(LOG_TAG, "sending to server: " + workingHourToAdd);

        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<WorkingHour> call = service.addWorkingHour(workingHourToAdd);
        call.enqueue(new Callback<WorkingHour>() {
            @Override
            public void onResponse(@NonNull Call<WorkingHour> call, @NonNull Response<WorkingHour> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "addWorkingHour was successful");
                    Log.d(LOG_TAG, "returned WorkingHour: " + response.body());
                    mAdapter.add(response.body());
                } else {
                    Log.d(LOG_TAG, "addWorkingHour was unsuccessful: " + response.message());
                    Toast.makeText(WorkingHourActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WorkingHour> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "addWorkingHour failed: " + t.getMessage());
                Toast.makeText(WorkingHourActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWorkingHour(final WorkingHour workingHour) {
        if (!checkOnline()) {
            return;
        }
        Log.d(LOG_TAG, "sending to server: " + workingHour);

        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<String> call = service.updateWorkingHour(workingHour);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "updateWorkingHour was successful");
                    mAdapter.update(workingHour);
                } else {
                    Log.d(LOG_TAG, "updateWorkingHour was unsuccessful: " + response.message());
                    Toast.makeText(WorkingHourActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "updateWorkingHour failed: " + t.getMessage());
                Toast.makeText(WorkingHourActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWorkingHourAdded(final long duration, final Issue issue, final Date date) {
        final WorkingHour workingHour = new WorkingHour();
        workingHour.setDuration(duration);
        workingHour.setIssue(issue);
        workingHour.setStarting(date.getTime());
        addWorkingHour(workingHour);
    }

    @Override
    public void onWorkingHourEdited(final WorkingHour editedWorkingHour) {
        updateWorkingHour(editedWorkingHour);
    }

    @Override
    public void deleteItem(final int positionToDelete) {
        askForDelete(positionToDelete);
    }
}
