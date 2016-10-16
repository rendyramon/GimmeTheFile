package com.drivfe.gimmethefile.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.drivfe.gimmethefile.R;
import com.drivfe.gimmethefile.databinding.ActivitySettingsBinding;
import com.drivfe.gimmethefile.fragments.SettingsFragment;
import com.drivfe.gimmethefile.utilities.HelperUtils;

import java.io.File;

public class SettingsActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback {
    private final String SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.title_activity_settings));
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new SettingsFragment(), SETTINGS_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        HelperUtils.setAppDownloadDirectory(this, folder.getPath());
    }
}
