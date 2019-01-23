package org.smartregister.ug.hpv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import org.joda.time.DateTime;
import org.smartregister.domain.FetchStatus;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.service.intent.ExtendedSyncIntentService;
import org.smartregister.ug.hpv.service.intent.SyncIntentService;
import org.smartregister.ug.hpv.util.ServiceTools;

import java.io.Serializable;
import java.util.ArrayList;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static org.smartregister.util.Log.logError;

/**
 * Created by Jason Rogena - jrogena@ona.io on 12/05/2017.
 */

public class SyncStatusBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_SYNC_STATUS = "sync_status";
    public static final String EXTRA_FETCH_STATUS = "fetch_status";
    public static final String EXTRA_COMPLETE_STATUS = "complete_status";

    private static SyncStatusBroadcastReceiver singleton;
    private final ArrayList<SyncStatusListener> syncStatusListeners;
    private boolean isSyncing;
    private boolean alarmsTriggered = false;
    private long lastFetchedTimestamp;

    public SyncStatusBroadcastReceiver() {
        syncStatusListeners = new ArrayList<>();
    }

    public static void init(Context context) {
        if (singleton != null) {
            destroy(context);
        }

        singleton = new SyncStatusBroadcastReceiver();
        context.registerReceiver(singleton,
                new IntentFilter(SyncStatusBroadcastReceiver.ACTION_SYNC_STATUS));
    }

    public static void destroy(Context context) {
        try {
            if (singleton != null) {
                context.unregisterReceiver(singleton);
            }
        } catch (IllegalArgumentException e) {
            logError("Error on destroy: " + e);
        }
    }

    public static SyncStatusBroadcastReceiver getInstance() {
        return singleton;
    }

    public void addSyncStatusListener(SyncStatusListener syncStatusListener) {
        if (!syncStatusListeners.contains(syncStatusListener)) {
            syncStatusListeners.add(syncStatusListener);
        }
    }

    public void removeSyncStatusListener(SyncStatusListener syncStatusListener) {
        if (syncStatusListeners.contains(syncStatusListener)) {
            syncStatusListeners.remove(syncStatusListener);
        }
    }

    public boolean isSyncing() {
        return isSyncing;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();
        if (data != null) {
            Serializable fetchStatusSerializable = data.getSerializable(EXTRA_FETCH_STATUS);
            if (fetchStatusSerializable != null && fetchStatusSerializable instanceof FetchStatus) {
                FetchStatus fetchStatus = (FetchStatus) fetchStatusSerializable;
                if (fetchStatus.equals(FetchStatus.fetchStarted)) {
                    started();
                } else {
                    boolean isComplete = data.getBoolean(EXTRA_COMPLETE_STATUS);
                    if (isComplete) {
                        complete(fetchStatus, context);

                        boolean wakeup = intent.getBooleanExtra(SyncIntentService.WAKE_UP, false);
                        startExtendedSyncAndAlarms(context, wakeup);
                    } else {
                        inProgress(fetchStatus);
                    }
                }
            }
        }
    }

    private void started() {
        isSyncing = true;
        lastFetchedTimestamp = 0;
        for (SyncStatusListener syncStatusListener : syncStatusListeners) {
            syncStatusListener.onSyncStart();
        }
    }

    private void inProgress(FetchStatus fetchStatus) {
        isSyncing = true;

        long currentTimeStamp = DateTime.now().getMillis();
        if (lastFetchedTimestamp != 0) {
            long timeDiff = currentTimeStamp - lastFetchedTimestamp;
            if (timeDiff < DrishtiSyncScheduler.SYNC_INTERVAL) {
                return;
            }
        }

        lastFetchedTimestamp = currentTimeStamp;

        for (SyncStatusListener syncStatusListener : syncStatusListeners) {
            syncStatusListener.onSyncInProgress(fetchStatus);
        }
    }

    private void complete(FetchStatus fetchStatus, Context context) {
        if (fetchStatus.equals(FetchStatus.nothingFetched) || fetchStatus.equals(FetchStatus.fetchedFailed)) {
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(getDefaultSharedPreferences(context.getApplicationContext()));
            allSharedPreferences.saveIsSyncInitial(false);
        }
        isSyncing = false;
        lastFetchedTimestamp = 0;
        for (SyncStatusListener syncStatusListener : syncStatusListeners) {
            syncStatusListener.onSyncComplete(fetchStatus);
        }
    }

    private void startExtendedSyncAndAlarms(Context context, boolean wakeup) {
        startExtendedSync(context, wakeup);
        if (!alarmsTriggered) {
            HpvApplication.setAlarms(context);
            alarmsTriggered = true;
        }
    }

    private void startExtendedSync(Context context, boolean wakeup) {
        ServiceTools.startService(context, ExtendedSyncIntentService.class, wakeup);
    }

    public interface SyncStatusListener {
        void onSyncStart();

        void onSyncInProgress(FetchStatus fetchStatus);

        void onSyncComplete(FetchStatus fetchStatus);
    }
}
