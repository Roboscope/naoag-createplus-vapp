package de.createplus.vertretungsplan.backgroundservices;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import de.createplus.vertretungsplan.databases.SPDatabaseHelper;
import de.createplus.vertretungsplan.substitutionplan.Substitutionplan;

import java.io.IOException;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-03
 */
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

        SPDatabaseHelper SPDbHelper = new SPDatabaseHelper(this);

        int maxplans = SPDownloader.download(1, 1, "Schueler", "schueler", "SuS74!", this);
        //Log.e("VERTRETINGSPLANDOWNLOAD",""+maxplans);
        if(maxplans > 0){
            SPDbHelper.removeAll();
            for(int i = 1; i <= maxplans; i++){
                maxplans = SPDownloader.download(i, 1, "Schueler", "schueler", "SuS74!", this);
                //Log.e("VERTRETINGSPLANDOWNLOAD","LOOP:"+i);
            }
            maxplans = SPDownloader.download(1, 2, "Schueler", "schueler", "SuS74!", this);
            for(int i = 2; i <= maxplans; i++){
                maxplans = SPDownloader.download(i, 2, "Schueler", "schueler", "SuS74!", this);
                //Log.e("VERTRETINGSPLANDOWNLOAD","LOOP:"+i);
            }
        }

        Intent localIntent;
        if(maxplans > 0){
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, "DONE");
        }else{
            localIntent = new Intent(Constants.BROADCAST_ACTION)
                    .putExtra(Constants.EXTENDED_DATA_STATUS, "FAILED");
        }



        //Log.e("Test","ICH HABS GESCHAFFT");


        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
