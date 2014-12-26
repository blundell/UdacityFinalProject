package com.blundell.viewvideoyoutube.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class YouTubeSyncService extends Service {
    private static final Object lock = new Object();
    private static YouTubeSyncAdapter youTubeSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("YouTubeSyncAdapter", "onCreate - YouTubeSyncAdapter");
        synchronized (lock) {
            if (youTubeSyncAdapter == null) {
                youTubeSyncAdapter = new YouTubeSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return youTubeSyncAdapter.getSyncAdapterBinder();
    }
}
