package com.hw.szoftarch.worklogger.admin;

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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hw.szoftarch.worklogger.R;
import com.hw.szoftarch.worklogger.WorkLoggerApplication;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.networking.RetrofitClient;
import com.hw.szoftarch.worklogger.networking.WorkLoggerService;
import com.hw.szoftarch.worklogger.recycler_tools.ClickListener;
import com.hw.szoftarch.worklogger.recycler_tools.DeleteCallback;
import com.hw.szoftarch.worklogger.recycler_tools.RecyclerTouchListener;
import com.hw.szoftarch.worklogger.recycler_tools.SwipeTouchHelperCallback;
import com.hw.szoftarch.worklogger.report.ReportActivity;
import com.hw.szoftarch.worklogger.stopper.StopperActivity;
import com.hw.szoftarch.worklogger.workinghour.WorkingHourActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserManagementActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, UserManagementEditFragment.EditCallback, DeleteCallback {

    private static final String LOG_TAG = UserManagementActivity.class.getName();
    private boolean doubleBackToExitPressedOnce = false;
    private GoogleApiClient mGoogleApiClient;
    private Animation mRotateAnimation;
    private UserManagementAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRotateAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_slowly_360);
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.startAnimation(mRotateAnimation);
                getAllUsers();
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


        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.d(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
                        getAllUsers();
                    }
                }
        );
        initList();
        getAllUsers();
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

    private void initList() {
        mAdapter = new UserManagementAdapter(this);
        final RecyclerView recyclerView = findViewById(android.R.id.list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (!checkOnline()) {
                            return;
                        }
                        final UserManagementEditFragment editFragment = new UserManagementEditFragment();
                        editFragment.putUser(mAdapter.getItem(position));
                        editFragment.show(getFragmentManager(), UserManagementEditFragment.TAG);
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                    }
                }));

        ItemTouchHelper.Callback callback = new SwipeTouchHelperCallback(mAdapter);
        ItemTouchHelper mTouchHelper = new ItemTouchHelper(callback);
        mTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void getAllUsers() {
        if (!checkOnline()) {
            mSwipeRefreshLayout.setRefreshing(false);
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
                        mAdapter.setUsers(users);
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    Log.d(LOG_TAG, "getUsers was unsuccessful: " + response.message());
                    Toast.makeText(UserManagementActivity.this, "Cannot get from server. Please try again.", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "getUsers failed: " + t.getMessage());
                Toast.makeText(UserManagementActivity.this, "Cannot get from server. Please try again.", Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
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
        final Call<String> call = service.removeUser(mAdapter.getItem(positionToDelete).getGoogleId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "removeUser was successful");
                    mAdapter.remove(positionToDelete);
                } else {
                    mAdapter.notifyItemChanged(positionToDelete);
                    Log.d(LOG_TAG, "removeUser was unsuccessful: " + response.message());
                    Toast.makeText(UserManagementActivity.this, "Cannot delete on server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                mAdapter.notifyItemChanged(positionToDelete);
                Log.d(LOG_TAG, "removeUser failed: " + t.getMessage());
                Toast.makeText(UserManagementActivity.this, "Cannot delete on server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUser(final User user) {
        if (!checkOnline()) {
            return;
        }

        final WorkLoggerService service = new RetrofitClient().createService();
        final Call<String> call = service.updateUser(user);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(LOG_TAG, "updateUser was successful");
                    mAdapter.update(user);
                } else {
                    Log.d(LOG_TAG, "updateUser was unsuccessful: " + response.message());
                    Toast.makeText(UserManagementActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.d(LOG_TAG, "updateUser failed: " + t.getMessage());
                Toast.makeText(UserManagementActivity.this, "Cannot send to server. Please try again.", Toast.LENGTH_SHORT).show();
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
        } else if (id == R.id.nav_reports) {
            final Intent intent = new Intent(this, ReportActivity.class);
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

    @Override
    public void onUserEdited(User editedUser) {
        updateUser(editedUser);
    }

    @Override
    public void deleteItem(int positionToDelete) {
        askForDelete(positionToDelete);
    }

}
