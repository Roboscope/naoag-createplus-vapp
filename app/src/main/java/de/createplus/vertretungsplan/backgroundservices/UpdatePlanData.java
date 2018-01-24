package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.createplus.vertretungsplan.config;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.settings.SettingsActivity;
import de.createplus.vertretungsplan.substitutionplan.Substitutionplan;

import java.io.IOException;


public class UpdatePlanData extends IntentService {
    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "de.createplus.vertretungsplan.backgroundservices.UpdatePlanData.BROADCAST";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "de.createplus.vertretungsplan.backgroundservices.UpdatePlanData.STATUS";
    }


    public UpdatePlanData() {
        super("UpdatePlanData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("ERROR", "THIS METHOD IS OUTDATED. <---------------------");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean teacherMode = sharedPref.getBoolean(SettingsActivity.KEY_TEACHERMODE, false);
        if (teacherMode) {
            if (teacherPWtest()) return;
            Log.e("VERTRETUNGSPLAN", "Updating teacher plan");
            teacherModeUpdate();
        } else {
            Log.e("VERTRETUNGSPLAN", "Testing Student Pw");
            if (studentPWtest()) return;
            Log.e("VERTRETUNGSPLAN", "Updating student plan");
            studentModeUpdate();
        }
    }

    private boolean teacherPWtest() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String teacherUSERNAMEPref = sharedPref.getString(SettingsActivity.KEY_TEACHER_USERNAME, "");
        String teacherPASSWORDPref = sharedPref.getString(SettingsActivity.KEY_TEACHER_PASSWORD, "");

        if (teacherUSERNAMEPref.length() <= 0 || teacherPASSWORDPref.length() <= 0) {
            Intent localIntent = new Intent(UpdatePlanData.Constants.BROADCAST_ACTION).putExtra(UpdatePlanData.Constants.EXTENDED_DATA_STATUS, "Bitte Namen und Passwort in den Einstellungen auswählen.");

            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return true;
        }

        if (!teacherUSERNAMEPref.equals(config.teacherUsername) || !teacherPASSWORDPref.equals(config.teacherPasswd)) {
            Intent localIntent = new Intent(UpdatePlanData.Constants.BROADCAST_ACTION).putExtra(UpdatePlanData.Constants.EXTENDED_DATA_STATUS, "Lehrer Passwort oder Benutzername falsch.");
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return true;
        }
        return false;
    }

    private boolean studentPWtest() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String studentUSERNAMEPref = sharedPref.getString(SettingsActivity.KEY_STUDENT_USERNAME, "");
        String studentPASSWORDPref = sharedPref.getString(SettingsActivity.KEY_STUDENT_PASSWORD, "");

        if (studentUSERNAMEPref.length() <= 0 || studentPASSWORDPref.length() <= 0) {
            Intent localIntent = new Intent(UpdatePlanData.Constants.BROADCAST_ACTION).putExtra(UpdatePlanData.Constants.EXTENDED_DATA_STATUS, "Bitte Namen und Passwort in den Einstellungen auswählen.");
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return true;
        }

        if (!studentUSERNAMEPref.equals(config.studentUsername) || !studentPASSWORDPref.equals(config.studentPasswd)) {
            Intent localIntent = new Intent(UpdatePlanData.Constants.BROADCAST_ACTION).putExtra(UpdatePlanData.Constants.EXTENDED_DATA_STATUS, "Schüler Passwort oder Benutzername falsch.");
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return true;
        }
        return false;
    }

    public void teacherModeUpdate() {
        SPDatabaseHelper SPDbHelper = new SPDatabaseHelper(this);

        int maxplans = SPDownloader.download(1, 1, "Lehrer", "lehrer", "LwuerseLen", this);
        //Log.e("VERTRETINGSPLANDOWNLOAD",""+maxplans);
        if (maxplans > 0) {
            SPDbHelper.removeAll();
            for (int i = 1; i <= maxplans; i++) {
                maxplans = SPDownloader.download(i, 1, "Lehrer", "lehrer", "LwuerseLen", this);
                //Log.e("VERTRETINGSPLANDOWNLOAD","LOOP:"+i);
            }
            maxplans = SPDownloader.download(1, 2, "Lehrer", "lehrer", "LwuerseLen", this);
            for (int i = 2; i <= maxplans; i++) {
                maxplans = SPDownloader.download(i, 2, "Lehrer", "lehrer", "LwuerseLen", this);
                //Log.e("VERTRETINGSPLANDOWNLOAD","LOOP:"+i);
            }
        }

        //Log.e("VERTRETINGSPLAN",""+maxplans);
        Intent localIntent;
        if (maxplans > 0) {
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, config.spPlanSuccessReloadMsg);
        } else {
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, config.spPlanFailReloadMsg);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    public void studentModeUpdate() {
        SPDatabaseHelper SPDbHelper = new SPDatabaseHelper(this);

        int maxplans = SPDownloader.download(1, 1, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
        //Log.e("VERTRETINGSPLANDOWNLOAD",""+maxplans);
        if (maxplans > 0) {
            SPDbHelper.removeAll();
            for (int i = 1; i <= maxplans; i++) {
                maxplans = SPDownloader.download(i, 1, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
                //Log.e("VERTRETINGSPLANDOWNLOAD","LOOP:"+i);
            }
            maxplans = SPDownloader.download(1, 2, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
            for (int i = 2; i <= maxplans; i++) {
                maxplans = SPDownloader.download(i, 2, config.spPlanStudentDir, config.studentUsername, config.studentPasswd, this);
                //Log.e("VERTRETINGSPLANDOWNLOAD","LOOP:"+i);
            }
        }

        //Log.e("VERTRETINGSPLAN",""+maxplans);
        Intent localIntent;
        if (maxplans > 0) {
            //Log.e("VERTRETINGSPLAN","DONE");
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, config.spPlanSuccessReloadMsg);
        } else {
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, config.spPlanFailReloadMsg);
        }


        //Log.e("Test","ICH HABS GESCHAFFT");


        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
