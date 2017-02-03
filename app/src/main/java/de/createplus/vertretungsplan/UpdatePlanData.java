package de.createplus.vertretungsplan;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
        Log.e("Test","ICH HABS GESCHAFFT");

        Intent localIntent =
                new Intent(Constants.BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putExtra(Constants.EXTENDED_DATA_STATUS, "DONE");
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


}
