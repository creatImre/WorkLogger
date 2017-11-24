package com.hw.szoftarch.worklogger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.hw.szoftarch.worklogger.entities.User;

import static com.google.common.base.Preconditions.checkNotNull;


public class WorkLoggerApplication extends Application {

    public static String SOCKET_KEY = "socket";
    public static String IP_ADDRESS_KEY = "ipAddress";
    public static String SERVICE_NAME_KEY = "serviceName";

    private static WorkLoggerApplication mInstance;
    private @Nullable User mCurrentUser = null;

    @Nullable
    public static GoogleSignInAccount getGoogleSignInAccount() {
        return GoogleSignIn.getLastSignedInAccount(getContext());
    }

    @NonNull
    public static String getGoogleId() {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account == null) {
            return "not logged in";
        }
        final String id = account.getId();
        if (id == null) {
            return "not logged in";
        }
        return id;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getContext() {
        return mInstance.getApplicationContext();
    }

    public static boolean userLoggedIn() {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        return account != null;
    }

    @Nullable
    public static String getUserIdToken() {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account == null) {
            return null;
        }
        return account.getIdToken();
    }

    public static String getServiceUrl() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String ip = preferences.getString(IP_ADDRESS_KEY, "192.168.1.15");
        final String socket = preferences.getString(SOCKET_KEY, "8080");
        final String serviceName = preferences.getString(SERVICE_NAME_KEY, "service/rest");
        return "http://" + ip + ":" + socket + "/" + serviceName + "/";
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

    @Nullable
    public static User getCurrentUser() {
        return mInstance.mCurrentUser;
    }

    public static boolean currentUserExists() {
        return mInstance.mCurrentUser != null;
    }
}
