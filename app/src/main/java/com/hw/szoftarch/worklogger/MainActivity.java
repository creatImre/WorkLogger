package com.hw.szoftarch.worklogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.hw.szoftarch.worklogger.workinghour.WorkingHourActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final GoogleSignInAccount account = WorkLoggerApplication.getGoogleSignInAccount();
        if (account != null) {
            Log.d(WorkingHourActivity.class.getName(), "User already logged in to Google. Name: " + account.getDisplayName());

            final Intent intent = new Intent(this, WorkingHourActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } else {
            Log.d(WorkingHourActivity.class.getName(), "User not logged in. Starting " + SignInActivity.class.getName() + "...");

            final Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        }
    }
}
