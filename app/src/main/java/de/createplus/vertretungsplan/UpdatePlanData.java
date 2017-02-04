package de.createplus.vertretungsplan;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-03
 */
public class UpdatePlanData extends IntentService{
    public final class Constants {
        // Defines a custom Intent action
        public static final String BROADCAST_ACTION =
                "de.createplus.vertretungsplan.UpdatePlanData.BROADCAST";
        // Defines the key for the status "extra" in an Intent
        public static final String EXTENDED_DATA_STATUS =
                "de.createplus.vertretungsplan.UpdatePlanData.STATUS";
    }



    public UpdatePlanData() {
        super("UpdatePlanData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Substitutionplan SubPlan = new Substitutionplan(1,2,"Schueler","schueler","SuS74!");
        Intent localIntent;
        try{
            SubPlan.update();
            for(int i = 2; i < SubPlan.getMaxPlans()+1; i++){
                Substitutionplan SubPlanToADD = new Substitutionplan(i,2,"Schueler","schueler","SuS74!");
                SubPlanToADD.update();
                SubPlan.add(SubPlanToADD);
            }

            localIntent =
                    new Intent(Constants.BROADCAST_ACTION)
                            // Puts the status into the Intent
                            .putExtra(Constants.EXTENDED_DATA_STATUS, "DONE");
        }catch (IOException ex){

            localIntent =
                    new Intent(Constants.BROADCAST_ACTION)
                            // Puts the status into the Intent
                            .putExtra(Constants.EXTENDED_DATA_STATUS, "FAILED");

        }


        //Log.e("Test","ICH HABS GESCHAFFT");


        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
