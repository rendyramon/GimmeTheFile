package com.drivfe.gimmethefile.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.drivfe.gimmethefile.Config;
import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.activities.AboutActivity;
import com.drivfe.gimmethefile.activities.SettingsActivity;
import com.drivfe.gimmethefile.utilities.HelperUtils;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    PreferenceManager mPrefManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        mPrefManager = getPreferenceManager();
        mPrefManager.setSharedPreferencesName(getString(R.string.settings_preferences));
        mPrefManager.setSharedPreferencesMode(Context.MODE_PRIVATE);

        findPreference(getString(R.string.pref_download_location))
                .setSummary(Config.DOWNLOAD_FOLDER);

        Preference defaultDir = (Preference) findPreference(getString(R.string.pref_download_location));
        defaultDir.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new FolderChooserDialog.Builder((SettingsActivity) getActivity())
                        .chooseButton(R.string.choose)
                        .initialPath(HelperUtils.getAppDownloadDirectory().getAbsolutePath())
                        .show();
                return true;
            }
        });

        Preference about = findPreference(getString(R.string.pref_about));
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        findPreference(key).setSummary(Config.DOWNLOAD_FOLDER);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrefManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefManager.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
