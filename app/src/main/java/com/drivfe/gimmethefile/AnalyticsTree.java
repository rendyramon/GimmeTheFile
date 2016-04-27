package com.drivfe.gimmethefile;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

public class AnalyticsTree extends Timber.Tree {
    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        if (priority == Log.VERBOSE || priority == Log.INFO || priority == Log.DEBUG)
            return;
        else {
            Crashlytics.log(priority, tag, message);
            if (t != null)
                Crashlytics.logException(t);
        }
    }
}
