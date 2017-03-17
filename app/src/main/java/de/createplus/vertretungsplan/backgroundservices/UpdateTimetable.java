package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.databases.ThtmlDatabaseHelper;
import de.createplus.vertretungsplan.settings.SettingsActivity;

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
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String classPref = sharedPref.getString(SettingsActivity.KEY_STUFE, "");
        String weekPref = sharedPref.getString(SettingsActivity.KEY_WEEK, "");
        if(weekPref.length()==0)weekPref= "7";
        //Log.e("timetabel",""+Integer.parseInt(classPref));
        String out = "";
        if(classPref.length() > 0 && Integer.parseInt(classPref) <23 && Integer.parseInt(classPref) >0){
            try{
                Timetable Plan = new Timetable(Integer.parseInt(weekPref),"c",Integer.parseInt(classPref),"schueler","SuS74!");
                Plan.update();
                Plan.print();
                out = Plan.getHtml();

            }catch (IOException e){
                out = "<html><h1>Download Error</h1> </html>";
            }
        }else{
            out = "<html><h1>Stufen Einstellung nicht verwendbar.</h1> </html>";
        }

        ThtmlDatabaseHelper db = new ThtmlDatabaseHelper(this);
        db.removeAll();
        db.addLine(weekPref,classPref,out);
        //Plan.print();
        //System.out.print(Plan.toString());
        Intent localIntent = new Intent(Constants.BROADCAST_ACTION).putExtra(Constants.EXTENDED_DATA_STATUS, out);

        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
