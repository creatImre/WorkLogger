package com.hw.szoftarch.worklogger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.UserLevel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


public class WorkLoggerApplication extends Application {

    private static final String TAG = "WorkLoggerApplication";
    private static final String PROFILE_PICTURE_NAME = "profilePicture";
    private static final String PREF_NAME = "configurationPreferences";

    public static String SECURITY_KEY = "security";
    public static String PORT_KEY = "port";
    public static String IP_ADDRESS_KEY = "ipAddress";
    public static String SERVICE_NAME_KEY = "serviceName";
    private static SharedPreferences mPreferences;

    private static WorkLoggerApplication mInstance;
    private @Nullable User mCurrentUser = null;

    @Nullable
    public static GoogleSignInAccount getGoogleSignInAccount() {
        return GoogleSignIn.getLastSignedInAccount(getContext());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    public static Context getContext() {
        return mInstance.getApplicationContext();
    }

    @Nullable
    public static String getUserIdToken() {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account == null) {
            return null;
        }
        return account.getIdToken();
    }

    public static void signOutFromGoogle(final GoogleApiClient client, final Activity activity) {
        try {
            Auth.GoogleSignInApi.signOut(client).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.d(TAG, "signOut status: " + status.getStatus());
                            activity.finish();
                            final Intent intent = new Intent(activity, SignInActivity.class);
                            activity.startActivity(intent);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Cannot sign out: " + e.getMessage());
        }
    }

    public static String getFullServiceUrl() {
        return getSecurity() + "://" + getIpAddress() + ":" + getPort() + "/" + getService() + "/";
    }

    public static String getSecurity() {
        return mPreferences.getString(SECURITY_KEY, "http");
    }

    public static String getIpAddress() {
        return mPreferences.getString(IP_ADDRESS_KEY, "192.168.1.15");
    }

    public static String getPort() {
        return mPreferences.getString(PORT_KEY, "8080");
    }

    public static String getService() {
        return mPreferences.getString(SERVICE_NAME_KEY, "service/rest");
    }

    public static void saveConfig(final String security, final String ip, final String port, final String service) {
        mPreferences.edit().putString(SECURITY_KEY, security).apply();
        mPreferences.edit().putString(IP_ADDRESS_KEY, ip).apply();
        mPreferences.edit().putString(PORT_KEY, port).apply();
        mPreferences.edit().putString(SERVICE_NAME_KEY, service).apply();
    }

    public static List<String> getSecurityLevels() {
        final List<String> securityLevels = new ArrayList<>();
        securityLevels.add("http");
        securityLevels.add("https");
        return securityLevels;
    }

    public static boolean appIsOnline() {
        return mInstance.isOnline();
    }

    private boolean isOnline(){
        final ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        final NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void setCurrentUser(@NonNull User user) {
        mInstance.mCurrentUser = checkNotNull(user, "user cannot be null");
    }

    public static void setAdminMenuVisibleIfAdmin(final NavigationView navigationView ) {
        final Menu navigationDrawerMenu = navigationView.getMenu();
        if (mInstance.mCurrentUser == null) {
            return;
        }
        if (mInstance.mCurrentUser.getUserLevel() != UserLevel.ADMIN) {
            return;
        }
        navigationDrawerMenu.findItem(R.id.nav_users).setVisible(true);

    }

    public static void setReportMenuVisibleIfProjectLeaderOrAdmin(final NavigationView navigationView ) {
        final Menu navigationDrawerMenu = navigationView.getMenu();
        if (mInstance.mCurrentUser == null) {
            return;
        }
        if (mInstance.mCurrentUser.getUserLevel() != UserLevel.ADMIN && mInstance.mCurrentUser.getUserLevel() != UserLevel.PROJECT_LEADER) {
            return;
        }
        navigationDrawerMenu.findItem(R.id.nav_reports).setVisible(true);

    }

    @Nullable
    public static User getCurrentUser() {
        return mInstance.mCurrentUser;
    }

    public static boolean currentUserExists() {
        return mInstance.mCurrentUser != null;
    }

    public static void deleteUserProfilePicture() {
        final boolean exists = getContext().getFileStreamPath(PROFILE_PICTURE_NAME).exists();
        if (!exists) {
            Log.d(TAG, "Not deleting profile picture, not exists.");
            return;
        }
        final boolean deleted = getContext().deleteFile(PROFILE_PICTURE_NAME);
        if (!deleted) {
            Log.d(TAG, "Cannot delete existing user picture.");
            return;
        }
        Log.d(TAG, "Successfully deleted profile picture.");
    }

    public static void saveUserProfilePicture(final Activity activity, final Uri pictureUrl) {
        loadProfilePictureTo(activity, pictureUrl, getFileTarget(), null);
    }

    private static Target getFileTarget() {
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            final FileOutputStream outputStream = getContext().openFileOutput(PROFILE_PICTURE_NAME, MODE_PRIVATE);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                            outputStream.flush();
                            outputStream.close();
                            Log.d(TAG, "Successfully saved profile picture to file " + getContext().getFileStreamPath(PROFILE_PICTURE_NAME));
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
    }

    public static void setGoogleAccountDataToNavigationDrawer(final Activity activity) {
        final NavigationView navigationView = activity.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        ImageView navPicture = headerView.findViewById(R.id.nav_profile_picture);
        TextView navName = headerView.findViewById(R.id.nav_profile_name);
        TextView navEmail = headerView.findViewById(R.id.nav_profile_email);

        final GoogleSignInAccount account = getGoogleSignInAccount();
        assert account != null;
        navName.setText(account.getDisplayName());
        navEmail.setText(account.getEmail());

        final File file = getContext().getFileStreamPath(PROFILE_PICTURE_NAME);
        if (file.exists()) {
            Picasso.with(activity)
                    .load(file).transform(new CircleTransform())
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(navPicture);
            Log.d(TAG, "Successfully loaded profile picture from file.");
            return;
        }

        Log.d(TAG, "Not loaded profile picture from file. Loading it from internet...");
        loadProfilePictureTo(activity, account.getPhotoUrl(), null, navPicture);
        saveUserProfilePicture(activity, account.getPhotoUrl());
    }

    private static void loadProfilePictureTo(final Activity activity, final Uri pictureUrl, final Target target, final ImageView targetView) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        int iconSize = 64;
        if (am != null) {
            iconSize = am.getLauncherLargeIconSize();
        }
        if (targetView != null) {
            Picasso.with(activity)
                    .load(pictureUrl).transform(new CircleTransform())
                    .resize(iconSize, iconSize)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(targetView);
        } else {
            Picasso.with(activity)
                    .load(pictureUrl).transform(new CircleTransform())
                    .resize(iconSize, iconSize)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(target);
        }
    }
}
