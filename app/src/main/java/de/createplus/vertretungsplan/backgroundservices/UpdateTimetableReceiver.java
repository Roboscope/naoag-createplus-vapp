package de.createplus.vertretungsplan.backgroundservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.settings.SettingsActivity;


public class UpdateTimetableReceiver extends BroadcastReceiver {
    // Prevents instantiation
    private MainActivity activity;

    public UpdateTimetableReceiver(MainActivity activity) {
        this.activity = activity;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getExtras().getString(UpdateTimetable.Constants.EXTENDED_DATA_STATUS);

        if(msg.equals("Bitte Stufe in den Einstellungen auswählen.")){
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

        //showNotification(context, "Stundenplan Download", msg, R.drawable.ic_menu_send);

        activity.updateContainerContent();


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
