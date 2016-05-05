package com.kii.world;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kii.cloud.storage.KiiUser;

/**
 * Created by irori on 2016/05/05.
 */
public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String error = null;
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String senderId = getString(R.string.gcm_defaultSenderId);
                String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                boolean development = true;
                KiiUser.pushInstallation(development).install(token);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            error = e.getLocalizedMessage();
        }
        Intent registrationComplete = new Intent("com.kii.world.COMPLETED");
        registrationComplete.putExtra("ErrorMessage", error);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}