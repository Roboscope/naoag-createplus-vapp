package de.createplus.vertretungsplan.backgroundservices;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;

import de.createplus.vertretungsplan.MainActivity;
import de.createplus.vertretungsplan.R;
import de.createplus.vertretungsplan.tempteachermode.TempTeacherModeActivity;


public class UpdateTempTeacherPlanReceiver extends BroadcastReceiver {
    // Prevents instantiation
    private TempTeacherModeActivity activity;

    public UpdateTempTeacherPlanReceiver(TempTeacherModeActivity activity) {
        this.activity = activity;
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = intent.getExtras().getString(UpdateTempTeacherPlan.Constants.EXTENDED_DATA_STATUS);
        activity.setContent(msg);




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
