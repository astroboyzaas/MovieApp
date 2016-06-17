package com.example.android.movieapp.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Manuel on 10/06/2016.
 */
public class MovieAppSyncService extends Service {
    // Storage for an instance of the sync adapter
    private static MovieAppSyncAdapter sMovieAppSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sMovieAppSyncAdapter == null) {
                sMovieAppSyncAdapter = new MovieAppSyncAdapter(getApplicationContext(), true);
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sMovieAppSyncAdapter.getSyncAdapterBinder();
    }
}
