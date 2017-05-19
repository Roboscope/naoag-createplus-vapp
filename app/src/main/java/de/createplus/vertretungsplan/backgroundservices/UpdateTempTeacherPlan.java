package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.config;
import de.createplus.vertretungsplan.databases.TgroupsDatabaseHelper;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;
import de.createplus.vertretungsplan.databases.TplanDatabaseHelper;
import de.createplus.vertretungsplan.settings.SettingsActivity;
import de.createplus.vertretungsplan.tempteachermode.TempTeacherModeActivity;

import static android.content.ContentValues.TAG;
import static de.createplus.vertretungsplan.backgroundservices.Timetable.getTimtableIndex;


public class UpdateTempTeacherPlan extends IntentService {
    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "de.createplus.vertretungsplan.backgroundservices.UpdateTimetable.BROADCAST";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "de.createplus.vertretungsplan.backgroundservices.UpdateTimetable.STATUS";
    }


    public UpdateTempTeacherPlan() {
        super("UpdateTimetable");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e(TAG,"LÄUFT");


        Timetable.urlArchive = "http://gymnasium-wuerselen.de/untis/Lehrer-Stundenplan/";
        Timetable.varname = "teachers";

        Intent localIntent = new Intent(UpdateTempTeacherPlan.Constants.BROADCAST_ACTION).putExtra(UpdateTempTeacherPlan.Constants.EXTENDED_DATA_STATUS, "<html>[----]0% Lade Lehrer Plan für: "+ TempTeacherModeActivity.teacher.toUpperCase()+" (Loading)</html>");
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        Pair data = null;
        try {
            data = Timetable.getTimtableIndex(config.teacherUsername, config.teacherPasswd);
        }catch (IOException e){
            e.printStackTrace();
        }
        int teacherindex = -1;
        if(data == null || data.a == null ||data.b == null){
            localIntent = new Intent(UpdateTempTeacherPlan.Constants.BROADCAST_ACTION).putExtra(UpdateTempTeacherPlan.Constants.EXTENDED_DATA_STATUS, "<html>Download fehlgeschlagen</html>");
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }
        for(int i = 0; i < data.a.length;i++){
            if(data.a[i].toUpperCase().replace("","").equals(TempTeacherModeActivity.teacher.toUpperCase().replace(" ",""))){
                teacherindex = i+1;
                break;
            }
        }

        String out = "";
        int[] weeks = new int[2];

        String[] Indexes = MainActivity.TimetableIndex.b;
        String[] weekCurrent = Indexes[0].split(">");
        String[] weekNext = Indexes[1].split(">");
        //Log.e("Timetable", weekCurrent[0]);
        //Log.e("Timetable", weekNext[0]);
        weeks[0] = Integer.parseInt(weekCurrent[0].replace("\"",""));
        weeks[1] = Integer.parseInt(weekNext[0].replace("\"",""));

        localIntent = new Intent(UpdateTempTeacherPlan.Constants.BROADCAST_ACTION).putExtra(UpdateTempTeacherPlan.Constants.EXTENDED_DATA_STATUS, "<html>[=---]25% Lade Lehrer Plan für: "+ TempTeacherModeActivity.teacher.toUpperCase()+" ("+teacherindex+")</html>");
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        String classPref = ""+teacherindex;
        //-------------------------------------------------------------
        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[0],"t",Integer.parseInt(classPref),config.teacherUsername,config.teacherPasswd);
                Plan.update();
                //Plan.print();
                /*TplanDatabaseHelper dbplan = new TplanDatabaseHelper(getApplicationContext());
                dbplan.removeAll();
                dbplan.close();*/
                //Plan.addToSQL(this);

                String weekname = "A";
                if(weeks[0]%2 > 0){
                    weekname = "B";
                }
                if(Plan.getHtml() != null){
                    out = Plan.getHtml().replace("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">", "<font size=\"7\" face=\"Arial\" color=\"#0000FF\">" + weekname +":");
                }



            }catch (IOException e){
                e.printStackTrace();
                out = "Download Error (Plan für: "+ weekCurrent[1] +")";
            }
        }else{
            out = "Bitte Stufe in den Einstellungen auswählen.";
        }

        localIntent = new Intent(UpdateTempTeacherPlan.Constants.BROADCAST_ACTION).putExtra(UpdateTempTeacherPlan.Constants.EXTENDED_DATA_STATUS, "<html>[===-]75% Lade Lehrer Plan für: "+ TempTeacherModeActivity.teacher.toUpperCase()+" ("+teacherindex+")</html>");
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        if(classPref.length() > 0 && Integer.parseInt(classPref) > 0){
            try{
                Timetable Plan = new Timetable(weeks[1],"t",Integer.parseInt(classPref),config.teacherUsername,config.teacherPasswd);
                Plan.update();
                //Plan.print();

                //Plan.addToSQL(this);

                String weekname = "A";
                if(weeks[1]%2 > 0){
                    weekname = "B";
                }
                if(Plan.getHtml() != null){
                    out = out +"\n" +Plan.getHtml().replace("<font size=\"7\" face=\"Arial\" color=\"#0000FF\">", "<font size=\"7\" face=\"Arial\" color=\"#0000FF\">" + weekname +":");
                }



            }catch (IOException e){
                e.printStackTrace();
                out = "Download Error (Vertretungsplan für: "+ weekNext[1] +")";
            }
        }else{
            out = "Bitte Stufe in den Einstellungen auswählen.";
        }

        localIntent = new Intent(UpdateTempTeacherPlan.Constants.BROADCAST_ACTION).putExtra(UpdateTempTeacherPlan.Constants.EXTENDED_DATA_STATUS, "<html>"+out+"</html>");
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);


        Timetable.urlArchive = "http://gymnasium-wuerselen.de/untis/Schueler-Stundenplan/";
        Timetable.varname = "classes";
    }



}
