package com.hw.szoftarch.worklogger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.hw.szoftarch.worklogger.workinghour.LogWorkActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (WorkLoggerApplication.userLoggedIn()) {
            Log.d(LogWorkActivity.class.getName(), "User already logged in. Name: " + WorkLoggerApplication.getGoogleSignInAccount().getDisplayName());

            final Intent intent = new Intent(this, LogWorkActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } else {
            Log.d(LogWorkActivity.class.getName(), "User not logged in. Starting " + SignInActivity.class.getName() + "...");

            final Intent intent = new Intent(this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        }
    }
}
