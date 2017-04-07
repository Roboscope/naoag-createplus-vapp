package de.createplus.vertretungsplan.settings;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.backgroundservices.Pair;
import de.createplus.vertretungsplan.backgroundservices.UpdateTimetable;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;

/**
 * Created by Max Nuglisch on 16.02.2017.
 */

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_STUFE = "pref_key_list_class";
    public static final String KEY_STUDENT_PASSWORD = "pref_key_studentlogin_password";
    public static final String KEY_STUDENT_USERNAME = "pref_key_studentlogin_username";
    public static final String KEY_TEACHERMODE = "pref_key_teacherlogin_status";
    public static final String KEY_TEACHER_PASSWORD = "pref_key_teacherlogin_password";
    public static final String KEY_TEACHER_USERNAME = "pref_key_teacherlogin_username";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_overview);
        //ListPreference lp = (ListPreference) findViewById();
        if(MainActivity.TimetableIndex != null) {
            final ListPreference listPreference = (ListPreference) findPreference("pref_key_list_class");
            setListPreferenceData(listPreference);
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Context con = getApplicationContext();
        Intent mServiceIntent = new Intent(con, UpdateTimetable.class);
        con.startService(mServiceIntent);

        //ThtmlDatabaseHelper db = new ThtmlDatabaseHelper(this);
        //db.getClassWeek();
    }

    protected static void setListPreferenceData(ListPreference lp) {
        Pair Info = MainActivity.TimetableIndex;

        CharSequence[] entries = Info.a;
        CharSequence[] entryValues = new CharSequence[entries.length];
        for(int i = 0; i < entries.length; i++){
            entryValues[i] = ""+(i+1);
        }
        lp.setEntries(entries);
        lp.setDefaultValue("1");
        lp.setEntryValues(entryValues);
    }

    private void showNotification(Context context, String title, String text, int icon) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(text);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

}}