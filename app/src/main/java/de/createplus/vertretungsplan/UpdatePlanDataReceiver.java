package de.createplus.vertretungsplan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;

/**
 * TODO: MISSING JAVADOC
 *
 * @author Roboscope
 * @version 1.0
 * @since 2017-02-03
 */
public class UpdatePlanDataReceiver extends BroadcastReceiver {
    // Prevents instantiation
    private MainActivity activity;

    public UpdatePlanDataReceiver(MainActivity activity) {
        this.activity = activity;
    }
    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        Snackbar.make(activity.findViewById(R.id.main_content), "DONE", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        activity.Plan = intent.getExtras().getString(UpdatePlanData.Constants.EXTENDED_DATA_STATUS);
        activity.updateContainerContent();

    }
}
