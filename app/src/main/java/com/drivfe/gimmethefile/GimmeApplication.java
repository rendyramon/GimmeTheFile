package com.drivfe.gimmethefile;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.drivfe.gimmethefile.utilities.HelperUtils;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class GimmeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new AnalyticsTree());
            if (!Fabric.isInitialized())
                Fabric.with(this, new Crashlytics());
        }

        PreferenceManager.setDefaultValues(this, getString(R.string.settings_preferences), Context.MODE_PRIVATE, R.xml.settings, false);
        HelperUtils.setAppDownloadDirectory(this, null);
    }
}