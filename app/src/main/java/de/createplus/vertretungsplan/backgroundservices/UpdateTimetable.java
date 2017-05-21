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
import de.createplus.vertretungsplan.config;
import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.databases.TgroupsDatabaseHelper;
import de.createplus.vertretungsplan.databases.ThtmlContract;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;
import de.createplus.vertretungsplan.databases.TplanDatabaseHelper;
import de.createplus.vertretungsplan.settings.SettingsActivity;

import static de.createplus.vertretungsplan.backgroundservices.Timetable.getTimtableIndex;


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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean teacherMode = sharedPref.getBoolean(SettingsActivity.KEY_TEACHERMODE, false);
        if(teacherMode){
            teacherModeUpdate();
        }else{
            studentModeUpdate();
        }
    }

    public void teacherModeUpdate(){
        Timetable.urlArchive = "http://gymnasium-wuerselen.de/untis/Lehrer-Stundenplan/";
        Timetable.varname = "teachers";
        ThtmlDatabaseHelper db = new ThtmlDatabaseHelper(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String classPref = sharedPref.getString(SettingsActivity.KEY_STUFE, "");
        String teacherUSERNAMEPref = sharedPref.getString(SettingsActivity.KEY_TEACHER_USERNAME, "");
        String teacherPASSWORDPref = sharedPref.getString(SettingsActivity.KEY_TEACHER_PASSWORD, "");

        if(teacherUSERNAMEPref.length() <=0 || teacherPASSWORDPref.length() <= 0){
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, "Bitte Namen und Passwort in den Einstellungen auswählen.");

            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }

        if(!teacherUSERNAMEPref.equals(config.teacherUsername) || !teacherPASSWORDPref.equals(config.teacherPasswd)){
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, "Lehrer Passwort oder Benutzername falsch.");
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }


        //SQLiteDatabase WBdb = db.getWritableDatabase();
        //WBdb.delete(ThtmlContract.ThtmlEntry.TABLE_NAME, ThtmlContract.ThtmlEntry.COLUMN_NAME_CALENDARWEEK + " < ?", new String[]{"1"});
        try{
            Pair pref = getTimtableIndex(teacherUSERNAMEPref,teacherPASSWORDPref);
            MainActivity.TimetableIndex = pref;
        }catch (IOException e){
            //db.addLine("0","0","<html> Offline Modus: Der Plan könnte veraltet sein! <html>");
            return;
        }

        int[] weeks = new int[2];

        String[] Indexes = MainActivity.TimetableIndex.b;
        String[] weekCurrent = Indexes[0].split(">");
        String[] weekNext = Indexes[1].split(">");
        //Log.e("Timetable", weekCurrent[0]);
        //Log.e("Timetable", weekNext[0]);
        weeks[0] = Integer.parseInt(weekCurrent[0].replace("\"",""));
        weeks[1] = Integer.parseInt(weekNext[0].replace("\"",""));


        Pair inSQL = db.getClassWeek();
        int testifequal = 0;
        //Log.e("inSQL", inSQL + "");
        if(inSQL.a[0] != null){
            if(inSQL.a[0].equals(weekCurrent[0].replace("\"",""))){
                //Log.e("Update Timetable","WEEK ONE IS EQUAL_TEST");
                testifequal++;
            }
            if(inSQL.b[0].equals(weekNext[0].replace("\"",""))){
                //Log.e("Update Timetable","WEEK TWO IS EQUAL");
                testifequal++;
            }
            if(inSQL.a[1].equals(classPref)){
                //Log.e("Update Timetable","CLASS IS EQUAL");

                testifequal++;
            }else {
                TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
                dbgroups.removeAll();
                dbgroups.close();
            }
            if(inSQL.b[1].equals(classPref)){
                //Log.e("Update Timetable","CLASS IS EQUAL");

                testifequal++;
            }else{
                TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
                dbgroups.removeAll();
                dbgroups.close();
            }
        }

        if(testifequal == 4){
            Log.e("Update Timetable: ","Not updating.");
            return;
        }Log.e("Update Timetable: ","Updating.");

        String msg = "Stundenplanplan erfolgreich neu geladen!";
        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[0],"t",Integer.parseInt(classPref),teacherUSERNAMEPref,teacherPASSWORDPref);
                Plan.update();
                //Plan.print();
                /*TplanDatabaseHelper dbplan = new TplanDatabaseHelper(getApplicationContext());
                dbplan.removeAll();
                dbplan.close();*/
                //Plan.addToSQL(this);
                db.removeAll();
                String weekname = "A";
                if(weeks[0]%2 > 0){
                    weekname = "B";
                }
                if(Plan.getHtml() != null){
                    db.addLine(weeks[0]+"",classPref,Plan.getHtml().replace("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">", "<font size=\"7\" face=\"Arial\" color=\"#0000FF\">" + weekname +":"));
                }



            }catch (IOException e){
                msg = "Download Error (Plan für: "+ weekCurrent[1] +")";
            }
        }else{
            msg = "Bitte Stufe in den Einstellungen auswählen.";
        }

        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[1],"t",Integer.parseInt(classPref),teacherUSERNAMEPref,teacherPASSWORDPref);
                Plan.update();
                //Plan.print();

                //Plan.addToSQL(this);

                String weekname = "A";
                if(weeks[1]%2 > 0){
                    weekname = "B";
                }
                if(Plan.getHtml() != null){
                    db.addLine(weeks[1]+"",classPref,Plan.getHtml().replace("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">", "<font size=\"7\" face=\"Arial\" color=\"#0000FF\">" + weekname +":"));
                }



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
    public void studentModeUpdate(){
        Timetable.urlArchive = "http://gymnasium-wuerselen.de/untis/Schueler-Stundenplan/";
        Timetable.varname = "classes";

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

        if(!studentUSERNAMEPref.equals(config.studentUsername) || !studentPASSWORDPref.equals(config.studentPasswd)){
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
            e.printStackTrace();
            //db.addLine("0","0","<html> Offline Modus: Der Plan könnte veraltet sein! <html>");
            return;
        }
        if(MainActivity.TimetableIndex.a == null){
            return;
        }

        int[] weeks = new int[2];

        String[] Indexes = MainActivity.TimetableIndex.b;
        String[] weekCurrent = Indexes[0].split(">");
        String[] weekNext = Indexes[1].split(">");
        //Log.e("Timetable", weekCurrent[0]);
        //Log.e("Timetable", weekNext[0]);
        weeks[0] = Integer.parseInt(weekCurrent[0].replace("\"",""));
        weeks[1] = Integer.parseInt(weekNext[0].replace("\"",""));

        Log.e("Update Timetable: ","Done Loading");
        Pair inSQL = db.getClassWeek();
        int testifequal = 0;
        //Log.e("inSQL", inSQL + "");
        if(inSQL.a[0] != null){
            if(inSQL.a[0].equals(weekCurrent[0].replace("\"",""))){
                //Log.e("Update Timetable","WEEK ONE IS EQUAL_TEST");
                testifequal++;
            }
            if(inSQL.b[0].equals(weekNext[0].replace("\"",""))){
                //Log.e("Update Timetable","WEEK TWO IS EQUAL");
                testifequal++;
            }
            if(inSQL.a[1].equals(classPref)){
                //Log.e("Update Timetable","CLASS IS EQUAL");

                testifequal++;
            }else {
                TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
                dbgroups.removeAll();
                dbgroups.close();
            }
            if(inSQL.b[1].equals(classPref)){
                //Log.e("Update Timetable","CLASS IS EQUAL");

                testifequal++;
            }else{
                TgroupsDatabaseHelper dbgroups = new TgroupsDatabaseHelper(getApplicationContext());
                dbgroups.removeAll();
                dbgroups.close();
            }
        }

        if(testifequal == 4){
            Log.e("Update Timetable: ","Not updating.");
            return;
        }Log.e("Update Timetable: ","Updating.");

        String msg = "Stundenplanplan erfolgreich neu geladen!";
        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[0],"c",Integer.parseInt(classPref),studentUSERNAMEPref,studentPASSWORDPref);
                Plan.update();
                Plan.print();
                TplanDatabaseHelper dbplan = new TplanDatabaseHelper(getApplicationContext());
                dbplan.removeAll();
                dbplan.close();
                Plan.addToSQL(this);
                db.removeAll();
                String weekname = "A";
                if(weeks[0]%2 > 0){
                    weekname = "B";
                }
                if(Plan.getHtml() != null){
                    db.addLine(weeks[0]+"",classPref,Plan.getHtml().replace("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">", "<font size=\"7\" face=\"Arial\" color=\"#0000FF\">" + weekname +":"));
                }



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
                Plan.print();

                Plan.addToSQL(this);

                String weekname = "A";
                if(weeks[1]%2 > 0){
                    weekname = "B";
                }
                if(Plan.getHtml() != null){
                    db.addLine(weeks[1]+"",classPref,Plan.getHtml().replace("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">", "<font size=\"7\" face=\"Arial\" color=\"#0000FF\">" + weekname +":"));
                }



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
