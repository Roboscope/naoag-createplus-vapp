package de.createplus.vertretungsplan.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.createplus.vertretungsplan.R;

/**
 * Created by Max Nuglisch on 16.02.2017.
 */

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_overview);
    }
}