package de.createplus.vertretungsplan.settings;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.backgroundservices.Pair;
import de.createplus.vertretungsplan.backgroundservices.UpdateTimetable;
import de.createplus.vertretungsplan.databases.TgroupsContract;
import de.createplus.vertretungsplan.databases.TgroupsDatabaseHelper;
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
    public static final String KEY_AD_BANNER = "pref_key_ad_status";
    public static final String KEY_COURSE_SELECTION = "pref_key_list_timetableselection";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_overview);
        //ListPreference lp = (ListPreference) findViewById();
        if(MainActivity.TimetableIndex != null) {
            final ListPreference listPreference = (ListPreference) findPreference("pref_key_list_class");
            setListPreferenceDataCLASS(listPreference);
        }

        TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
        LinkedList<String[]> coursesall = dbgroups.getAllCourses();
        if(coursesall.size() > 0) {
            final MultiSelectListPreference listPreference = (MultiSelectListPreference) findPreference(KEY_COURSE_SELECTION);
            setListPreferenceDataCOURSE(listPreference,coursesall);
        }

        /*TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
        LinkedList<String[]> coursesall = dbgroups.getAllGroups();
        /*for(int i = 0; i < coursesall.size();i++){
            String[] tmp = coursesall.get(i);
            for(int o = i+1; o < coursesall.size();o++){
                if(tmp[1].equals(coursesall.get(o)[1])){
                    Log.e("lol",Arrays.toString(coursesall.get(i)));
                    coursesall.remove(o);
                    o--;
                }
            }
        }
        for(int i = 0; i < coursesall.size();i++){
            Arrays.toString(coursesall.get(i));
        }*/


        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Log.e("SETTINGS","Changed.");
        Context con = getApplicationContext();
        Intent mServiceIntent = new Intent(con, UpdateTimetable.class);
        con.startService(mServiceIntent);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> courseSET = sharedPref.getStringSet(SettingsActivity.KEY_COURSE_SELECTION, null);
        TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
        SQLiteDatabase wdb = dbgroups.getWritableDatabase();
        ContentValues val = new ContentValues();
        val.put(TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED,"0");
        wdb.update(TgroupsContract.TgroupsEntry.TABLE_NAME,val,TgroupsContract.TgroupsEntry._ID + " > ?",  new String[]{"0"});

        if(courseSET != null){
            String[] tmp = new String[courseSET.size()];
            int o = 0;
            for (String i : courseSET){
                //Log.e("GROUP SET TO TRUE",i);
                tmp[o] = i;
                o++;
            }
        /*for(int i = 0; i < tmp.length; i++){
            tmp[i] = tmp[i].split("SPLITPOINT")[1];
        }*/
            val = new ContentValues();
            val.put(TgroupsContract.TgroupsEntry.COLUMN_NAME_ENABLED,"1");
            for(int i = 0; i < tmp.length;i++){
                wdb.update(TgroupsContract.TgroupsEntry.TABLE_NAME,val,TgroupsContract.TgroupsEntry.COLUMN_NAME_COURSE + " = ?",  new String[]{tmp[i]});
            }
            MainActivity.THIS.updateContainerContent();
        }
    }

    protected static void setListPreferenceDataCLASS(ListPreference lp) {
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

    protected static void setListPreferenceDataCOURSE(MultiSelectListPreference lp,LinkedList<String[]> in) {


        CharSequence[] entries = new CharSequence[in.size()];
        CharSequence[] entryValues = new CharSequence[in.size()];
        Set<String> dafaultValues = new HashSet<String>();

        for(int i = 0; 0 < in.size();i++){
            String[] tmp = in.remove();
            entries[i] = tmp[0];
            entryValues[i] = tmp[0];
            if(Integer.parseInt(tmp[1])==1){
                dafaultValues.add(tmp[0]);
            }
        }
        //Log.e("ADDING",""+ dafaultValues);

        lp.setEntries(entries);
        lp.setValues(dafaultValues);
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