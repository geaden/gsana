package com.geaden.android.gsana.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GsanaSyncService extends Service {
    private final String LOG_TAG = GsanaSyncService.class.getSimpleName();

    private static final Object sSyncAdapterLock = new Object();
    private static GsanaSyncAdapter sGsanaSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - GsanaSynceService");
        synchronized (sSyncAdapterLock) {
            if (sGsanaSyncAdapter == null) {
                sGsanaSyncAdapter = new GsanaSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sGsanaSyncAdapter.getSyncAdapterBinder();
    }
}
