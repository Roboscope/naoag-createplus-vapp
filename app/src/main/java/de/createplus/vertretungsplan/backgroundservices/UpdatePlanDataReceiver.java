package de.createplus.vertretungsplan.backgroundservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.backgroundservices.UpdatePlanData;
import de.createplus.vertretungsplan.settings.SettingsActivity;


public class UpdatePlanDataReceiver extends BroadcastReceiver {
    // Prevents instantiation
    private MainActivity activity;

    public UpdatePlanDataReceiver(MainActivity activity) {
        this.activity = activity;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        final String msg = intent.getExtras().getString(UpdatePlanData.Constants.EXTENDED_DATA_STATUS);
        final Context con = context;

        if(msg.equals("Bitte Namen und Passwort in den Einstellungen auswählen.")||msg.equals("Lehrer Passwort oder Benutzername falsch.")||msg.equals("Schüler Passwort oder Benutzername falsch.")){
            View.OnClickListener toSettings = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(activity, SettingsActivity.class);
                    activity.startActivity(myIntent);
                }
            };

            Snackbar.make(activity.findViewById(R.id.main_content), msg, Snackbar.LENGTH_LONG)
                    .setAction("Einstellungen", toSettings).setDuration(20000).show();
        }else {
            Snackbar.make(activity.findViewById(R.id.main_content), msg, Snackbar.LENGTH_LONG)
                    .setAction("Einstellungen", null).show();
        }

        activity.updateContainerContent();

        //showNotification(context, "Vertrtungsplan Download", msg, R.drawable.ic_menu_send);


        //activity.Plan = intent.getExtras().getString(UpdatePlanData.Constants.EXTENDED_DATA_STATUS);
        MainActivity.updating = false;
        MainActivity.mSwipeRefreshLayout.setRefreshing(false);
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

    }
}
