package com.hw.szoftarch.worklogger;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;


public class WorkLoggerApplication extends Application {
    private static WorkLoggerApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getContext() {
        return mInstance.getApplicationContext();
    }

    public static String getUserIdToken() {
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        assert account != null;
        return account.getIdToken();
    }

    public static void verifyTokenOnServer() {
        final String idToken = getUserIdToken();
        if (idToken == null) {
            return;
        }
        final boolean valid = ServerHelper.validate(idToken);
        if (!valid) {
            ServerHelper.addUser(idToken);
        }
    }
}
