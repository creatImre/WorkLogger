package com.hw.szoftarch.worklogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.networking.RetrofitClient;
import com.hw.szoftarch.worklogger.networking.WorkLoggerService;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import retrofit2.Call;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mStatusTextView = findViewById(R.id.status);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.server_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        final SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }

    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignIn.getLastSignedInAccount(this);
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            new GoogleSignInTask(result).execute((Void) null);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            WorkLoggerApplication.verifyTokenOnServer();
            assert account != null;
            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));
            updateUI(true);
        } else {
            updateUI(false);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            finish();
            Intent intent = new Intent(this, LogWorkActivity.class);
            startActivity(intent);
        } else {
            mStatusTextView.setText(R.string.signed_out);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    public class GoogleSignInTask extends AsyncTask<Void, Void, GoogleSignInResult> {

        private GoogleSignInResult result;

        Realm realm;

        GoogleSignInTask(GoogleSignInResult result) {
            this.result = result;
        }

        @Override
        protected GoogleSignInResult doInBackground(Void... params) {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account == null) {
                return result;
            }
            Log.d(SignInActivity.class.getName(), "id: " + result.getSignInAccount().getId() + ", token: " + result.getSignInAccount().getIdToken());
            try {
                realm = Realm.getDefaultInstance();
            } catch (RealmMigrationNeededException e) {
                Log.e(SignInActivity.class.getName(), "Migration needed by realm. Resetting realm...");
                Realm.deleteRealm(new RealmConfiguration.Builder().build());
                Realm.setDefaultConfiguration(new RealmConfiguration.Builder().build());
                realm = Realm.getDefaultInstance();
            }
            User localUser = realm.where(User.class)
                    .equalTo("googleId", account.getId())
                    .findFirst();

            final RetrofitClient client = new RetrofitClient();
            final WorkLoggerService service = client.createService();
            final Call<User> loginCall = service.login();
            User loginUser = null;
            try {
                loginUser = loginCall.execute().body();
            } catch (IOException e) {
                Log.e(SignInActivity.class.getName(), e.getMessage());
            }

            if (loginUser != null) {
                Log.d(SignInActivity.class.getName(), "loginUser is not null");
                if (localUser == null) {
                    storeUser(loginUser, realm);
                    Log.d(SignInActivity.class.getName(), "local user not found, storing loginUser...");
                } else {
                    localUser.update(loginUser, realm);
                    Log.d(SignInActivity.class.getName(), "local user found, updating...");
                }
                realm.close();
            } else {
                Log.d(SignInActivity.class.getName(), "loginUser is null");
            }
            return result;
        }

        private void storeUser(User user, Realm realm) {
            realm.beginTransaction();
            realm.copyToRealm(user);
            realm.commitTransaction();
            realm.close();
            Log.d(SignInActivity.class.getName(), "Storing is finished");
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(final GoogleSignInResult result) {
            hideProgressDialog();
            handleSignInResult(result);
        }

        @Override
        protected void onCancelled() {
            hideProgressDialog();
        }
    }
}