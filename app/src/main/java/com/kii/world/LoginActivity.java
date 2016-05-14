//
//
// Copyright 2012 Kii Corporation
// http://kii.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//

package com.kii.world;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;

//**extends AppCompatActivity にしないとだめかも？**
public class LoginActivity extends Activity {

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private static final String TAG = "LoginActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // define our UI elements
    private TextView mUsernameField;
    private TextView mPasswordField;
    private ProgressDialog mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        // link our variables to UI elements
        mUsernameField = (TextView) findViewById(R.id.username_field);
        mPasswordField = (TextView) findViewById(R.id.password_field);


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String errorMessage = intent.getStringExtra("ErrorMessage");
                Log.e("GCMTest", "Registration completed:" + errorMessage);
                if (errorMessage != null) {
                    Toast.makeText(LoginActivity.this, "Error push registration:" + errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Succeeded push registration", Toast.LENGTH_LONG).show();
                }
            }
        };


//プッシュ通知利用可能かチェック
        if (!checkPlayServices()) {
            Toast.makeText(LoginActivity.this, "This application needs Google Play Services", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(LoginActivity.this, "Google Play Services OK", Toast.LENGTH_LONG).show();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter("com.kii.world.COMPLETED"));
    }
    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }



    // called by the 'Sign Up' button on the UI
    public void handleSignUp(View v) {

        // show a loading progress dialog
        mProgress = ProgressDialog.show(LoginActivity.this, "",
                "Signing up...", true);

        // get the username/password combination from the UI
        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();
        Log.v(TAG, "Registering: " + username + ":" + password);

        // create a KiiUser object
        try {
            KiiUser user = KiiUser.createWithUsername(username);
            // register the user asynchronously
            user.register(new KiiUserCallBack() {

                // catch the callback's "done" request
                public void onRegisterCompleted(int token, KiiUser user,
                                                Exception e) {

                    // hide our progress UI element
                    mProgress.cancel();

                    // check for an exception (successful request if e==null)
                    if (e == null) {

                        // tell the console and the user it was a success!
                        Log.v(TAG, "Registered: " + user.toString());
                        showToast("User registered!");

                        // go to the next screen
                        Intent myIntent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        LoginActivity.this.startActivity(myIntent);

                    }

                    // otherwise, something bad happened in the request
                    else {

                        // tell the console and the user there was a failure
                        Log.v(TAG, "Error registering: " + e.getLocalizedMessage());
                        showToast("Error Registering: " + e.getLocalizedMessage());

                    }

                }

            }, password);

        } catch (Exception e) {
            mProgress.cancel();
            showToast("Error signing up: " + e.getLocalizedMessage());
        }

    }

    // called by the 'Log In' button on the UI
    public void handleLogin(View v) {

        // show a loading progress dialog
        mProgress = ProgressDialog.show(LoginActivity.this, "",
                "Signing in...", true);

        // get the username/password combination from the UI
        String username = mUsernameField.getText().toString();
        String password = mPasswordField.getText().toString();
        Log.v(TAG, "Logging in: " + username + ":" + password);

        // authenticate the user asynchronously
        KiiUser.logIn(new KiiUserCallBack() {

            // catch the callback's "done" request
            public void onLoginCompleted(int token, KiiUser user, Exception e) {

                // hide our progress UI element
                mProgress.cancel();

                // check for an exception (successful request if e==null)
                if (e == null) {

                    // tell the console and the user it was a success!
                    Log.v(TAG, "Logged in: " + user.toString());
                    showToast("User authenticated!");


                    // go to the main screen
                    Intent myIntent = new Intent(LoginActivity.this,
                            MainActivity.class);
                    LoginActivity.this.startActivity(myIntent);

                }

                // otherwise, something bad happened in the request
                else {

                    // tell the console and the user there was a failure
                    Log.v(TAG, "Error registering: " + e.getLocalizedMessage());
                    showToast("Error registering: " + e.getLocalizedMessage());

                }

            }

        }, username, password);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("PushTest", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
