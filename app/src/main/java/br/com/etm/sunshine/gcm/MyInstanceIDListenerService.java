package br.com.etm.sunshine.gcm;

import android.content.Intent;
import android.support.annotation.StringRes;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by EDUARDO_MARGOTO on 1/9/2017.
 */

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";


    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startActivity(intent);
    }


}
