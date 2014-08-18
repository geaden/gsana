package com.geaden.android.app.gsana.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by geaden on 14/08/14.
 */
public class GsanaAuthenticatorService extends Service {
    private GsanaAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new GsanaAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
