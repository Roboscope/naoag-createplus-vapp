package de.createplus.vertretungsplan.settings;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import de.createplus.vertretungsplan.R;

/**
 * Created by Max Nuglisch on 16.02.2017.
 */

public class SettingsActivity extends PreferenceActivity {
    public static final String KEY_STUFE = "pref_key_class";
    public static final String KEY_WEEK = "pref_key_timetable_week";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_overview);
        //ListPreference lp = (ListPreference) findViewById();
    }

}