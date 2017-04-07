package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.databases.ThtmlContract;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;
import de.createplus.vertretungsplan.settings.SettingsActivity;

import static de.createplus.vertretungsplan.backgroundservices.Timetable.getTimtableIndex;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-03
 */
public class UpdateTimetable extends IntentService {
    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "de.createplus.vertretungsplan.backgroundservices.UpdateTimetable.BROADCAST";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "de.createplus.vertretungsplan.backgroundservices.UpdateTimetable.STATUS";
    }


    public UpdateTimetable() {
        super("UpdateTimetable");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ThtmlDatabaseHelper db = new ThtmlDatabaseHelper(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String classPref = sharedPref.getString(SettingsActivity.KEY_STUFE, "");
        String studentUSERNAMEPref = sharedPref.getString(SettingsActivity.KEY_STUDENT_USERNAME, "");
        String studentPASSWORDPref = sharedPref.getString(SettingsActivity.KEY_STUDENT_PASSWORD, "");

        if(studentUSERNAMEPref.length() <=0 || studentPASSWORDPref.length() <= 0){
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, "Bitte Namen und Passwort in den Einstellungen auswählen.");

            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }

        if(!studentUSERNAMEPref.equals("schueler") || !studentPASSWORDPref.equals("SuS74!")){
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, "Schüler Passwort oder Benutzername falsch.");

            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }
        //SQLiteDatabase WBdb = db.getWritableDatabase();
        //WBdb.delete(ThtmlContract.ThtmlEntry.TABLE_NAME, ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK + " < ?", new String[]{"1"});
        try{
            Pair pref = getTimtableIndex(studentUSERNAMEPref,studentPASSWORDPref);
            MainActivity.TimetableIndex = pref;
        }catch (IOException e){
            //db.addLine("0","0","<html> Offline Modus: Der Plan könnte veraltet sein! <html>");
            return;
        }

        int[] weeks = new int[2];

        String[] Indexes = MainActivity.TimetableIndex.b;
        String[] weekCurrent = Indexes[0].split(">");
        String[] weekNext = Indexes[1].split(">");
        Log.e("Timetable", weekCurrent[0]);
        Log.e("Timetable", weekNext[0]);
        weeks[0] = Integer.parseInt(weekCurrent[0].replace("\"",""));
        weeks[1] = Integer.parseInt(weekNext[0].replace("\"",""));


        Pair inSQL = db.getClassWeek();
        int testifequal = 0;
        Log.e("inSQL", inSQL + "");
        if(inSQL.a[0].equals(weekCurrent[0].replace("\"",""))){
            Log.e("EQUALIZER","A IS EQUAL");
            testifequal++;
        }
        if(inSQL.b[0].equals(weekNext[0].replace("\"",""))){
            Log.e("EQUALIZER","B IS EQUAL");
            testifequal++;
        }
        if(inSQL.a[1].equals(classPref)){
            Log.e("EQUALIZER","A2 IS EQUAL");
            testifequal++;
        }
        if(inSQL.b[1].equals(classPref)){
            Log.e("EQUALIZER","B2 IS EQUAL");
            testifequal++;
        }

        if(testifequal == 4){
            return;
        }

        String msg = "Vertretungsplan erfolgreich neu geladen!";
        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[0],"c",Integer.parseInt(classPref),studentUSERNAMEPref,studentPASSWORDPref);
                Plan.update();
                //Plan.print();
                db.removeAll();
                db.addLine(weeks[0]+"",classPref,Plan.getHtml());

            }catch (IOException e){
                msg = "Download Error (Plan für: "+ weekCurrent[1] +")";
            }
        }else{
            msg = "Bitte Stufe in den Einstellungen auswählen.";
        }

        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[1],"c",Integer.parseInt(classPref),studentUSERNAMEPref,studentPASSWORDPref);
                Plan.update();
                //Plan.print();
                db.addLine(weeks[1]+"",classPref,Plan.getHtml());

            }catch (IOException e){
                msg = "Download Error (Vertretungsplan für: "+ weekNext[1] +")";
            }
        }else{
            msg = "Bitte Stufe in den Einstellungen auswählen.";
        }


        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, msg);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
