package br.com.etm.sunshine.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import br.com.etm.sunshine.MainActivity;
import br.com.etm.sunshine.R;

/**
 * Created by EDUARDO_MARGOTO on 1/9/2017.
 */

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

//        try {
//
//            synchronized (TAG) {
//                InstanceID instanceID = InstanceID.getInstance(this);
//                String token =
//                        instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
//
//                sendRegistrationToServer(token);
//
//                sharedPreferences.edit()
//                        .putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, true).apply();
//            }
//
//        }catch (Exception e){
//            Log.d(TAG, "Failed to complete token refresh", e);
//
//            sharedPreferences.edit()
//                    .putBoolean(MainActivity.SENT_TOKEN_TO_SERVER, false).apply();
//
//        }
    }


    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        Log.i(TAG, "GCM Registration Token: " + token);
    }


}
